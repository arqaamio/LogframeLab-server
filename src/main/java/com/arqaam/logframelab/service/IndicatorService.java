package com.arqaam.logframelab.service;

import com.arqaam.logframelab.controller.dto.FiltersDto;
import com.arqaam.logframelab.controller.dto.IndicatorsRequestDto.FilterRequestDto;
import com.arqaam.logframelab.exception.*;
import com.arqaam.logframelab.model.IndicatorResponse;
import com.arqaam.logframelab.model.MLScanIndicatorResponse.MLScanIndicator;
import com.arqaam.logframelab.model.NumIndicatorsSectorLevel;
import com.arqaam.logframelab.model.StatementResponse;
import com.arqaam.logframelab.model.persistence.*;
import com.arqaam.logframelab.model.projection.CounterSectorLevel;
import com.arqaam.logframelab.model.projection.IndicatorFilters;
import com.arqaam.logframelab.repository.*;
import com.arqaam.logframelab.util.Constants;
import com.arqaam.logframelab.util.DocManipulationUtil;
import com.arqaam.logframelab.util.Logging;
import com.arqaam.logframelab.util.Utils;
import org.apache.logging.log4j.util.Strings;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.util.StringUtil;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.*;
import org.apache.xmlbeans.XmlCursor;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCell;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STBorder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.print.Doc;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;

@Service
public class IndicatorService implements Logging {

  private static final int DEFAULT_FONT_SIZE = 10;
  /** Indicates the number of indicators template the level has by default */
  protected static final Integer IMPACT_NUM_TEMP_INDIC = 1,
      OUTCOME_NUM_TEMP_INDIC = 3,
      OUTPUT_NUM_TEMP_INDIC = 2;
  private static final Integer TOTAL_PERCENTAGE = 100;
  private static final Integer TOTAL_PERCENTAGE_OF_SCANNING = 70;
  private static final Integer TOTAL_PERCENTAGE_OF_SMALL_TASKS = 5;
  private final IndicatorRepository indicatorRepository;

  private final LevelRepository levelRepository;
  private final SourceRepository sourceRepository;
  private final SDGCodeRepository sdgCodeRepository;
  private final CRSCodeRepository crsCodeRepository;
  private final MachineLearningService machineLearningService;

  private final Utils utils;

  public IndicatorService(IndicatorRepository indicatorRepository, LevelRepository levelRepository,
                          SourceRepository sourceRepository, SDGCodeRepository sdgCodeRepository,
                          CRSCodeRepository crsCodeRepository, MachineLearningService machineLearningService, Utils utils) {
    this.indicatorRepository = indicatorRepository;
    this.levelRepository = levelRepository;
    this.sourceRepository = sourceRepository;
    this.sdgCodeRepository = sdgCodeRepository;
    this.crsCodeRepository = crsCodeRepository;
      this.machineLearningService = machineLearningService;
      this.utils = utils;
  }

  /**
   * Extract Indicators from a Word file
   *
   * @param file Word file
   * @param filter <code>FilterDto</code>
   * @return List of Indicators
   */
/*  public List<IndicatorResponse> extractIndicatorsFromWordFile( MultipartFile file, FiltersDto filter) {
    Integer progress = TOTAL_PERCENTAGE_OF_SMALL_TASKS;
    List<IndicatorResponse> result = new ArrayList<>();
    List<Indicator> indicatorsList;
    utils.sendProgressMessage(progress);
    if (filter != null && !filter.isEmpty()) {
      indicatorsList = indicatorsFromFilter(filter);
    } else {
      indicatorsList = indicatorRepository.findAll();
    }
    utils.sendProgressMessage(progress+=TOTAL_PERCENTAGE_OF_SMALL_TASKS);
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
      utils.sendProgressMessage(progress+=TOTAL_PERCENTAGE_OF_SMALL_TASKS);

      try {
        Map<Long, Indicator> mapResult;
          if(file.getOriginalFilename().matches(".+\\.docx$")) {
            logger().info("Searching indicators in .docx file. maxIndicatorLength: {}", maxIndicatorLength);
            XWPFDocument doc = new XWPFDocument(file.getInputStream());
            mapResult = searchForIndicatorsInText(new XWPFWordExtractor(doc).getText(), maxIndicatorLength, progress, indicatorsList);
            doc.close();
        } else {
          // Read .doc
          logger().info("Searching indicators in .doc file. maxIndicatorLength: {}", maxIndicatorLength);
          HWPFDocument doc = new HWPFDocument(file.getInputStream());
          mapResult = searchForIndicatorsInText(new WordExtractor(doc).getText(), maxIndicatorLength, progress, indicatorsList);
          doc.close();
        }
        if(!mapResult.isEmpty()) {
          List<Level> levelsList = levelRepository.findAllByOrderByPriority();
          logger().info("Starting the sort of the indicators");
          // Sort by Level and then by number of times a keyword was tricked
          result = mapResult.values().stream().sorted((o1, o2) -> {
            if (o1.getLevel().getId().equals(o2.getLevel().getId())){
              return o1.getNumTimes() > o2.getNumTimes() ? -1 :
                  (o1.getNumTimes().equals(o2.getNumTimes()) ? 0 : 1);
            }
            for (Level level : levelsList) {
              if(level.getPriority().equals(o1.getLevel().getPriority())) return -1;
              if(level.getPriority().equals(o2.getLevel().getPriority())) return 1;
            }
            return 1;
          }).map(this::convertIndicatorToIndicatorResponse).collect(Collectors.toList());
        }
      } catch (IOException e) {
        logger().error("Failed to open word file. Name of the file: {}", file.getName(), e);
        throw new FailedToOpenFileException();
      } catch (Exception e){
        logger().error("Failed to load/process the word file. Name of the file: {}", file.getName(), e);
        throw new WordFileLoadFailedException();
      }
    }
    logger().info("Successfuly scanned the file for "+ result.size() +" indicators");
    utils.sendProgressMessage(TOTAL_PERCENTAGE);
    return result;
  }*/

    /**
     * Search for indicators keywords in the given text and return the found indicators
     * @param text Text to be searched
     * @param maxIndicatorLength Max number of words to be searched at the same time (Its the size of the biggest keyword)
     * @param progress Progress value to be sent through the web socket
     * @param indicatorsList List of indicators to be searched
     * @return Map of the with the indicator id as key and indicator as value
     */
  /*protected Map<Long, Indicator> searchForIndicatorsInText(String text, Integer maxIndicatorLength, Integer progress, List<Indicator> indicatorsList){
      utils.sendProgressMessage(progress+=TOTAL_PERCENTAGE_OF_SMALL_TASKS);
      Map<Long, Indicator> mapResult = new HashMap<>();
      List<String> wordsToScan = new ArrayList<>();
      Matcher matcher = Pattern.compile("\\w+").matcher(text);
      int totalMatches = 0, i = 0;

      //TODO: Change to matcher function once java upgraded to 11
      while(matcher.find()){
          totalMatches++;
      }
      matcher.reset();

      double fraction = (double)TOTAL_PERCENTAGE_OF_SCANNING/(double)totalMatches;
      // Inverted of the fraction is the number of matches that it takes to reach the 1% of the TOTAL_PERCENTAGE_OF_SCANNING
      int countUntilSend = fraction > 1 ? (int) fraction : (int) (1/fraction);
      logger().debug("Total number of matches: {}, fraction: {}, countUntilSend: {}", totalMatches, fraction, countUntilSend);
      while(matcher.find()){
          i++;
          wordsToScan.add(matcher.group());
          if (wordsToScan.size() == maxIndicatorLength) {
              // Count until the inverted of the fraction
              if(i==countUntilSend){
                  // Send progress value through the web socket(it always increase only by 1%)
                  utils.sendProgressMessage(progress++);
                  // Restart the counter to reach the fraction value
                  i = 0;
              }
              checkIndicators(wordsToScan, indicatorsList, mapResult);
              wordsToScan = new ArrayList<>();
          }
      }
      return mapResult;
  }*/
  /**
     * Fills a list of indicators that contain certain words
     * @param wordsToScan Words to find in the indicators' keyword list
     * @param indicators Indicators to be analyzed
     * @param mapResult Map Indicators' Id and IndicatorResponses
     */
    /*protected void checkIndicators(List<String> wordsToScan, List<Indicator> indicators,
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
    }*/

