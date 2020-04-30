package com.arqaam.logframelab.service;

import com.arqaam.logframelab.exception.*;
import com.arqaam.logframelab.model.IndicatorResponse;
import com.arqaam.logframelab.model.persistence.Indicator;
import com.arqaam.logframelab.model.persistence.Level;
import com.arqaam.logframelab.repository.IndicatorRepository;
import com.arqaam.logframelab.repository.LevelRepository;
import com.arqaam.logframelab.util.Logging;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.docx4j.jaxb.Context;
import org.docx4j.jaxb.XPathBinderAssociationIsPartialException;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.ObjectFactory;
import org.docx4j.wml.R;
import org.docx4j.wml.Text;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;

@Service
public class IndicatorService implements Logging {

    @Autowired
    private IndicatorRepository indicatorRepository;

    @Autowired
    private LevelRepository levelRepository;

    /**
     * Extract Indicators from a Word file
     * @param tmpfilePath Temporary Path of the word file
     * @return List of Indicators
     */
    public List<IndicatorResponse> extractIndicatorsFromWordFile(Path tmpfilePath, List<String> themeFilter) {
        List<IndicatorResponse> result = new ArrayList();
        Map<Long, IndicatorResponse> mapResult = new HashMap<>();
        List<Indicator> indicatorsList;
        if (themeFilter != null && !themeFilter.isEmpty()) {
            indicatorsList = indicatorRepository.getIndicatorsByThemes(themeFilter);
        } else {
            indicatorsList = indicatorRepository.findAll();
        }
        File tmpfile = tmpfilePath.toFile();
        List<String> wordstoScan = new ArrayList(); // current words
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
                WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(tmpfile);
                MainDocumentPart mainDocumentPart = wordMLPackage.getMainDocumentPart();
                String textNodesXPath = "//w:t";
                List<Object> textNodes = mainDocumentPart.getJAXBNodesViaXPath(textNodesXPath, true);
                Pattern p = Pattern.compile("[a-z0-9]", Pattern.CASE_INSENSITIVE);
                StringBuffer currentWord = null;

                for (Object obj : textNodes) {
                    Text text = (Text) ((JAXBElement) obj).getValue();
                    String currentText = text.getValue();
                    char[] strArray = currentText.toLowerCase().toCharArray();
                    for (int i = 0; i < strArray.length; i++) {
                        // append current word to wordstoScan list
                        if (currentWord == null && p.matcher(strArray[i] + "").find()) {
                            currentWord = new StringBuffer();
                        } else if (currentWord != null && !p.matcher(strArray[i] + "").find()) {
                            wordstoScan.add(currentWord.toString());
                            currentWord = null;
                        }
                        if (currentWord != null) {
                            currentWord.append(strArray[i]);
                        }
                        // clear wordstoScan list if exceed the max length of indicators
                        if (wordstoScan.size() == maxIndicatorLength) {
                            checkIndicators(wordstoScan, indicatorsList, mapResult, result);
                            wordstoScan.remove(wordstoScan.size() - 1);
                        }
                    }
                }
                if(!result.isEmpty()) {
                    // Sort by Level
                    //TODO fix this static strings
                    result = result.stream().sorted((o1, o2) -> {
                        if (o1.getLevel().equals(o2.getLevel())) return 0;
                        if (o1.getLevel().equals("IMPACT")) return -1;
                        if (o2.getLevel().equals("IMPACT")) return 1;
                        if (o1.getLevel().equals("OUTCOME")) return -1;
                        if (o2.getLevel().equals("OUTCOME")) return 1;
                        if (o1.getLevel().equals("OUTPUT")) return -1;
                        return 1;
                    }).collect(Collectors.toList());
                }
            } catch (XPathBinderAssociationIsPartialException | JAXBException e) {
                logger().error("Failed to process temporary word file", e);
                throw new FailedToProcessWordFileException();
            } catch (Docx4JException e){
                logger().error("Failed to load/process the temporary word file.TmpFileName: {}", tmpfile.getName(), e);
                throw new WordFileLoadFailedException();
            }
        }
        tmpfile.delete();
        return result;
    }

    /**
     * Fills a list of indicators that contain certain words
     * @param wordsToScan Words to find in the indicators' keyword list
     * @param indicators  Indicators to be analyzed
     * @param mapResult   Map Indicators' Id and IndicatorResponses
     * @param result      List of Indicators that contains words to scan variable
     */
    protected void checkIndicators(List<String> wordsToScan, List<Indicator> indicators,
                                   Map<Long, IndicatorResponse> mapResult,
                                   List<IndicatorResponse> result) {
        logger().debug("Check Indicators with wordsToScan: {}, indicators: {}, mapResult: {}, result: {}",
                wordsToScan, indicators, mapResult, result);
        String wordsStr = wordsToScan.stream()
                .collect(Collectors.joining(" ", " ", " "));
        // key1 key2 key3 compared to ke,key1,key2 key3
        for(Indicator indicator : indicators) {
            if (indicator.getKeywordsList() != null && !indicator.getKeywordsList().isEmpty()) {
                Iterator<String> keysIterator = indicator.getKeywordsList().iterator();
                while (keysIterator.hasNext()) {
                    String currentKey = keysIterator.next();
//                for(String currentKey : indicator.getKeywordsList()) {
                    if (wordsStr.toLowerCase().contains(" " + currentKey.toLowerCase() + " ")) {
                        // new indicator found
                        if (!mapResult.containsKey(indicator.getId())) {
                            IndicatorResponse indicatorResponse = IndicatorResponse.builder()
                                    .id(result.size() + 1)
                                    .level(indicator.getLevel().getName())
                                    .color(indicator.getLevel().getColor())
                                    .name(indicator.getName())
                                    .description(indicator.getDescription())
//                                    .keys(new ArrayList<>())
                                    .var(indicator.getLevel().getTemplateVar())
                                    .themes(indicator.getThemes())
                                    .disaggregation(indicator.getDisaggregation())
                                    .crsCode(indicator.getCrsCode())
                                    .sdgCode(indicator.getSdgCode())
                                    .source(indicator.getSource())
                                    .build();
                            result.add(indicatorResponse);
                            mapResult.put(indicator.getId(), indicatorResponse);
                        }
                        // add the keyword
//                        mapResult.get(indicator.getId()).getKeys().add(currentKey);
                        //
                        keysIterator.remove();
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
                Text text = (Text) ((JAXBElement) obj).getValue();
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
     * Import Indicators from an excel file with the extension xlsx
     * @param path Path of the excel file
     */
    public void importIndicators(String path) {
        List<Level> levels = levelRepository.findAll();
        Map<String, Level> levelMap = new HashMap<>();
        for (Level lvl : levels){
            levelMap.put(lvl.getName(), lvl);
        }
        logger().info("Importing indicators from xlsx, path {}", path);
        File file = new File(path);
        try {
            Iterator<Row> iterator = new XSSFWorkbook(file).getSheetAt(0).iterator();
            // skip the headers row
            if (iterator.hasNext()) {
                iterator.next();
            }
            int count=-1;
            while (iterator.hasNext()) {
                logger().info(" ");
                Row currentRow = iterator.next();

//                indicator.setDescription(currentRow.getCell(1).getStringCellValue());
                // key words
                String[] keys = currentRow.getCell(2).getStringCellValue().toLowerCase().split(",");
                for (int i = 0; i < keys.length; i++) {
                    keys[i] = keys[i].trim().replaceAll("\\s+", " ");
                }
//                indicator.setKeywords(String.join(",", keys));
//                indicator.setName(currentRow.getCell(3).getStringCellValue());
//                Level level = levelRepository.findLevelByName(currentRow.getCell(0).getStringCellValue().toUpperCase());
                Level level = levelMap.get(currentRow.getCell(0).getStringCellValue().toUpperCase());
                if (!isNull(level)) {
                    Cell crsCodeCell = currentRow.getCell(7, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
//                    indicator.setLevel(level);
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
                    indicatorRepository.save(indicator);
                    count++;
                }
            }
//            Level	Theme	Key words	Indicator	Description	Source	Disaggregation	DAC 5 / CRS	SDG	Sources of Verification	Data Source

        } catch (IOException e) {
            logger().error("Failed to open worksheet.", e);
            throw new FailedToOpenWorksheetException();
        } catch (InvalidFormatException e) {
            logger().error("Failed to interpret worksheet. It must be in a wrong format.", e);
            throw new WorksheetInWrongFormatException();
        }
    }

    /**
     * Get all thematic areas of indicators
     * @return all thematic areas
     */
    public List<String> getAllThemes() {
        return indicatorRepository.getThemes();
    }
}