package com.arqaam.logframelab.service;

import com.arqaam.logframelab.model.persistence.Indicator;
import com.arqaam.logframelab.model.IndicatorResponse;
import com.arqaam.logframelab.model.persistence.Level;
import com.arqaam.logframelab.repository.IndicatorRepository;
import com.arqaam.logframelab.repository.LevelRepository;

import com.arqaam.logframelab.util.Logging;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.docx4j.jaxb.Context;
import org.docx4j.jaxb.XPathBinderAssociationIsPartialException;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.ObjectFactory;
import org.docx4j.wml.Text;
import org.docx4j.wml.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import java.io.*;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class IndicatorService implements Logging {

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    private IndicatorRepository indicatorRepository;

    @Autowired
    private LevelRepository levelRepository;

    /**
     * Extract Indicators from a Word file
     * @param tmpfilePath Temporary Path of the word file
     * @return List of Indicators
     */
    public List<IndicatorResponse> extractIndicatorsFromWordFile(Path tmpfilePath) {
        List<IndicatorResponse> result = new ArrayList();
        Map<Long, IndicatorResponse> mapResult = new HashMap<>();
        List<Indicator> indicatorsList = indicatorRepository.findAll();
        File tmpfile = tmpfilePath.toFile();
        List<String> wordstoScan = new ArrayList(); // current words
        // get the maximum indicator length
        int maxIndicatorLength = 1;
        if (indicatorsList != null && !indicatorsList.isEmpty()) {
            for (Indicator ind : indicatorsList) {
                if(ind.getKeywordsList() != null) {
                    for (String words : ind.getKeywordsList()) {
                        int numberKeywords = words.split(" ").length;
                        if (numberKeywords > maxIndicatorLength) {
                            maxIndicatorLength = numberKeywords;
                        }
                    }
                }
            }
            try {
                WordprocessingMLPackage wordMLPackage = null;
                wordMLPackage = WordprocessingMLPackage.load(tmpfile);
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
            } catch (JAXBException e) {
                e.printStackTrace();
            } catch (XPathBinderAssociationIsPartialException e) {
                e.printStackTrace();
            } catch (Docx4JException e) {
                e.printStackTrace();
            }
        }
        tmpfile.delete();
        return result;
    }

    /**
     * Fills a list of indicators that contain certain words
     * @param wordsToScan Words to find in the indicators' keyword list
     * @param indicators Indicators to be analyzed
     * @param mapResult Map Indicators' Id and IndicatorResponses
     * @param result List of Indicators that contains words to scan variable
     */
    protected void checkIndicators(List<String> wordsToScan, List<Indicator> indicators,
                                Map<Long, IndicatorResponse> mapResult,
                                List<IndicatorResponse> result) {
        logger().debug("Check Indicators with wordsToScan: {}, indicators: {}, mapResult: {}, result: {}",
                wordsToScan, indicators, mapResult, result);
        String wordsStr = wordsToScan.stream()
                .collect(Collectors.joining(" ", " ", " "));
        // key1 key2 key3 compared to ke,key1,key2 key3
        Iterator<Indicator> iterator = indicators.iterator();
        while (iterator.hasNext()) {
            Indicator indicator = iterator.next();
            if (indicator.getKeywordsList() != null && !indicator.getKeywordsList().isEmpty()) {
                Iterator<String> keysIterator = indicator.getKeywordsList().iterator();
                while (keysIterator.hasNext()) {
                    String currentKey = keysIterator.next();
                    if (wordsStr.toLowerCase().contains(" " + currentKey.toLowerCase() + " ")) {
                        // new indicator found
                        if (!mapResult.containsKey(indicator.getId())) {
                            IndicatorResponse indicatorResponse = new IndicatorResponse(result.size() + 1,
                                    indicator.getLevel().getName(),
                                    indicator.getLevel().getColor(),
                                    indicator.getName(),
                                    indicator.getDescription(),
                                    new ArrayList<String>(),
                                    indicator.getLevel().getTemplateVar());
                            result.add(indicatorResponse);
                            mapResult.put(indicator.getId(), indicatorResponse);
                        }
                        // add the keyword
                        mapResult.get(indicator.getId()).getKeys().add(currentKey);
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

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            File template = new File("/var/www/indicatorsExportTemplate.docx"); //resourceLoader.getResource("classpath:indicatorsExportTemplate.docx").getFile();
            WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(template);
            MainDocumentPart mainDocumentPart = wordMLPackage.getMainDocumentPart();
            String textNodesXPath = "//w:t";
            List<Object> textNodes = mainDocumentPart.getJAXBNodesViaXPath(textNodesXPath, true);
            Iterator<Object> iterator = textNodes.iterator();
            while (iterator.hasNext()) {
                Object obj = iterator.next();
                Text text = (Text) ((JAXBElement) obj).getValue();
                String value = text.getValue();
                if (value != null && value.contains("{var}")) {
                    parseVarWithValue(text, value, indicatorResponses);
                }
            }
            wordMLPackage.save(outputStream);
        } catch (Docx4JException e) {
            e.printStackTrace();
        } catch (JAXBException e) {
            e.printStackTrace();
        }
        return outputStream;
    }

    /**
     * Parse Nodes with indicators' label when the text param contains the indicators' var attribute
     * @param textNode Node that will have elements parsed into
     * @param text Text to be found in the indicators' var
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
                    TextIndicator.setValue(indicator.getLabel());
                    runParent.getContent().add(TextIndicator);
                    runParent.getContent().add(factory.createBr());
                    runParent.getContent().add(factory.createBr());
                    runParent.getContent().add(factory.createBr());
                    runParent.getContent().add(factory.createBr());
                }
            }
    }

    /**
     * TODO
     * @param path
     */
    public void importIndicators(String path) {
        logger().info("------ import indicators from xlsx");
        File file = new File(path);
        try {
            Workbook workbook = new XSSFWorkbook(file);
            Sheet datatypeSheet = workbook.getSheetAt(0);
            Iterator<Row> iterator = datatypeSheet.iterator();
            // skip the headers row
            if (iterator.hasNext()) {
                iterator.next();
            }
            while (iterator.hasNext()) {
                logger().info(" ");
                Row currentRow = iterator.next();
                Indicator indicator = new Indicator();
                indicator.setDescription(currentRow.getCell(1).getStringCellValue());
                // key words
                String[] keys = currentRow.getCell(2).getStringCellValue().toLowerCase().split(",");
                for (int i = 0; i < keys.length; i++) {
                    keys[i] = keys[i].trim().replaceAll("\\s+", " ");
                }
                indicator.setKeywords(String.join(",", keys));
                indicator.setName(currentRow.getCell(3).getStringCellValue());
                Level level = levelRepository.findLevelByName(currentRow.getCell(0).getStringCellValue().toUpperCase());
                if (level != null) {
                    indicator.setLevel(level);
                    logger().info(level.getName());
                    indicatorRepository.save(indicator);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidFormatException e) {
            e.printStackTrace();
        }
    }
}