    /**
     * Export Indicators in a word template (.docx) file
     * @param indicatorResponses List of indicator responses to fill the template
     * @return Word template filled with the indicators
     */
    public ByteArrayOutputStream exportIndicatorsInWordFile(List<IndicatorResponse> indicatorResponses,
                                                            List<StatementResponse> statements , List<StatementResponse> assumptionStatements) {
        try {
            logger().info("Starting to export the indicators to the word template. IndicatorResponses: {}", indicatorResponses);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            List<Level> levels = levelRepository.findAllByOrderByPriority();
            List<Indicator> indicatorList = indicatorRepository.findAllById(indicatorResponses.stream()
                    .mapToLong(IndicatorResponse::getId).boxed().collect(Collectors.toList()));

            List<Indicator> impactIndicators = new ArrayList<>();
            List<Indicator> outcomeIndicators = new ArrayList<>();
            List<Indicator> outputIndicators = new ArrayList<>();
            for (Indicator indicator : indicatorList) {
                indicatorResponses.stream().filter(x -> x.getId() == indicator.getId()).findFirst().ifPresent(ind -> {
                    if (!StringUtils.isEmpty(ind.getValue()))
                        indicator.setValue(ind.getValue());
                    if (!StringUtils.isEmpty(ind.getDate()))
                        indicator.setDate(ind.getDate());
                    indicator.setStatement(ind.getStatement());
                });

                // Can't do switch because the values aren't known before runtime
                if (levels.get(0).equals(indicator.getLevel())) {
                    impactIndicators.add(indicator);
                } else if (levels.get(1).equals(indicator.getLevel())) {
                    outcomeIndicators.add(indicator);
                } else {
                    outputIndicators.add(indicator);
                }
            }
            List<String> impactStatements = new ArrayList<>();
            List<String> outcomeStatements = new ArrayList<>();
            List<String> outputStatements = new ArrayList<>();
            for(StatementResponse statement: statements) {
                if (levels.get(0).getName().equalsIgnoreCase(statement.getLevel())) {
                    impactStatements.add(statement.getStatement());
                } else if (levels.get(1).getName().equalsIgnoreCase(statement.getLevel())) {
                    outcomeStatements.add(statement.getStatement());
                } else {
                    outputStatements.add(statement.getStatement());
                }
            }
            //create an assumption map key : statement , value : assumption
            Map<String, String> outputAssumption = assumptionStatements.stream().filter(statementResponse ->
                    statementResponse.getLevel().equalsIgnoreCase(Constants.OUTPUT_LEVEL)
            ).collect(HashMap::new, (m,v)->m.put(v.getStatement(), v.getAssumption()), HashMap::putAll);

            Map<String, String> outcomeAssumption = assumptionStatements.stream().filter(statementResponse ->
                    statementResponse.getLevel().equalsIgnoreCase(Constants.OUTCOME_LEVEL)
            ).collect(HashMap::new, (m,v)->m.put(v.getStatement(), v.getAssumption()), HashMap::putAll);

            XWPFDocument document = new XWPFDocument(new ClassPathResource(Constants.WORD_FORMAT+ "_Template" + Constants.WORD_FILE_EXTENSION).getInputStream());
            XWPFTable table = document.getTableArray(0);
            Integer rowIndex = 1;
            rowIndex = fillWordTableByLevel(impactIndicators.stream().sorted(Comparator.comparing(Indicator::getStatement,
                    Comparator.nullsLast(Comparator.naturalOrder()))).collect(Collectors.toList()),
                    table, rowIndex, impactStatements, new HashMap<>());
            rowIndex = fillWordTableByLevel(outcomeIndicators.stream().sorted(Comparator.comparing(Indicator::getStatement,
                    Comparator.nullsLast(Comparator.naturalOrder()))).collect(Collectors.toList()),
                    table, rowIndex, outcomeStatements, outcomeAssumption);
            fillWordTableByLevel(outputIndicators.stream().sorted(Comparator.comparing(Indicator::getStatement,
                    Comparator.nullsLast(Comparator.naturalOrder()))).collect(Collectors.toList()),
                    table, rowIndex, outputStatements, outputAssumption);
            document.write(outputStream);
            document.close();
            indicatorRepository.saveAll(indicatorList.stream().peek(x-> x.setTimesDownloaded(x.getTimesDownloaded()+1)).collect(Collectors.toList()));
            return outputStream;
        } catch (IOException e) {
            logger().error("Template was not Found", e);
            throw new TemplateNotFoundException();
        }
    }

    /**
     * Fill the template's level with the indicators
     * @param indicatorList Indicators with which the table will filled
     * @param table         Table to filled
     * @param rowIndex      Index of the first row to be filled/where the level starts
     * @return The index of next row after the level's template
     */
    private Integer fillWordTableByLevel(List<Indicator> indicatorList, XWPFTable table, Integer rowIndex, List<String> statements, Map<String, String> assumption){
        boolean filledTemplateIndicators = false;
        Integer initialRow = rowIndex;
        logger().info("Starting to fill the table with the indicators information. RowIndex: {}", rowIndex);
        if(indicatorList.size() > 0) {
            String currentStatement = "";
            Integer statementRow = rowIndex;
            for (Indicator indicator : indicatorList) {
                // First fill the template then add new rows
                if (filledTemplateIndicators) DocManipulationUtil.insertTableRow(table, rowIndex);
                else filledTemplateIndicators = true;

                if(!Objects.equals(currentStatement, indicator.getStatement())) {
                    DocManipulationUtil.setTextOnCell(table.getRow(rowIndex).getCell(1), indicator.getStatement(), DEFAULT_FONT_SIZE);
                    if (!org.apache.commons.lang3.StringUtils.isBlank(indicator.getStatement()) && assumption.get(indicator.getStatement()) != null) { // adding assumptions on column 7
                        DocManipulationUtil.setTextOnCell(table.getRow(rowIndex).getCell(7), assumption.get(indicator.getStatement()), DEFAULT_FONT_SIZE);
                    }
                    currentStatement = indicator.getStatement();
                    statementRow = rowIndex;
                } else if (Objects.equals(currentStatement, indicator.getStatement()) ) { //|| rowIndex == initialRow + indicatorList.size() -1
                    DocManipulationUtil.mergeCellsByColumn(table, statementRow, rowIndex, 1);
                    DocManipulationUtil.mergeCellsByColumn(table, statementRow, rowIndex, 7);// merge column 7 based on statement
                }
                // Set values
                DocManipulationUtil.setTextOnCell(table.getRow(rowIndex).getCell(2), indicator.getName(), DEFAULT_FONT_SIZE);
                DocManipulationUtil.setTextOnCell(table.getRow(rowIndex).getCell(6), Optional.ofNullable(indicator.getSourceVerification()).orElse(""), DEFAULT_FONT_SIZE);

                if (!isNull(indicator.getValue()) && !isNull(indicator.getDate())) {
                    if(StringUtils.isEmpty(indicator.getDataSource()))
                        DocManipulationUtil.setTextOnCell(table.getRow(rowIndex).getCell(3), indicator.getValue() + " (" +
                            indicator.getDate() + ")", DEFAULT_FONT_SIZE);
                    else
                        DocManipulationUtil.setHyperLinkOnCell(table.getRow(rowIndex).getCell(3), indicator.getValue() + " (" +
                                indicator.getDate() + ")", indicator.getDataSource(), DEFAULT_FONT_SIZE);
                }
                rowIndex++;
            }
            for(String statement: statements) {
                if (filledTemplateIndicators) DocManipulationUtil.insertTableRow(table, rowIndex);
                else filledTemplateIndicators = true;
                if(!org.apache.commons.lang3.StringUtils.isBlank(statement)  && assumption.get(statement)!=null){// adding assumptions on column 7
                    DocManipulationUtil.setTextOnCell(table.getRow(rowIndex).getCell(7), assumption.get(statement), DEFAULT_FONT_SIZE);
                }
                DocManipulationUtil.setTextOnCell(table.getRow(rowIndex).getCell(1), statement, DEFAULT_FONT_SIZE);
                rowIndex++;
            }
        } else {
            for(String statement: statements) {
                if (filledTemplateIndicators) DocManipulationUtil.insertTableRow(table, rowIndex);
                else filledTemplateIndicators = true;
                if(!org.apache.commons.lang3.StringUtils.isBlank(statement)  && assumption.get(statement)!=null){// adding assumptions on column 7
                    DocManipulationUtil.setTextOnCell(table.getRow(rowIndex).getCell(7), assumption.get(statement), DEFAULT_FONT_SIZE);
                }
                DocManipulationUtil.setTextOnCell(table.getRow(rowIndex).getCell(1), statement, DEFAULT_FONT_SIZE);
                rowIndex++;
            }
            // Add template row
            if(!filledTemplateIndicators) rowIndex++;
        }
        if(initialRow <= rowIndex-1) {
            // Merge column of level and assumption
            DocManipulationUtil.mergeCellsByColumn(table, initialRow, rowIndex - 1, 0);
          //  DocManipulationUtil.mergeCellsByColumn(table, initialRow, rowIndex - 1, 7);
        }
        return rowIndex;
    }

