package com.arqaam.logframelab.service;

import com.arqaam.logframelab.controller.dto.FiltersDto;
import com.arqaam.logframelab.exception.*;
import com.arqaam.logframelab.model.IndicatorResponse;
import com.arqaam.logframelab.model.persistence.Indicator;
import com.arqaam.logframelab.model.persistence.Level;
import com.arqaam.logframelab.model.projection.IndicatorFilters;
import com.arqaam.logframelab.repository.IndicatorRepository;
import com.arqaam.logframelab.repository.LevelRepository;
import com.arqaam.logframelab.util.Logging;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.docx4j.jaxb.Context;
import org.docx4j.jaxb.XPathBinderAssociationIsPartialException;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.ObjectFactory;
import org.docx4j.wml.R;
import org.docx4j.wml.Text;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.criteria.Predicate;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;

@Service
public class IndicatorService implements Logging {

  /** Indicates the number of indicators template the level has by default */
  protected static final Integer IMPACT_NUM_TEMP_INDIC = 1,
      OUTCOME_NUM_TEMP_INDIC = 3,
      OUTPUT_NUM_TEMP_INDIC = 2;

  private final IndicatorRepository indicatorRepository;

  private final LevelRepository levelRepository;

  public IndicatorService(IndicatorRepository indicatorRepository, LevelRepository levelRepository) {
    this.indicatorRepository = indicatorRepository;
    this.levelRepository = levelRepository;
  }

  /**
   * Extract Indicators from a Word file
   *
   * @param file Word file
   * @param filter <code>FilterDto</code>
   * @return List of Indicators
   */
  public List<IndicatorResponse> extractIndicatorsFromWordFile(
      MultipartFile file, FiltersDto filter) {
    List<IndicatorResponse> result = new ArrayList<>();
    List<Indicator> indicatorsList;

    if (filter != null && !filter.isEmpty()) {
      indicatorsList = indicatorRepository.findAll(specificationForIndicatorFromFilter(filter));
    } else {
      indicatorsList = indicatorRepository.findAll();
    }
    List<String> wordstoScan = new ArrayList<>(); // current words
    // get the maximum indicator length
    int maxIndicatorLength = 1;
    if (indicatorsList != null && !indicatorsList.isEmpty()) {
      for (Indicator ind : indicatorsList) {
        if (ind.getKeywordsList() != null) {
          for (String words : ind.getKeywordsList()) {
            int numberKeywords = words.split(" ").length;
            if (numberKeywords > maxIndicatorLength) {
              maxIndicatorLength = numberKeywords;
            }
          }
        }
      }
      try {
        WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(file.getInputStream());
        MainDocumentPart mainDocumentPart = wordMLPackage.getMainDocumentPart();
        String textNodesXPath = "//w:t";
        List<Object> textNodes = mainDocumentPart.getJAXBNodesViaXPath(textNodesXPath, true);
        Pattern p = Pattern.compile("[a-z0-9]", Pattern.CASE_INSENSITIVE);
        StringBuffer currentWord = null;
        Map<Long, Indicator> mapResult = new HashMap<>();
        for (Object obj : textNodes) {
          Text text = ((JAXBElement<Text>) obj).getValue();
          char[] strArray = text.getValue().toLowerCase().toCharArray();
          for (char c : strArray) {
            // append current word to wordstoScan list
            if (currentWord == null && p.matcher(c + "").find()) {
              currentWord = new StringBuffer();
            } else if (currentWord != null && !p.matcher(c + "").find()) {
              wordstoScan.add(currentWord.toString());
              currentWord = null;
            }
            if (currentWord != null) {
              currentWord.append(c);
            }
            // clear wordstoScan list if exceed the max length of indicators
            if (wordstoScan.size() == maxIndicatorLength) {
              checkIndicators(wordstoScan, indicatorsList, mapResult);
              wordstoScan.remove(wordstoScan.size() - 1);
            }
          }
        }
        if (!mapResult.isEmpty()) {
          List<Level> levelsList = levelRepository.findAllByOrderByPriority();
          logger().info("Starting the sort of the indicators {}", mapResult);
          // Sort by Level and then by number of times a keyword was tricked
          result =
              mapResult.values().stream()
                  .sorted(
                      (o1, o2) -> {
                        if (o1.getLevel().getId().equals(o2.getLevel().getId())) {
                          return o1.getNumTimes() > o2.getNumTimes()
                              ? -1
                              : (o1.getNumTimes().equals(o2.getNumTimes()) ? 0 : 1);
                        }
                        for (Level level : levelsList) {
                          if (level.getPriority().equals(o1.getLevel().getPriority())) return -1;
                          if (level.getPriority().equals(o2.getLevel().getPriority())) return 1;
                        }
                        return 1;
                      })
                  .map(this::convertIndicatorToIndicatorResponse)
                  .collect(Collectors.toList());
        }
      } catch (XPathBinderAssociationIsPartialException | JAXBException e) {
        logger().error("Failed to process temporary word file", e);
        throw new FailedToProcessWordFileException();
      } catch (Docx4JException e) {
        logger()
            .error(
                "Failed to load/process the temporary word file. Name of the file: {}",
                file.getName(),
                e);
        throw new WordFileLoadFailedException();
      } catch (IOException e) {
        logger().error("Failed to open word file. Name of the file: {}", file.getName(), e);
        throw new FailedToOpenFileException();
      }
    }
    return result;
  }

  /**
     * Fills a list of indicators that contain certain words
     * @param wordsToScan Words to find in the indicators' keyword list
     * @param indicators Indicators to be analyzed
     * @param mapResult Map Indicators' Id and IndicatorResponses
     */
    protected void checkIndicators(List<String> wordsToScan, List<Indicator> indicators,
                                   Map<Long, Indicator> mapResult) {
        logger().debug("Check Indicators with wordsToScan: {}, indicators: {}, mapResult: {}",
                wordsToScan, indicators, mapResult);
        String wordsStr = wordsToScan.stream()
                .collect(Collectors.joining(" ", " ", " "));
        // key1 key2 key3 compared to ke,key1,key2 key3
        for(Indicator indicator : indicators) {
            if (indicator.getKeywordsList() != null && !indicator.getKeywordsList().isEmpty()) {
                for(String currentKey : indicator.getKeywordsList()) {
                    if (wordsStr.toLowerCase().contains(" " + currentKey.toLowerCase() + " ")) {
                        // new indicator found
                        if (mapResult.containsKey(indicator.getId())) {
                            mapResult.get(indicator.getId()).setNumTimes(mapResult.get(indicator.getId()).getNumTimes()+1);
                        }else {
                            mapResult.put(indicator.getId(), indicator);
                        }
                    }
                }
            }
        }
    }