    /**
     * Import Indicators from an worksheet/excel file with the extension xlsx
     * @param file Worksheet file
     */
     public List<Indicator> importIndicators(MultipartFile file) {
       return indicatorRepository.saveAll(extractIndicatorFromFile(file));
    }

    public List<Indicator> extractIndicatorFromFile(MultipartFile file) {
      List<Level> levels = levelRepository.findAll();
      List<Source> sources = sourceRepository.findAll();
      List<SDGCode> sdgCodes = sdgCodeRepository.findAll();
      List<CRSCode> crsCodes = crsCodeRepository.findAll();

      Map<String, Level> levelMap = new HashMap<>();
      for (Level lvl : levels){
        levelMap.put(lvl.getName(), lvl);
      }

      logger().info("Importing indicators from xlsx, name {}", file.getOriginalFilename());
      try {
        List<Indicator> indicatorList = new ArrayList<>();
        Iterator<Row> iterator = new XSSFWorkbook(file.getInputStream()).getSheetAt(0).iterator();
        int row = 0;
        // skip the headers row
        if (iterator.hasNext()) {
          iterator.next();
          row++;
        }
        while (iterator.hasNext()) {
          Row currentRow = iterator.next();
          row++;
          try {
             currentRow.getCell(3).getStringCellValue();
          } catch (NullPointerException ex) {
            continue;
          }
          // key words
          String[] keys = currentRow.getCell(2, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue().toLowerCase().split(",");
          for (int i = 0; i < keys.length; i++) {
            keys[i] = keys[i].trim().replaceAll("\\s+", " ");
          }
          Level level = levelMap.get(cleanText(currentRow.getCell(0, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue()).toUpperCase());
          if (!isNull(level)) {
            Cell crsCodeCell = currentRow.getCell(7, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
            Cell sdgCodeCell = currentRow.getCell(8, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
            Indicator indicator = Indicator.builder()
                .level(level)
                .sector(cleanText(currentRow.getCell(1, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue()))
                .keywords(cleanText(String.join(",", keys).replaceAll("\\s+","")))
                .name(cleanText(currentRow.getCell(3, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue()))
                .description(cleanText(currentRow.getCell(4, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue()))
                .disaggregation(cleanText(currentRow.getCell(6, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue()).equalsIgnoreCase("yes"))
                .sourceVerification(cleanText(currentRow.getCell(9, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue()))
                .dataSource(cleanText(currentRow.getCell(10, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue()))
                .build();
            int finalRow = row;
            if(!currentRow.getCell(5, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue().isEmpty()){
                 indicator.setSource(Arrays.stream(cleanText(currentRow.getCell(5, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue()).split(",")).map(
                        x-> sources.stream().filter(y->y.getName().equalsIgnoreCase(x.trim())).findFirst()
                        .orElseThrow(() -> new WorksheetInWrongFormatException("Source on row " + finalRow + " is invalid"))).collect(Collectors.toSet()));

            }
            String crsCodeContent = cleanText(crsCodeCell.getCellType().equals(CellType.NUMERIC) ? String.valueOf((int)crsCodeCell.getNumericCellValue()) : crsCodeCell.getStringCellValue());
            if(!crsCodeContent.isEmpty()){
                  indicator.setCrsCode(Arrays.stream(crsCodeContent.split(",")).map(x-> crsCodes.stream()
                        .filter(y->String.valueOf(y.getId()).equalsIgnoreCase(x.trim())).findFirst()
                        .orElseThrow(() -> new WorksheetInWrongFormatException("CRS Code on row " + finalRow + " is invalid"))).collect(Collectors.toSet()));

            }
            String sdgCodeContent = cleanText(sdgCodeCell.getCellType().equals(CellType.NUMERIC) ? String.valueOf((int)sdgCodeCell.getNumericCellValue()) : sdgCodeCell.getStringCellValue());
            if(!sdgCodeContent.isEmpty()){
                    indicator.setSdgCode(Arrays.stream(sdgCodeContent.split(",")).map(x-> sdgCodes.stream()
                        .filter(y->String.valueOf(y.getId()).equalsIgnoreCase(x.trim())).findFirst()
                        .orElseThrow(() -> new WorksheetInWrongFormatException("SDG Code on row " + finalRow + " is invalid"))).collect(Collectors.toSet()));
            }
            indicatorList.add(indicator);
            /*
             * Logging per row with a large-enough file caused an error in tests:
             * https://stackoverflow.com/a/52033799/2211446
             */
          } else {
              throw new WorksheetInWrongFormatException("Level on row " + row + " is invalid. Valid values: " + levelMap.keySet());
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
     * Removes special characters and extra empty space
     * @param text Text to be cleaned
     * @return Cleaned text
     */
    private String cleanText(String text) {
        // strips off all non-ASCII characters
        text = text.replaceAll("[^\\x00-\\x7F]", "");

        // erases all the ASCII control characters
        text = text.replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "");

        // removes non-printable characters from Unicode
        text = text.replaceAll("\\p{C}", "");

        text = text.replaceAll("[^ -~]","");

        text = text.replaceAll("[^\\p{ASCII}]", "");

        text = text.replaceAll("\\\\x\\p{XDigit}{2}", "");
        text = text.replaceAll("\\\\n","");


        text = text.replaceAll("[^\\x20-\\x7e]", "");
        return text.trim();
    }
    /**
     * Import Indicators from an worksheet/excel file with the extension xlsx
     * @param indicatorResponses Indicators to written in the excel file
     * @return Worksheet in xlsx format
     */
    public ByteArrayOutputStream exportIndicatorsInWorksheet(List<IndicatorResponse> indicatorResponses,
                                                             List<StatementResponse> statements,List<StatementResponse> assumptionStatement) {

        List<Indicator> indicatorList = indicatorRepository.findAllById(indicatorResponses.stream()
                                                           .map(IndicatorResponse::getId)
                                                           .collect(Collectors.toList()));

        logger().info("Write indicators into a worksheet, indicator {}", indicatorResponses);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        //create an assumption map key : statement , value : assumption
        Map<String, String> assumptionMap = assumptionStatement.stream().collect(HashMap::new, (m,v)->m.put(v.getStatement(), v.getAssumption()), HashMap::putAll);
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet();
        String[] columns = new String[]{"Level", "Sector", "Name", "Description", "Source", "Disaggregation", "DAC 5/CRS",
            "SDG", "Source of Verification", "Data Source", "Baseline Value", "Baseline Date", "Statement" ,"Assumption"};

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
        IndicatorResponse response;
        for (Indicator indicator : indicatorList) {
            response = indicatorResponses.stream().filter(x -> x.getId() == indicator.getId()).findFirst().get();
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(indicator.getLevel().getName());
            row.createCell(1).setCellValue(indicator.getSector());
            row.createCell(2).setCellValue(indicator.getName());
            row.createCell(3).setCellValue(indicator.getDescription());
            row.createCell(4).setCellValue(indicator.getSource().stream().map(Source::getName).collect(Collectors.joining()));
            addCellWithStyle(row, 5, isNull(indicator.getDisaggregation())? "" : (indicator.getDisaggregation() ? "Yes" : "No"), yellowCellStyle);
            addCellWithStyle(row, 6, indicator.getCrsCode().stream().map(x-> String.valueOf(x.getId())).collect(Collectors.joining(",")), redCellStyle);
            addCellWithStyle(row, 7, indicator.getSdgCode().stream().map(x-> String.valueOf(x.getId())).collect(Collectors.joining(",")), redCellStyle);
            addCellWithStyle(row, 8, indicator.getSourceVerification(), yellowCellStyle);
            row.createCell(9).setCellValue(indicator.getDataSource());
            row.createCell(10).setCellValue(response.getValue());
            row.createCell(11).setCellValue(response.getDate());
            row.createCell(12).setCellValue(response.getStatement());
            row.createCell(13).setCellValue(Optional.ofNullable(assumptionMap.get(response.getStatement())).orElse(""));
        }

        for (StatementResponse statement : statements) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(statement.getLevel().toUpperCase());
            row.createCell(12).setCellValue(statement.getStatement());
            row.createCell(13).setCellValue(Optional.ofNullable(assumptionMap.get(statement.getStatement())).orElse(""));
        }

        // Resize all columns to fit the content size
        for(int i = 0; i < columns.length; i++) {
            sheet.autoSizeColumn(i);
        }

        try {
            workbook.write(outputStream);
            workbook.close();
            indicatorRepository.saveAll(indicatorList.stream().peek(x-> x.setTimesDownloaded(x.getTimesDownloaded()+1)).collect(Collectors.toList()));
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
    public List<String> getSectors() {
        return indicatorRepository.getSectors();
    }

    /**
     * Returns filters
     * @param getAll True if retrieving filters independently if connected with indicators
     * @return Filters
     */
    public FiltersDto getFilters(Boolean getAll) {
        List<String> sectors = null;
        List<Source> sources = null;
        List<Level> levels = null;
        List<SDGCode> sdgCodes = null;
        List<CRSCode> crsCodes = null;
        FiltersDto filters = new FiltersDto();
        if(getAll) {
            sectors = indicatorRepository.getSectors();
            sources = sourceRepository.findAll();
            levels = levelRepository.findAll();
            sdgCodes = sdgCodeRepository.findAll();
            crsCodes = crsCodeRepository.findAll();
        } else {
            List<IndicatorFilters> filtersResult = indicatorRepository.getAllBy();
            sectors = filtersResult.stream().map(IndicatorFilters::getSector).collect(Collectors.toList());
            sources = filtersResult.stream().map(IndicatorFilters::getSource).filter(f -> !f.isEmpty()).flatMap(Collection::stream).collect(Collectors.toList());
            levels = filtersResult.stream().map(IndicatorFilters::getLevel).collect(Collectors.toList());
            sdgCodes = filtersResult.stream().map(IndicatorFilters::getSdgCode).filter(f -> !f.isEmpty()).flatMap(Collection::stream).collect(Collectors.toList());
            crsCodes = filtersResult.stream().map(IndicatorFilters::getCrsCode).filter(f -> !f.isEmpty()).flatMap(Collection::stream).collect(Collectors.toList());
        }
        filters.getSector().addAll(sectors.stream().filter(f -> !f.isEmpty()).sorted().collect(Collectors.toList()));
        filters.getSource().addAll(sources.stream().sorted(Comparator.comparing(Source::getName)).collect(Collectors.toCollection(LinkedHashSet::new)));
        filters.getLevel().addAll(levels.stream().sorted().collect(Collectors.toList()));
        filters.getSdgCode().addAll(sdgCodes.stream().sorted(Comparator.comparing(SDGCode::getName)).collect(Collectors.toCollection(LinkedHashSet::new)));
        filters.getCrsCode().addAll(crsCodes.stream().sorted(Comparator.comparing(CRSCode::getName)).collect(Collectors.toCollection(LinkedHashSet::new)));
        return filters;
    }

    /**
     * Returns indicators that match the filters
     * @param sector List of Sectors
     * @param sources List of Sources
     * @param levels List of Levels id
     * @param sdgCodes List of SDG codes
     * @param crsCodes List of CRS Codes
     * @return List of IndicatorResponse
     */
    public List<Indicator> getIndicators(Optional<List<String>> sector, Optional<List<Long>> sources, Optional<List<Long>> levels,
                                                 Optional<List<Long>> sdgCodes, Optional<List<Long>> crsCodes, String indicatorName) {
        logger().info("Starting repository call with with sector: {}, sources: {}, levels: {}, sdgCodes: {}, crsCodes: {}, indicatorName: {}",
                sector, sources, levels, sdgCodes, crsCodes, indicatorName);
        return sector.isEmpty() && sources.isEmpty() && levels.isEmpty() && sdgCodes.isEmpty() && crsCodes.isEmpty()
                && (indicatorName == null || indicatorName.isEmpty())?
            indicatorRepository.findAll() :
            indicatorRepository.findAll(
                getIndicatorSpecification(sector, sources, levels, sdgCodes, crsCodes, indicatorName,false));
    }

   Specification<Indicator> getIndicatorSpecification(Optional<List<String>> sector,
      Optional<List<Long>> sources, Optional<List<Long>> levels, Optional<List<Long>> sdgCodes,
      Optional<List<Long>> crsCodes, String indicatorName, boolean temp) {
    return (root, criteriaQuery, criteriaBuilder) -> {
        List<Predicate> predicates = new ArrayList<>();

        if(sector.isPresent()){
            Predicate predicate = criteriaBuilder.and(criteriaBuilder.in(root.get("sector")).value(sector.get()));
            if(sector.get().stream().anyMatch(x->x.equalsIgnoreCase(Constants.EMPTY_VALUE)))
                predicates.add(criteriaBuilder.or(predicate, criteriaBuilder.like(root.get("sector"), "")));
            else
                predicates.add(predicate);
        }

        if(sources.isPresent()){
            Predicate predicate = criteriaBuilder.and(criteriaBuilder.in(root.join("source", JoinType.LEFT).get("id")).value(sources.get()));
            if(sources.get().stream().anyMatch(x-> x.equals(Constants.EMPTY_VALUE_ID)))
                predicates.add(criteriaBuilder.or(predicate, criteriaBuilder.isEmpty(root.get("source"))));
            else
                predicates.add(predicate);
        }
        if(levels.isPresent()){
            Predicate predicate = criteriaBuilder.and(criteriaBuilder.in(root.get("level").get("id")).value(levels.get()));
            if(levels.get().stream().anyMatch(x-> x.equals(Constants.EMPTY_VALUE_ID)))
                predicates.add(criteriaBuilder.or(predicate, criteriaBuilder.isNull(root.get("level"))));
            else
                predicates.add(predicate);
        }

        if(sdgCodes.isPresent()){
            Predicate predicate = criteriaBuilder.and(criteriaBuilder.in(root.join("sdgCode", JoinType.LEFT).get("id")).value(sdgCodes.get()));
            if(sdgCodes.get().stream().anyMatch(x-> x.equals(Constants.EMPTY_VALUE_ID)))
                predicates.add(criteriaBuilder.or(predicate, criteriaBuilder.isEmpty(root.get("sdgCode"))));
            else
                predicates.add(predicate);
        }

        if(crsCodes.isPresent()){
            Predicate predicate = criteriaBuilder.and(criteriaBuilder.in(root.join("crsCode", JoinType.LEFT).get("id")).value(crsCodes.get()));
            if(crsCodes.get().stream().anyMatch(x->x == -1))
                predicates.add(criteriaBuilder.or(predicate, criteriaBuilder.isEmpty(root.get("crsCode"))));
            else
                predicates.add(predicate);
        }
        if(indicatorName != null && !indicatorName.isEmpty())
            predicates.add(criteriaBuilder.and(criteriaBuilder.like(root.get("name"), "%"+indicatorName+"%")));
        predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get("temp"), temp)));

      return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
    };
  }
  public Specification<Indicator> specificationFromFilter(FilterRequestDto filters, boolean temp) {
    if (filters == null) {
      filters = new FilterRequestDto();
    }
    return getIndicatorSpecification(
        filters.getSectors() != null && !filters.getSectors().isEmpty() ? Optional
            .of(new ArrayList<>(filters.getSectors()))
            : Optional.empty(),
        filters.getSourceIds() != null && !filters.getSourceIds().isEmpty() ? Optional
            .of(new ArrayList<>(filters.getSourceIds()))
            : Optional.empty(),
        filters.getLevelIds() != null && !filters.getLevelIds().isEmpty() ? Optional
            .of(new ArrayList<>(filters.getLevelIds()))
            : Optional.empty(),
        filters.getSdgCodeIds() != null && !filters.getSdgCodeIds().isEmpty() ? Optional
            .of(new ArrayList<>(filters.getSdgCodeIds()))
            : Optional.empty(),
        filters.getCrsCodeIds() != null && !filters.getCrsCodeIds().isEmpty() ? Optional
            .of(new ArrayList<>(filters.getCrsCodeIds()))
            : Optional.empty(), filters.getIndicatorName(), temp);
  }

  /**
     * Get indicator with id. If not found throws IndicatorNotFoundException
     * @param id Id of the indicator
     * @return Indicator
     */
    public Indicator getIndicator(Long id){
        logger().info("Searching for indicator with id: {}", id);
        return indicatorRepository.findById(id).orElseThrow(IndicatorNotFoundException::new);
    }

    /**
     * Get indicator with name. If none are found IndicatorNotFoundException
     * @param indicatorName Names of the indicators
     * @return List of Indicators
     */
    public List<Indicator> getIndicatorWithName(List<String> indicatorName){
        logger().info("Searching for indicator with names: {}", indicatorName);
        List<Indicator> indicators = indicatorRepository.findAllByNameIn(indicatorName);
        if(indicators.isEmpty()) {
            logger().info("Failed to find indicators with names");
            throw new IndicatorNotFoundException();
        }
        return indicators;
    }

    /**
     * Get indicator with name. If none are found IndicatorNotFoundException
     * @param indicatorName Names of the indicator
     * @return Indicator with name
     */
    public Indicator getIndicatorByName(String indicatorName){
        logger().info("Searching for indicator with name: {}", indicatorName);
        Optional<Indicator> indicator = indicatorRepository.findTopByName(indicatorName);
        if(indicator.isEmpty()) {
            logger().info("Failed to find indicators with names");
            throw new IndicatorNotFoundException();
        }
        return indicator.get();
    }

    /**
     * Get indicator with ids. If none are found throws IndicatorNotFoundException
     * @param ids Ids of the indicators
     * @return List of Indicators
     */
    public List<Indicator> getIndicatorWithId(List<Long> ids){
        logger().info("Searching for indicator with ids: {}", ids);
        List<Indicator> indicators = indicatorRepository.findAllByIdIn(ids);
        if(indicators.isEmpty()) {
            logger().info("Failed to find indicators with ids: {}", ids);
            throw new IndicatorNotFoundException();
        }
        return indicators;
    }

    /**
     * Get indicators with similarity unchecked or checked
     * @param checked Similarity status
     * @return List of Indicators
     */
    public List<Indicator> getIndicatorsWithSimilarity(Boolean checked){
        logger().info("Searching for indicators with similarity: {}", checked);
        return indicatorRepository.findFirst50BySimilarityCheckEquals(checked);
    }

    /**
     * Updating indicator's similarity check
     * @param id Id of the indicator
     * @param checked Whether to get it checked or unchecked
     * @return Indicator that was updated
     */
    public Indicator updateSimilarityCheck(Long id, Boolean checked){
        logger().info("Updating similarity check with check: {} and id: {}", checked, id);
        Indicator ind = indicatorRepository.findById(id).orElseThrow(IndicatorNotFoundException::new);
        ind.setSimilarityCheck(checked);
        return indicatorRepository.save(ind);
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
                .name(indicator.getName())
                .description(indicator.getDescription())
                .sector(indicator.getSector())
                .disaggregation(indicator.getDisaggregation())
                .crsCode(indicator.getCrsCode())
                .sdgCode(indicator.getSdgCode())
                .source(indicator.getSource())
                .score(indicator.getScore())
                .value(indicator.getValue())
                .date(indicator.getDate())
                .statement(indicator.getStatement())
                .build();
    }

  private List<Indicator> indicatorsFromFilter(FiltersDto filter) {
    return getIndicators(
        filter.getSector().size() > 0
            ? Optional.of(new ArrayList<>(filter.getSector()))
            : Optional.empty(),
        filter.getSource().size() > 0
            ? Optional.of(filter.getSource().stream().map(Source::getId).collect(Collectors.toList()))
            : Optional.empty(),
        filter.getLevel().size() > 0
            ? Optional.of(filter.getLevel().stream().map(Level::getId).collect(Collectors.toList()))
            : Optional.empty(),
        filter.getSdgCode().size() > 0
            ? Optional.of(filter.getSdgCode().stream().map(SDGCode::getId).collect(Collectors.toList()))
            : Optional.empty(),
        filter.getCrsCode().size() > 0
            ? Optional.of(filter.getCrsCode().stream().map(CRSCode::getId).collect(Collectors.toList()))
            : Optional.empty(), null);
  }
    /**
     * Fills the DFID template with the indicators
     * @param indicatorResponse Indicators to fill the indicator file
     * @return The DFID template filled with the indicators
     */
    public ByteArrayOutputStream exportIndicatorsDFIDFormat(List<IndicatorResponse> indicatorResponse,
                                                            List<StatementResponse> statements){
        try {
            logger().info("Start exporting Indicators in DFID format");
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            XSSFWorkbook wk = new XSSFWorkbook(new ClassPathResource(Constants.DFID_FORMAT+"_Template" +Constants.WORKSHEET_FILE_EXTENSION).getInputStream());
            XSSFSheet sheet  = wk.getSheetAt(0);
            List<Level> levels = levelRepository.findAllByOrderByPriority();
            List<Indicator> indicatorList = indicatorRepository.findAllById(indicatorResponse.stream()
                    .mapToLong(IndicatorResponse::getId).boxed().collect(Collectors.toList()));
            List<Indicator> impactIndicators = new ArrayList<>();
            List<Indicator> outcomeIndicators = new ArrayList<>();
            List<Indicator> outputIndicators = new ArrayList<>();

            for (int i = 0; i < indicatorList.size(); i++) {
                Indicator indicator = indicatorList.get(i);
                indicatorResponse.stream().filter(x -> x.getId() == indicator.getId()).findFirst().ifPresent(ind -> {
                    if (!StringUtils.isEmpty(ind.getValue()))
                        indicator.setValue(ind.getValue());
                    if (!StringUtils.isEmpty(ind.getDate()))
                        indicator.setDate(ind.getDate());
                    indicator.setStatement(ind.getStatement());
                });
                // Can't do switch because the values aren't known before runtime
                if (levels.get(0).equals(indicator.getLevel())) {
                    impactIndicators.add(indicator);
                } else if (levels.get(1).equals(indicator.getLevel())) {
                    outcomeIndicators.add(indicator);
                } else {
                    outputIndicators.add(indicator);
                }
            }
            List<String> impactStatements = new ArrayList<>();
            List<String> outcomeStatements = new ArrayList<>();
            List<String> outputStatements = new ArrayList<>();
            for(StatementResponse statement: statements) {
                if (levels.get(0).getName().equalsIgnoreCase(statement.getLevel())) {
                    impactStatements.add(statement.getStatement());
                } else if (levels.get(1).getName().equalsIgnoreCase(statement.getLevel())) {
                    outcomeStatements.add(statement.getStatement());
                } else {
                    outputStatements.add(statement.getStatement());
                }
            }
            logger().info("Impact Indicators: {}\nOutcome Indicators: {}\nOutput Indicators: {}", impactIndicators, outcomeIndicators, outputIndicators);
            int startRowNewIndicator = 1;
            startRowNewIndicator = fillIndicatorsPerLevel(sheet, impactIndicators.stream().sorted(Comparator.comparing(Indicator::getStatement,
                    Comparator.nullsLast(Comparator.naturalOrder()))).collect(Collectors.toList()),
                    startRowNewIndicator, IMPACT_NUM_TEMP_INDIC, impactStatements);
            startRowNewIndicator = fillIndicatorsPerLevel(sheet, outcomeIndicators.stream().sorted(Comparator.comparing(Indicator::getStatement,
                    Comparator.nullsLast(Comparator.naturalOrder()))).collect(Collectors.toList()),
                    startRowNewIndicator, OUTCOME_NUM_TEMP_INDIC, outcomeStatements);
            fillIndicatorsPerLevel(sheet, outputIndicators.stream().sorted(Comparator.comparing(Indicator::getStatement,
                    Comparator.nullsLast(Comparator.naturalOrder()))).collect(Collectors.toList()),
                    startRowNewIndicator, OUTPUT_NUM_TEMP_INDIC, outputStatements);
            wk.write(output);
            wk.close();
            indicatorRepository.saveAll(indicatorList.stream().peek(x-> x.setTimesDownloaded(x.getTimesDownloaded()+1)).collect(Collectors.toList()));
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
                                           Integer numberTemplateIndicators, List<String> statements) {
        Integer initialRow = startRowNewIndicator;
        Integer statementCount = 0;
        List<Indicator> indicators = new ArrayList<>();
        if(indicatorList.size() > 0) {
            Integer count = 0;
            Map<String, List<Indicator>> map = new HashMap<>();
            for (Indicator ind : indicatorList) {
                if (map.containsKey(ind.getStatement())) {
                    map.get(ind.getStatement()).add(ind);
                } else {
                    List<Indicator> indicators1 = new ArrayList<>();
                    indicators1.add(ind);
                    map.put(ind.getStatement(), indicators1);
                }
            }
            indicators = map.values().iterator().next();
            if (map.entrySet().size() == 1 && indicators.size() <= numberTemplateIndicators) {
                statementCount++;
                for (Indicator indicator : indicators) {
                    count++;
                    fillDFIDIndicatorRows(sheet, indicator, startRowNewIndicator, count,1, count==1);
                    startRowNewIndicator += 4;
                }
                if(statements.isEmpty()){
                    return initialRow + 4*numberTemplateIndicators;
                }
            } else {
                // Remove merge
                List<CellRangeAddress> mergedRegions = sheet.getMergedRegions();
                for (int j = 0; j < mergedRegions.size(); j++) {
                    // Check every first row of template indicators including the last row of the previous template
                    if (mergedRegions.get(j).getLastColumn() == 0 && mergedRegions.get(j).getLastRow() == startRowNewIndicator+3) {
                        sheet.removeMergedRegion(j);
                    }
                }
                Iterator<Map.Entry<String, List<Indicator>>> iterator = map.entrySet().iterator();
                // For all statements
                while(iterator.hasNext()) {
                    indicators = iterator.next().getValue();
                    Integer statementRow = startRowNewIndicator;
                    statementCount = statementCount + (Strings.isNotEmpty(indicators.get(0).getStatement()) ? 1 : 0);

                    // Has more statements
                    if(iterator.hasNext()){
                        // Add rows
                        for (int i = 0; i < indicators.size(); i++) {
                            //add rows
                            sheet.shiftRows(startRowNewIndicator + 4, sheet.getLastRowNum(), 4);
                            sheet.copyRows(startRowNewIndicator, startRowNewIndicator + 4, startRowNewIndicator+4, new CellCopyPolicy());

                            fillDFIDIndicatorRows(sheet, indicators.get(i), startRowNewIndicator, i+1, statementCount, true);
                            startRowNewIndicator+=4;
                        }
                        sheet.addMergedRegion(new CellRangeAddress(statementRow+1, startRowNewIndicator-1, 0, 0));
                    // Last statement or no statement
                    }else {
                        // fill existing template
                        if(indicators.size()<=numberTemplateIndicators){
                            // Fill rows
                            for (int i = 0; i < indicators.size(); i++) {
                                fillDFIDIndicatorRows(sheet, indicators.get(i), startRowNewIndicator, i+1, statementCount, i==0);
                                startRowNewIndicator+=4;
                            }
                            sheet.addMergedRegion(new CellRangeAddress(statementRow+1, statementRow+3, 0, 0));
                            startRowNewIndicator+=(numberTemplateIndicators - indicators.size())*4;
                            // Add rows and fill
                        }else {
                            Integer startRow = startRowNewIndicator;
                            for (int i = 0; i < indicators.size(); i++) {
                                Boolean createNewRows = indicators.size()- (i +1) >= numberTemplateIndicators;
                                // Add new rows
                                if(createNewRows) {
                                    sheet.shiftRows(startRowNewIndicator + 4, sheet.getLastRowNum(), 4);
                                    sheet.copyRows(startRowNewIndicator, startRowNewIndicator + 4, startRowNewIndicator + 4, new CellCopyPolicy());
                                }
                                // Fill new rows and new ones
                                fillDFIDIndicatorRows(sheet, indicators.get(i), startRowNewIndicator, i+1, statementCount, createNewRows);
                                startRowNewIndicator+=4;
                            }
                            // Merge added rows
                            if(numberTemplateIndicators.equals(OUTPUT_NUM_TEMP_INDIC)){
                                sheet.addMergedRegion(new CellRangeAddress(startRow + 1, startRowNewIndicator - 5, 0, 0));
                            }else {
                                sheet.addMergedRegion(new CellRangeAddress(startRow + 1, startRowNewIndicator - 3*numberTemplateIndicators, 0, 0));
                            }
                        }
                    }
                }
            }

        } else {
            startRowNewIndicator = initialRow + 4*numberTemplateIndicators;
        }

        if(!statements.isEmpty() &&  (indicatorList.stream().allMatch(x->StringUtils.isEmpty(x.getStatement()))
                || (indicatorList.size() >0 && indicatorList.stream().allMatch(
                        x->x.getStatement() != null && x.getStatement().equals(indicatorList.get(0).getStatement()))))
                && indicatorList.size() < numberTemplateIndicators){
            startRowNewIndicator = indicatorList.size() >0 ? initialRow + 4*numberTemplateIndicators : initialRow;
        }

        Integer i = 0;
        for(String statement : statements){
            statementCount++;
            if(indicatorList.size() > 0 || (indicatorList.isEmpty() && i!=0)) {
                // Add new rows
                sheet.shiftRows(startRowNewIndicator, sheet.getLastRowNum(), 4 * numberTemplateIndicators);
                sheet.copyRows(startRowNewIndicator - 4 * numberTemplateIndicators, startRowNewIndicator, startRowNewIndicator, new CellCopyPolicy());
            }
            String[] title = sheet.getRow(startRowNewIndicator).getCell(1).getStringCellValue().split(" ");
            // Clean old values
            for (int j = 0; j < numberTemplateIndicators; j++) {
                sheet.getRow(startRowNewIndicator + j * 4).getCell(1).setCellValue(title[0] + " " + title[1]);
                sheet.getRow(startRowNewIndicator + 1 + j * 4).getCell(0).setCellValue("");
                sheet.getRow(startRowNewIndicator + 1 + j * 4).getCell(1).setCellValue("");
                sheet.getRow(startRowNewIndicator + 1 + j * 4).getCell(2).setCellValue("");
                sheet.getRow(startRowNewIndicator + 3 + j * 4).getCell(2).setCellValue("");
            }
            sheet.getRow(startRowNewIndicator + 1).getCell(0).setCellValue(statement);
            sheet.getRow(startRowNewIndicator).getCell(0).setCellValue(title[0].toUpperCase() + " " + statementCount);

            startRowNewIndicator+=4*numberTemplateIndicators;
            i++;
        }
        return startRowNewIndicator;
    }

    private void fillDFIDIndicatorRows(XSSFSheet sheet, Indicator indicator, Integer startRowNewIndicator, Integer indicatorNumber,
                                        Integer statementCount, Boolean fillNumberStatement) {

        if(fillNumberStatement && Strings.isNotEmpty(indicator.getStatement())) {
            sheet.getRow(startRowNewIndicator).getCell(0).setCellValue(indicator.getLevel().getName().toUpperCase() + " " + statementCount);
            sheet.getRow(startRowNewIndicator+1).getCell(0).setCellValue(indicator.getStatement());
        }
        sheet.getRow(startRowNewIndicator).getCell(1).setCellValue(sheet.getRow(startRowNewIndicator).getCell(1)
                .getStringCellValue() + " " + (Strings.isEmpty(indicator.getStatement()) ? "" : statementCount + ".") + indicatorNumber);
        sheet.getRow(startRowNewIndicator + 1).getCell(1).setCellValue(indicator.getName());
        sheet.getRow(startRowNewIndicator + 3).getCell(2).setCellValue(indicator.getSourceVerification());
        if (!StringUtils.isEmpty(indicator.getValue()) && !StringUtils.isEmpty(indicator.getDate()))
            sheet.getRow(startRowNewIndicator + 1).getCell(2).setCellValue(indicator.getValue() +
                    " (" + indicator.getDate() + ")");

    }
//    private Integer fillIndicatorsPerLevel(XSSFSheet sheet, List<Indicator> indicatorList, Integer startRowNewIndicator,
//                                           Integer numberTemplateIndicators, Boolean fillBaseline){
//        Integer initialRow = startRowNewIndicator;
//        Integer numIndicators = indicatorList.size();
//        int count = 0;
//        int statementCount = 0;
//        String lastStatement = "";
//        Integer lastStatementRow = startRowNewIndicator;
//        logger().info("Starting to fill Indicators. Start Row New Indicator {}, Number of template indicators of level: {}",
//                startRowNewIndicator, numberTemplateIndicators);
//        // Fill the available spaces
//        while(count < numIndicators && count < numberTemplateIndicators) {
//
//
//            if(indicatorList.get(count).getStatement() !=null && !indicatorList.get(count).getStatement().isEmpty()
//                    && !lastStatement.equals(indicatorList.get(count).getStatement())) {
//                statementCount++;
//
//                if(count !=0 && numberTemplateIndicators > 1) {
//                    // Add new rows and copy the indicator template
//                    sheet.shiftRows(startRowNewIndicator, sheet.getLastRowNum(), 4);
//                    sheet.copyRows(startRowNewIndicator - 4, startRowNewIndicator, startRowNewIndicator, new CellCopyPolicy());
//                }
////                if (numberTemplateIndicators.equals(OUTPUT_NUM_TEMP_INDIC)) {
////                    sheet.addMergedRegion(new CellRangeAddress(lastStatementRow, startRowNewIndicator, 0, 0));
////                } else {
////                    sheet.addMergedRegion(new CellRangeAddress(lastStatementRow, startRowNewIndicator, 0, 0));
////                }
//                if(lastStatementRow < startRowNewIndicator){
//                    List<CellRangeAddress> mergedRegions = sheet.getMergedRegions();
//                    for (int j = 0; j < mergedRegions.size(); j++) {
//                        // Check every first row of template indicators including the last row of the previous template
//                        if (mergedRegions.get(j).getLastColumn() == 0 && mergedRegions.get(j).getLastRow() == startRowNewIndicator - 1) {
//                            sheet.removeMergedRegion(j);
//                        }
//                    }
//                    sheet.addMergedRegion(new CellRangeAddress(lastStatementRow+1, startRowNewIndicator, 0, 0));
//                }
//                lastStatement = indicatorList.get(count).getStatement();
//                lastStatementRow = startRowNewIndicator;
//                sheet.getRow(startRowNewIndicator).getCell(0).setCellValue(sheet.getRow(startRowNewIndicator).getCell(0)
//                        .getStringCellValue() + " " + statementCount);
//            }
//
//            if(Strings.isNotEmpty(indicatorList.get(count).getStatement()) && count==0) {
//                sheet.getRow(startRowNewIndicator + 1).getCell(0).setCellValue(indicatorList.get(count).getStatement());
//            }
//            sheet.getRow(startRowNewIndicator).getCell(2).setCellValue(sheet.getRow(startRowNewIndicator).getCell(2)
//                    .getStringCellValue() + " " + (Strings.isEmpty(indicatorList.get(count).getStatement()) ? "" :  statementCount + ".") + (count+1));
//            sheet.getRow(startRowNewIndicator + 1).getCell(2).setCellValue(indicatorList.get(count).getName());
//            sheet.getRow(startRowNewIndicator + 3).getCell(3).setCellValue(indicatorList.get(count).getSourceVerification());
//            if(fillBaseline && !isNull(indicatorList.get(count).getValue()) && !isNull(indicatorList.get(count).getDate()))
//                sheet.getRow(startRowNewIndicator + 1).getCell(3).setCellValue(indicatorList.get(count).getValue() +
//                    " (" + indicatorList.get(count).getDate() + ")");
//            startRowNewIndicator += 4;
//            count++;
//        }
//
//        // If needs new rows
//        if (numIndicators > numberTemplateIndicators) {
//            logger().info("Adding new rows to worksheet to insert indicators. IndicatorList Size: {}", indicatorList.size());
//            for (int i = numberTemplateIndicators; i < indicatorList.size(); i++) {
//                if (numberTemplateIndicators == i && numberTemplateIndicators.equals(OUTCOME_NUM_TEMP_INDIC)) {
//                    logger().info("Searching for cells needing unmerging in the first column, numberTemplateIndicators: {}", numberTemplateIndicators);
//                    // Unmerge merged on the first column, so it can be merged later
//                    List<CellRangeAddress> mergedRegions = sheet.getMergedRegions();
//                    for (int j = 0; j < mergedRegions.size(); j++) {
//                        // Check every first row of template indicators including the last row of the previous template
//                        if (mergedRegions.get(j).getLastColumn() == 0 && mergedRegions.get(j).getLastRow() == startRowNewIndicator - 1) {
//                            sheet.removeMergedRegion(j);
//                        }
//                    }
//                }
//                // Add new rows and copy the indicator template
////                sheet.shiftRows(startRowNewIndicator - i*4, sheet.getLastRowNum(), 4);
////                sheet.copyRows(startRowNewIndicator, startRowNewIndicator + 4, startRowNewIndicator - i*4, new CellCopyPolicy());
//                if(numberTemplateIndicators.equals(OUTCOME_NUM_TEMP_INDIC)){
//                    sheet.shiftRows(startRowNewIndicator - 8, sheet.getLastRowNum(), 4);
//                    sheet.copyRows(startRowNewIndicator, startRowNewIndicator + 4, startRowNewIndicator - 8, new CellCopyPolicy());
//                } else {
//                    sheet.shiftRows(startRowNewIndicator - 4, sheet.getLastRowNum(), 4);
//                    sheet.copyRows(startRowNewIndicator, startRowNewIndicator + 4, startRowNewIndicator - 4, new CellCopyPolicy());
//
//                }
////                sheet.shiftRows(startRowNewIndicator - 8, sheet.getLastRowNum(), 4);
////                sheet.copyRows(startRowNewIndicator, startRowNewIndicator + 4, startRowNewIndicator - i*4, new CellCopyPolicy());
//
//
//                // Clear cell for future merge
//                sheet.getRow(startRowNewIndicator).getCell(0).setCellValue("");
//
//
//                // Merge first column
//                if(indicatorList.get(count).getStatement() !=null && !indicatorList.get(count).getStatement().isEmpty()
//                        && !lastStatement.equals(indicatorList.get(count).getStatement())) {
//                    statementCount++;
//
//                    if(count !=0 && numberTemplateIndicators > 1) {
//                        // Add new rows and copy the indicator template
//                        sheet.shiftRows(startRowNewIndicator, sheet.getLastRowNum(), 4);
//                        sheet.copyRows(startRowNewIndicator - 4, startRowNewIndicator, startRowNewIndicator, new CellCopyPolicy());
//                    }
////                if (numberTemplateIndicators.equals(OUTPUT_NUM_TEMP_INDIC)) {
////                    sheet.addMergedRegion(new CellRangeAddress(lastStatementRow, startRowNewIndicator, 0, 0));
////                } else {
////                    sheet.addMergedRegion(new CellRangeAddress(lastStatementRow, startRowNewIndicator, 0, 0));
////                }
//                    if(lastStatementRow < startRowNewIndicator){
//                        List<CellRangeAddress> mergedRegions = sheet.getMergedRegions();
//                        for (int j = 0; j < mergedRegions.size(); j++) {
//                            // Check every first row of template indicators including the last row of the previous template
//                            if (mergedRegions.get(j).getLastColumn() == 0 && mergedRegions.get(j).getLastRow() == startRowNewIndicator - 1) {
//                                sheet.removeMergedRegion(j);
//                            }
//                        }
//                        sheet.addMergedRegion(new CellRangeAddress(lastStatementRow+1, startRowNewIndicator, 0, 0));
//                    }
//                    lastStatement = indicatorList.get(count).getStatement();
//                    lastStatementRow = startRowNewIndicator;
//                    sheet.getRow(startRowNewIndicator).getCell(0).setCellValue(sheet.getRow(startRowNewIndicator).getCell(0)
//                            .getStringCellValue() + " " + statementCount);
//                }
//
//                // Set values
//                if(indicatorList.get(count).getStatement() != null && !indicatorList.get(count).getStatement().isEmpty()) {
//                    sheet.getRow(startRowNewIndicator + 1).getCell(0).setCellValue(indicatorList.get(count).getStatement());
//                    statementCount++;
//                }
//
//                sheet.getRow(startRowNewIndicator).getCell(2).setCellValue(sheet.getRow(startRowNewIndicator).getCell(2)
//                        .getStringCellValue() + " " + (Strings.isEmpty(indicatorList.get(count).getStatement()) ? "" :  statementCount + ".") + (count+1));
//                sheet.getRow(startRowNewIndicator + 1).getCell(2).setCellValue(indicatorList.get(i).getName());
//                sheet.getRow(startRowNewIndicator + 3).getCell(3).setCellValue(indicatorList.get(i).getSourceVerification());
//                if(fillBaseline && !isNull(indicatorList.get(i).getValue()) && !isNull(indicatorList.get(i).getDate()))
//                    sheet.getRow(startRowNewIndicator + 1).getCell(3).setCellValue(indicatorList.get(i).getValue() +
//                            " (" + indicatorList.get(i).getDate() + ")");
//                else
//                    sheet.getRow(startRowNewIndicator+1).getCell(3).setCellValue("");
//
//                startRowNewIndicator += 4;
//            }
//
//            return startRowNewIndicator;
//        }
//
//        // Number of the row of the next level's template
//        return initialRow + numberTemplateIndicators * 4;
//    }

    /**
     * Fills the PRM template with the indicators
     * @param indicatorResponses Indicators to fill the indicator file
     * @return The PRM template filled with the indicators
     */
    public ByteArrayOutputStream exportIndicatorsPRMFormat(List<IndicatorResponse> indicatorResponses, List<StatementResponse>  statements){
        logger().info("Starting to export indicators using the PRM template");
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        List<Level> levels = levelRepository.findAllByOrderByPriority();
        List<Indicator> indicatorList = indicatorRepository.findAllById(indicatorResponses.stream()
                .mapToLong(IndicatorResponse::getId).boxed().collect(Collectors.toList()));

        logger().info("Organizing the indicators per level and add value and date");
        List<Indicator> impactIndicators = new ArrayList<>();
        List<Indicator> outcomeIndicators = new ArrayList<>();
        List<Indicator> outputIndicators = new ArrayList<>();
        for (int i = 0; i < indicatorList.size(); i++) {
            Indicator indicator = indicatorList.get(i);
           indicatorResponses.stream().filter(x-> x.getId() == indicator.getId()).findAny().ifPresent(response -> {
               if(!StringUtils.isEmpty(response.getValue()))
                   indicator.setValue(response.getValue());
               if(!StringUtils.isEmpty(response.getDate()))
                   indicator.setDate(response.getDate());
               if(!StringUtils.isEmpty(response.getStatement()))
                   indicator.setStatement(response.getStatement());
               // Can't do switch because the values aren't known before runtime
               if (levels.get(0).equals(indicator.getLevel())) {
                   impactIndicators.add(indicator);
               } else if (levels.get(1).equals(indicator.getLevel())) {
                   outcomeIndicators.add(indicator);
               } else {
                   outputIndicators.add(indicator);
               }
            });

        }

        try {
            XWPFDocument document = new XWPFDocument(new ClassPathResource(Constants.PRM_FORMAT + "_Template" + Constants.WORD_FILE_EXTENSION).getInputStream());
            XWPFTable impactTable = document.getTableArray(0);
            XWPFTable outcomeTable = document.getTableArray(1);
            XWPFTable outputTable = document.getTableArray(2);

            //group by statement and separate the indicators with null statement
            Map<String, List<Indicator>> dataImpact = impactIndicators.stream().collect(Collectors.groupingBy(
                    m -> m.getStatement() == null ?
                            " " : m.getStatement())
            );
            Map<String, List<Indicator>> dataOutcome = outcomeIndicators.stream().collect(Collectors.groupingBy(
                    m -> m.getStatement() == null ?
                            " " : m.getStatement())
            );
            Map<String, List<Indicator>> dataOutput = outputIndicators.stream().collect(Collectors.groupingBy(
                    m -> m.getStatement() == null ?
                            " " : m.getStatement())
            );
            //filter the statements for which indicators are not present
            List<String> impactStatements = new ArrayList<>();
            List<String> outcomeStatements = new ArrayList<>();
            List<String> outputStatements = new ArrayList<>();
            if(statements.size()>0) {
                for (StatementResponse statement : statements) {
                    if (levels.get(0).getName().equalsIgnoreCase(statement.getLevel())) {
                        impactStatements.add(statement.getStatement());
                    } else if (levels.get(1).getName().equalsIgnoreCase(statement.getLevel())) {
                        outcomeStatements.add(statement.getStatement());
                    } else {
                        outputStatements.add(statement.getStatement());
                    }
                }
            }
            fillIndicatorPerTableDynamic(dataImpact ,impactTable, document ,Constants.IMPACT_LEVEL_PREFIX,impactStatements);
            fillIndicatorPerTableDynamic(dataOutcome ,outcomeTable, document ,Constants.OUTCOME_LEVEL_PREFIX,outcomeStatements);
            fillIndicatorPerTableDynamic(dataOutput ,outputTable, document ,Constants.OUTPUT_LEVEL_PREFIX,outputStatements);

            logger().info("Removing template tables");
            document.removeBodyElement(document.getPosOfTable(impactTable));
            document.removeBodyElement(document.getPosOfTable(outcomeTable));
            document.removeBodyElement(document.getPosOfTable(outputTable));

            logger().info("Writing the changes to the template to a outputStream");
            document.write(outputStream);
            // set download times
            indicatorRepository.saveAll(indicatorList.stream().peek(x-> x.setTimesDownloaded(x.getTimesDownloaded()+1)).collect(Collectors.toList()));
        } catch (IOException e) {
            logger().error("Template was not found.", e);
            throw new FailedToOpenFileException();
        }
        return outputStream;
    }

    /**
     * Fills the table of the level with the indicators
     * @param source Table to be filled
     * @param data Indicators with which the table will be filled
     */

    private void
    fillIndicatorPerTableDynamic( Map<String, List<Indicator>> data ,XWPFTable source, XWPFDocument document , String type,List<String> statementResponses){
        logger().info("Starting to fill the table with the indicators of the the size: {}", data.size());
        Set<String> keys = data.keySet();
        int index =0;

        for (String statement : keys) {
            //copy the table from template and fill it dynamically
            XWPFTable table = document.createTable();
            DocManipulationUtil.copyTable(source, table);
            List<Indicator> indicators = data.get(statement);
            DocManipulationUtil.setTextOnCellWithBoldTitle(table.getRow(0).getCell(0), type + (index + 1), statement, null);
            for (int i = 0; i < indicators.size(); i++) {
                if (table.getRow(i * 2 + 2) == null) {
                    XWPFTableRow row = table.insertNewTableRow(table.getNumberOfRows());
                    XWPFTableRow row2 = table.insertNewTableRow(table.getNumberOfRows());
                    for (int j = 0; j < 5; j++) {
                        row.addNewTableCell();
                        row2.addNewTableCell();
                    }
                    // Merge the Notes row
                    DocManipulationUtil.mergeCellsByRow(table, 0, 4, table.getNumberOfRows() - 1);
                }
                DocManipulationUtil.setTextOnCellWithBoldTitle(table.getRow(i * 2 + 2).getCell(0), "Indicator " + (i + 1) + ":", indicators.get(i).getName(), null);
                if (!StringUtils.isEmpty(indicators.get(i).getValue()) && !StringUtils.isEmpty(indicators.get(i).getDate())) {
                    DocManipulationUtil.setTextOnCell(table.getRow(i * 2 + 2).getCell(1), indicators.get(i).getValue() + " (" + indicators.get(i).getDate() + ")", null);
                }
                DocManipulationUtil.setTextOnCellWithBoldTitle(table.getRow(i * 2 + 3).getCell(0), "NOTES:", indicators.get(i).getSourceVerification(), null);
            }
            index++;
            XWPFParagraph paragraph = document.createParagraph();
            XWPFRun run=paragraph.createRun();
            run.setText(" ");

        }
        int statementIndex =index;
        //Adding tables for non indicator statements
        if(statementResponses.size()>0) {
            for (String statement : statementResponses) {
                //copy the table from template and fill it dynamically
                XWPFTable table = document.createTable();
                DocManipulationUtil.copyTable(source, table);
                DocManipulationUtil.setTextOnCellWithBoldTitle(table.getRow(0).getCell(0), type + (statementIndex + 1), statement, null);
                statementIndex++;
                //creating an empty space between each table
                XWPFParagraph paragraph = document.createParagraph();
                XWPFRun run = paragraph.createRun();
                run.setText(" ");
            }
        }
    }

    public List<IndicatorResponse> scanForIndicators(String textToScan, FiltersDto filterDto) {
        logger().info("Retrieved the indicators and its score found in the text");
        List<IndicatorResponse> response = new ArrayList<>();
        List<MLScanIndicator> mlIndicators = machineLearningService.scanForIndicators(textToScan, filterDto);
        if(!mlIndicators.isEmpty()) {
            List<Indicator> indicators = getIndicatorWithId(mlIndicators.stream().map(MLScanIndicator::getId).collect(Collectors.toList()));
            
            for (int i = 0; i < indicators.size(); i++) {
                int finalI = i;
                Optional<MLScanIndicator> scanIndicator = mlIndicators.stream().filter(x->x.getId().equals(indicators.get(finalI).getId())).findFirst();
                if(scanIndicator.isPresent())
                    indicators.get(i).setScore((int)Math.round(scanIndicator.get().getSearchResult().getSimilarity()));
            }
             // Sort indicators by Level priority
            List<Level> levelsList = levelRepository.findAllByOrderByPriority();
            logger().info("Starting the sort of the indicators");
            // Sort by Level and then by score
            response = indicators.stream().sorted((o1, o2) -> {
                if (o1.getLevel().getId().equals(o2.getLevel().getId())){
                    return o1.getScore() > o2.getScore() ? -1 :
                            (o1.getScore().equals(o2.getScore()) ? 0 : 1);
                }
                for (Level level : levelsList) {
                    if(level.getPriority().equals(o1.getLevel().getPriority())) return -1;
                    if(level.getPriority().equals(o2.getLevel().getPriority())) return 1;
                }
                return 1;
            }).map(this::convertIndicatorToIndicatorResponse).collect(Collectors.toList());
       }
        return response;

    }

    /**
     * Counts all indicators
     * @return Number of indicators
     */
    public Long getTotalNumIndicators(){
        logger().info("Counting all indicators");
        return indicatorRepository.count();
    }

    /**
     * Retrieves count of indicators by level and sector
     * @return List of count of indicators by level organized by sector
     */
    public List<NumIndicatorsSectorLevel> getIndicatorsByLevelAndSector(){
        logger().info("Counting indicators by its different sectors and levels");
        List<CounterSectorLevel> counterSectorLevel = indicatorRepository.countIndicatorsGroupedBySectorAndLevel();
        List<NumIndicatorsSectorLevel> list = new ArrayList<>();
        String lastSector = "";
        List<NumIndicatorsSectorLevel.CountIndicatorsByLevel> lastElement = new ArrayList<>();
        for (CounterSectorLevel element : counterSectorLevel) {
            if(lastSector.equals(element.getSector())){
                lastElement.add(new NumIndicatorsSectorLevel.CountIndicatorsByLevel(element.getLevel(), element.getCount()));
            }else {
                lastElement = new ArrayList<>();
                lastElement.add(new NumIndicatorsSectorLevel.CountIndicatorsByLevel(element.getLevel(), element.getCount()));
                list.add(new NumIndicatorsSectorLevel(element.getSector(), lastElement));
                lastSector = element.getSector();
            }
        }
        return list;
    }
}