    /**
     * Creates Word File that contains indicators
     * @param indicatorResponses Indicators to be put on the word file
     * @return Word File
     */
    public ByteArrayOutputStream exportIndicatorsInWordFile(List<IndicatorResponse> indicatorResponses) {

        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            //File template = new File("/var/www/indicatorsExportTemplate.docx");
            WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(new ClassPathResource("indicatorsExportTemplate.docx").getFile());
            MainDocumentPart mainDocumentPart = wordMLPackage.getMainDocumentPart();
            String textNodesXPath = "//w:t";
            List<Object> textNodes = mainDocumentPart.getJAXBNodesViaXPath(textNodesXPath, true);
            for(Object obj : textNodes) {
                Text text = ((JAXBElement<Text>) obj).getValue();
                String value = text.getValue();
                if (value != null && value.contains("{var}")) {
                    parseVarWithValue(text, value, indicatorResponses);
                }
            }
            wordMLPackage.save(outputStream);
            return outputStream;
        } catch (Docx4JException | JAXBException e) {
            logger().error("Failed to process word template", e);
            throw new FailedToProcessWordFileException();
        } catch (IOException e) {
            logger().error("Template was not Found", e);
            throw new TemplateNotFoundException();
        }
    }

    /**
     * Parse Nodes with indicators' label when the text param contains the indicators' var attribute
     *
     * @param textNode   Node that will have elements parsed into
     * @param text       Text to be found in the indicators' var
     * @param indicators Indicators that have the label to be parsed
     */
    protected void parseVarWithValue(Text textNode, String text, List<IndicatorResponse> indicators) {
        textNode.setValue("");
        ObjectFactory factory = Context.getWmlObjectFactory();
        R runParent = (R) textNode.getParent();
        logger().info("Parsing Var with Value with text:{}, indicators: {}", text, indicators);
        if (indicators != null && !indicators.isEmpty())
            for (IndicatorResponse indicator : indicators) {
                if (text.contains(indicator.getVar())) {
                    Text TextIndicator = factory.createText();
                    TextIndicator.setValue(indicator.getName());
                    runParent.getContent().add(TextIndicator);
                    runParent.getContent().add(factory.createBr());
                    runParent.getContent().add(factory.createBr());
                    runParent.getContent().add(factory.createBr());
                    runParent.getContent().add(factory.createBr());
                }
            }
    }

    /**
     * Import Indicators from an worksheet/excel file with the extension xlsx
     * @param file Worksheet file
     */
     public List<Indicator> importIndicators(MultipartFile file) {
        List<Level> levels = levelRepository.findAll();
        Map<String, Level> levelMap = new HashMap<>();
        for (Level lvl : levels){
            levelMap.put(lvl.getName(), lvl);
        }
       
        logger().info("Importing indicators from xlsx, name {}", file.getName());
        try {
            List<Indicator> indicatorList = new ArrayList<>();
            Iterator<Row> iterator = new XSSFWorkbook(file.getInputStream()).getSheetAt(0).iterator();
            // skip the headers row
            if (iterator.hasNext()) {
                iterator.next();
            }
            int count=-1;
            while (iterator.hasNext()) {
                logger().info(" ");
                Row currentRow = iterator.next();

                // key words
                String[] keys = currentRow.getCell(2).getStringCellValue().toLowerCase().split(",");
                for (int i = 0; i < keys.length; i++) {
                    keys[i] = keys[i].trim().replaceAll("\\s+", " ");
                }
                Level level = levelMap.get(currentRow.getCell(0).getStringCellValue().toUpperCase());
                if (!isNull(level)) {
                    Cell crsCodeCell = currentRow.getCell(7, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                    Indicator indicator = Indicator.builder()
                            .level(level)
                            .themes(currentRow.getCell(1, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue())
                            .keywords(String.join(",", keys))
                            .name(currentRow.getCell(3, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue())
                            .description(currentRow.getCell(4, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue())
                            .source(currentRow.getCell(5, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue())
                            .disaggregation(currentRow.getCell(6, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue().equalsIgnoreCase("yes"))
                            .crsCode(crsCodeCell.getCellType().equals(CellType.NUMERIC) ? String.valueOf(crsCodeCell.getNumericCellValue()) : crsCodeCell.getStringCellValue())
                            .sdgCode(currentRow.getCell(8, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue())
                            .sourceVerification(currentRow.getCell(9, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue())
                            .dataSource(currentRow.getCell(10, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue())
                            .build();
                    logger().info("Line {}, Saving this indicator {}", count, indicator);
                    indicatorList.add(indicatorRepository.save(indicator));
                    count++;
                }
            }
            return indicatorList;

        } catch (IOException e) {
            logger().error("Failed to open worksheet.", e);
            throw new FailedToOpenWorksheetException();
        }/* catch (InvalidFormatException e) {
            logger().error("Failed to interpret worksheet. It must be in a wrong format.", e);
            throw new WorksheetInWrongFormatException();
        }*/
    }

    /**
     * Import Indicators from an worksheet/excel file with the extension xlsx
     * @param indicatorResponses Indicators to written in the excel file
     * @return Worksheet in xlsx format
     */
    public ByteArrayOutputStream exportIndicatorsInWorksheet(List<IndicatorResponse> indicatorResponses) {

        List<Indicator> indicatorList = indicatorRepository.findAllById(indicatorResponses.stream()
                                                           .map(IndicatorResponse::getId)
                                                           .collect(Collectors.toList()));

        logger().info("Write indicators into a worksheet, indicator {}", indicatorResponses);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet();
        String[] columns = new String[]{"Level", "Themes", "Name", "Description", "Source", "Disaggregation", "DAC 5/CRS", "SDG", "Source of Verification", "Data Source"};

        // Create a CellStyle with the font
        Font boldFont = workbook.createFont();
        boldFont.setBold(true);
        CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFont(boldFont);

        CellStyle redCellStyle = workbook.createCellStyle();
        redCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        redCellStyle.setFillForegroundColor(IndexedColors.RED.getIndex());

        CellStyle yellowCellStyle = workbook.createCellStyle();
        yellowCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        yellowCellStyle.setFillForegroundColor(IndexedColors.YELLOW.getIndex());

        // add the headers row
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < columns.length; i++) {
            addCellWithStyle(headerRow, i, columns[i], headerCellStyle);
        }

        int rowNum = 1;
        for (Indicator indicator : indicatorList) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(indicator.getLevel().getName());
            row.createCell(1).setCellValue(indicator.getThemes());
            row.createCell(2).setCellValue(indicator.getName());
            row.createCell(3).setCellValue(indicator.getDescription());
            row.createCell(4).setCellValue(indicator.getSource());
            addCellWithStyle(row, 5, isNull(indicator.getDisaggregation())? "" : (indicator.getDisaggregation() ? "Yes" : "No"), yellowCellStyle);
            addCellWithStyle(row, 6, indicator.getCrsCode(), redCellStyle);
            addCellWithStyle(row, 7, indicator.getSdgCode(), redCellStyle);
            addCellWithStyle(row, 8, indicator.getSourceVerification(), yellowCellStyle);
            row.createCell(9).setCellValue(indicator.getDataSource());
        }

        // Resize all columns to fit the content size
        for(int i = 0; i < columns.length; i++) {
            sheet.autoSizeColumn(i);
        }

        try {
            workbook.write(outputStream);
            workbook.close();
        } catch (IOException e) {
            logger().error("Failed to write/close the worksheet",e);
            throw new FailedToCloseFileException();
        }
        return outputStream;
    }

    /**
     * Creates a cell with a certain style and with a set value
     * @param row Row of the cell that will be created
     * @param i Index of the column of the cell
     * @param value Text of the cell
     * @param cellStyle Cell style (color, font, ...)
     */
    private void addCellWithStyle(Row row, Integer i, String value, CellStyle cellStyle){
        Cell cell = row.createCell(i);
        cell.setCellValue(value);
        cell.setCellStyle(cellStyle);
    }

    /**
     * Get all thematic areas of indicators
     * @return all thematic areas
     */
    public List<String> getAllThemes() {
        return indicatorRepository.getThemes();
    }

    public FiltersDto getFilters() {
        List<IndicatorFilters> filtersResult = indicatorRepository.getAllBy();

        FiltersDto filters = new FiltersDto();

        filters.getThemes().addAll(filtersResult.stream().map(IndicatorFilters::getThemes).filter(f -> !f.isEmpty()).collect(Collectors.toList()));
        filters.getSource().addAll(filtersResult.stream().map(IndicatorFilters::getSource).filter(f -> !f.isEmpty()).collect(Collectors.toList()));
        filters.getLevel().addAll(filtersResult.stream().map(IndicatorFilters::getLevel).collect(Collectors.toList()));
        filters.getSdgCode().addAll(filtersResult.stream().map(IndicatorFilters::getSdgCode).filter(f -> !f.isEmpty()).collect(Collectors.toList()));
        filters.getCrsCode().addAll(filtersResult.stream().map(IndicatorFilters::getCrsCode).filter(f -> !f.isEmpty()).collect(Collectors.toList()));

        return filters;
    }

    /**
     * Returns indicators that match the filters
     * @param themes List of Themes
     * @param sources List of Sources
     * @param levels List of Levels id
     * @param sdgCodes List of SDG codes
     * @param crsCodes List of CRS Codes
     * @return List of IndicatorResponse
     */
    public List<Indicator> getIndicators(Optional<List<String>> themes, Optional<List<String>> sources, Optional<List<Long>> levels,
                                                 Optional<List<String>> sdgCodes, Optional<List<String>> crsCodes) {
        logger().info("Starting repository call with with themes: {}, sources: {}, levels: {}, sdgCodes: {}, crsCodes: {}",
                themes, sources, levels, sdgCodes, crsCodes);
        return !themes.isPresent() && !sources.isPresent() && !levels.isPresent() && !sdgCodes.isPresent() && !crsCodes.isPresent() ?
            indicatorRepository.findAll() :
            indicatorRepository.findAll((Specification<Indicator>) (root, criteriaQuery, criteriaBuilder) -> {
                List<Predicate> predicates = new ArrayList<>();
                themes.ifPresent(x -> predicates.add(criteriaBuilder.and(criteriaBuilder.in(root.get("themes")).value(x))));
                sources.ifPresent(x -> predicates.add(criteriaBuilder.and(criteriaBuilder.in(root.get("source")).value(x))));
                levels.ifPresent(x -> predicates.add(criteriaBuilder.and(criteriaBuilder.in(root.get("level").get("id")).value(x))));
                sdgCodes.ifPresent(x -> predicates.add(criteriaBuilder.and(criteriaBuilder.in(root.get("sdgCode")).value(x))));
                crsCodes.ifPresent(x -> predicates.add(criteriaBuilder.and(criteriaBuilder.in(root.get("crsCode")).value(x))));
                return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
            });
    }

    /**
     * Converts Indicator to Indicator response
     * @param indicator Indicator to be converted
     * @return IndicatorResponse
     */
    public IndicatorResponse convertIndicatorToIndicatorResponse(Indicator indicator) {
        return IndicatorResponse.builder()
                .id(indicator.getId())
                .level(indicator.getLevel().getName())
                .color(indicator.getLevel().getColor())
                .name(indicator.getName())
                .description(indicator.getDescription())
                .var(indicator.getLevel().getTemplateVar())
                .themes(indicator.getThemes())
                .disaggregation(indicator.getDisaggregation())
                .crsCode(indicator.getCrsCode())
                .sdgCode(indicator.getSdgCode())
                .source(indicator.getSource())
                .numTimes(indicator.getNumTimes())
                .build();
    }

  private Specification<Indicator> specificationForIndicatorFromFilter(FiltersDto filter) {
    return (root, criteriaQuery, criteriaBuilder) -> {
      List<Predicate> predicates = new ArrayList<>();
      if (filter.getThemes().size() > 0) {
        predicates.add(
            criteriaBuilder.and(criteriaBuilder.in(root.get("themes")).value(filter.getThemes())));
      } else {
        predicates.add(criteriaBuilder.like(root.get("themes"), "%"));
      }

      if (filter.getSource().size() > 0) {
        predicates.add(
            criteriaBuilder.and(criteriaBuilder.in(root.get("source")).value(filter.getSource())));
      } else {
        predicates.add(criteriaBuilder.like(root.get("source"), "%"));
      }

      if (filter.getLevel().size() > 0) {
        predicates.add(
            criteriaBuilder.and(
                criteriaBuilder
                    .in(root.get("level").get("id"))
                    .value(
                        filter.getLevel().stream()
                            .map(Level::getId)
                            .collect(Collectors.toList()))));
      } else {
        predicates.add(criteriaBuilder.like(root.get("level").get("id").as(String.class), "_"));
      }

      if (filter.getSdgCode().size() > 0) {
        predicates.add(
            criteriaBuilder.and(
                criteriaBuilder.in(root.get("sdgCode")).value(filter.getSdgCode())));
      } else {
        predicates.add(criteriaBuilder.like(root.get("sdgCode"), "%"));
      }

      if (filter.getCrsCode().size() > 0) {
        predicates.add(
            criteriaBuilder.and(
                criteriaBuilder.in(root.get("crsCode")).value(filter.getCrsCode())));
      } else {
        predicates.add(criteriaBuilder.like(root.get("crsCode"), "%"));
      }
      return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    };
  }
    /**
     * Fills the DFID template with the indicators
     * @param indicatorResponse Indicators to fill the indicator file
     * @return The DFID template filled with the indicators
     */
    public ByteArrayOutputStream exportIndicatorsDFIDFormat(List<IndicatorResponse> indicatorResponse){
        try {
            logger().info("Start exporting Indicators in DFID format");
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            XSSFWorkbook wk = new XSSFWorkbook(new ClassPathResource("RF_Template.xlsx").getInputStream());
            XSSFSheet sheet  = wk.getSheetAt(0);
            List<Level> levels = levelRepository.findAllByOrderByPriority();
            List<Indicator> indicatorList = indicatorRepository.findAllById(indicatorResponse.stream()
                    .mapToLong(IndicatorResponse::getId).boxed().collect(Collectors.toList()));
            List<Indicator> impactIndicators = new ArrayList<>();
            List<Indicator> outcomeIndicators = new ArrayList<>();
            List<Indicator> outputIndicators = new ArrayList<>();

            for (Indicator indicator : indicatorList) {
                // Can't do switch because the values aren't known before runtime
                if(levels.get(0).equals(indicator.getLevel())) {
                    impactIndicators.add(indicator);
                }else if(levels.get(1).equals(indicator.getLevel())) {
                    outcomeIndicators.add(indicator);
                }else {
                    outputIndicators.add(indicator);
                }
            }

            logger().info("Impact Indicators: {}\nOutcome Indicators: {}\nOutput Indicators: {}", impactIndicators, outcomeIndicators, outputIndicators);
            int startRowNewIndicator = 1;
            startRowNewIndicator = fillIndicatorsPerLevel(sheet, impactIndicators, startRowNewIndicator, IMPACT_NUM_TEMP_INDIC);
            startRowNewIndicator = fillIndicatorsPerLevel(sheet, outcomeIndicators, startRowNewIndicator, OUTCOME_NUM_TEMP_INDIC);
            fillIndicatorsPerLevel(sheet, outputIndicators, startRowNewIndicator, OUTPUT_NUM_TEMP_INDIC);
            wk.write(output);
            wk.close();
            return output;
        } catch (IOException e) {
            logger().error("Failed to open template worksheet.", e);
            throw new FailedToOpenWorksheetException();
        }
    }

    /**
     * Fill the level's template indicators with the values and add new rows if necessary.
     * @param sheet The Template worksheet's sheet
     * @param indicatorList List of Indicators to fill on the template
     * @param startRowNewIndicator Index of the row where the template starts
     * @param numberTemplateIndicators Number of Indicator's Template of this level
     * @return Index of the row where the next template starts
     */
    private Integer fillIndicatorsPerLevel(XSSFSheet sheet, List<Indicator> indicatorList, Integer startRowNewIndicator,
                                           Integer numberTemplateIndicators){
        Integer initialRow = startRowNewIndicator;
        int count = 0;
        logger().info("Starting to fill Indicators. Start Row New Indicator {}, Number of template indicators of level: {}",
                startRowNewIndicator, numberTemplateIndicators);
        // Fill the available spaces
        while(count < indicatorList.size() && count < numberTemplateIndicators) {
            sheet.getRow(startRowNewIndicator + 1).getCell(2).setCellValue(indicatorList.get(count).getName());
            sheet.getRow(startRowNewIndicator + 3).getCell(3).setCellValue(indicatorList.get(count).getSourceVerification());
            startRowNewIndicator +=4;
            count++;
        }

        // If needs new rows
        if (indicatorList.size() > numberTemplateIndicators) {
            logger().info("Adding new rows to worksheet to insert indicators. IndicatorList Size: {}", indicatorList.size());
            for (int i = numberTemplateIndicators; i < indicatorList.size(); i++) {
                if (numberTemplateIndicators == i && numberTemplateIndicators.equals(OUTCOME_NUM_TEMP_INDIC)) {
                    logger().info("Searching for cells needing unmerging in the first column, numberTemplateIndicators: {}", numberTemplateIndicators);
                    // Unmerge merged on the first column, so it can be merged later
                    List<CellRangeAddress> mergedRegions = sheet.getMergedRegions();
                    for (int j = 0; j < mergedRegions.size(); j++) {
                        // Check every first row of template indicators including the last row of the previous template
                        if (mergedRegions.get(j).getLastColumn() == 0 && mergedRegions.get(j).getLastRow() == startRowNewIndicator - 1) {
                            sheet.removeMergedRegion(j);
                        }
                    }
                }
                // Add new rows and copy the indicator template
                sheet.shiftRows(startRowNewIndicator, sheet.getLastRowNum(), 4);
                sheet.copyRows(startRowNewIndicator - 4, startRowNewIndicator, startRowNewIndicator, new CellCopyPolicy());

                // Clear cell for future merge
                sheet.getRow(startRowNewIndicator).getCell(0).setCellValue("");

                // Set values
                sheet.getRow(startRowNewIndicator + 1).getCell(2).setCellValue(indicatorList.get(i).getName());
                sheet.getRow(startRowNewIndicator + 3).getCell(3).setCellValue(indicatorList.get(i).getSourceVerification());
                startRowNewIndicator += 4;
            }


            // Merge first column
            if(numberTemplateIndicators.equals(OUTPUT_NUM_TEMP_INDIC)) {
                sheet.addMergedRegion(new CellRangeAddress(initialRow + numberTemplateIndicators * 4 - 1, startRowNewIndicator - 1, 0, 0));
            } else {
                sheet.addMergedRegion(new CellRangeAddress(initialRow + numberTemplateIndicators * 3, startRowNewIndicator - 1, 0, 0));
            }
            return startRowNewIndicator;
        }

        // Number of the row of the next level's template
        return initialRow + numberTemplateIndicators * 4;
    }
}
