package com.arqaam.logframelab.service;

import com.arqaam.logframelab.model.IndicatorResponse;
import com.arqaam.logframelab.model.persistence.Indicator;
import com.arqaam.logframelab.model.persistence.Level;
import com.arqaam.logframelab.repository.IndicatorRepository;
import com.arqaam.logframelab.repository.LevelRepository;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Br;
import org.docx4j.wml.R;
import org.docx4j.wml.Text;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
public class IndicatorServiceTest {

    @Mock
    private IndicatorRepository indicatorRepository;

    @Mock
    private LevelRepository levelRepository;

    @InjectMocks
    private IndicatorService indicatorService;

    private final static Level[] mockLevels = new Level[]{
        new Level(1L, "OUTPUT", "OUTPUT", "{output}", "green", 3),
        new Level(2L, "OUTCOME", "OUTCOME", "{outcomes}", "red", 2),
        new Level(3L, "OTHER_OUTCOMES", "OTHER OUTCOMES", "{otheroutcomes}", "orange", 4),
        new Level(4L, "IMPACT", "IMPACT", "{impact}", "purple", 1)
    };

    private final static List<String> mockThemes = Arrays.asList("Digitalisation", "Education", "Poverty",
            "Nutrition", "Agriculture", "Health", "WASH", "Electricity", "Private Sector",
            "Infrastructure", "Migration", "Climate Change", "Environment", "Public Sector",
            "Human Rights", "Conflict", "Food Security", "Equality", "Water and Sanitation");
    private final static List<String> mockSources = Arrays.asList("Capacity4Dev", "EU", "WFP", "ECHO", "ECHO,WFP",
            "ECHO,WHO", "FAO", "FAO,WHO", "WHO", "FANTA", "IPA", "WHO,FAO", "ACF",
            "Nutrition Cluster", "Freendom House", "CyberGreen", "ITU",
            "UN Sustainable Development Goals", "World Bank", "UNDP", "ILO", "IMF");
    private final static List<String> mockSdgCodes = Arrays.asList("8.2", "7.1", "4.1", "1.a", "1.b") ;
    private final static List<String> mockCrsCodes = Arrays.asList("99810.0", "15160.0", "24010.0", "15190.0", "43010.0", "24050.0", "43030.0");
    private final static List<Long> mockLevelsId = Arrays.stream(mockLevels).map(Level::getId).collect(Collectors.toList());
    private final static List<String> mockSourceVerification = Arrays.asList("World Bank Data", "EU", "SDG Country Data",
            "Project's M&E system", "UNDP Global Human Development Indicators");

    @BeforeEach
    void setup(){

        when(levelRepository.findAll()).thenReturn(Arrays.asList(mockLevels));
        when(levelRepository.findAllByOrderByPriority()).thenReturn(Arrays.stream(mockLevels).sorted().collect(Collectors.toList()));
        when(indicatorRepository.save(any(Indicator.class))).thenAnswer(i -> i.getArguments()[0]);
        when(indicatorRepository.findAll()).thenReturn(mockIndicatorList());

        when(indicatorRepository.findAll(any(Specification.class))).
            thenReturn(mockIndicatorList().stream()
                        .filter(x -> mockThemes.contains(x.getThemes()) && mockLevelsId.contains(x.getLevel().getId()) && mockSources.contains(x.getSource())
                                && mockSdgCodes.contains(x.getSdgCode()) && mockCrsCodes.contains(x.getCrsCode())).collect(Collectors.toList()));
    }

    @Test
    void extractIndicatorsFromWordFile() throws IOException {
        when(indicatorRepository.findAll()).thenReturn(mockIndicatorList());
        List<IndicatorResponse> expectedResult = new ArrayList<>();
        expectedResult.add(IndicatorResponse.builder().build());
        MultipartFile file = new MockMultipartFile("test_doc.docx", new ClassPathResource("test_doc.docx").getInputStream());
        List<IndicatorResponse> result = indicatorService.extractIndicatorsFromWordFile(file, null);
        assertNotNull(result);
        assertFalse(result.isEmpty());
        for(IndicatorResponse response : result){
            System.out.println(response);
        }
//        assertEquals(1, result.size());
    }

    @Test
    void checkIndicators() {
        List<String> wordsToScan = Arrays.asList("food", "government", "policy", "retirement");
        List<Indicator> indicators = mockIndicatorList();
        // Test also indicators without keyword
        indicators.add(Indicator.builder().id(6L).name("Name").description("Description").build());
        Map<Long, Indicator> mapResult = new HashMap<>();
        indicatorService.checkIndicators(wordsToScan, indicators, mapResult);

        assertEquals(3, mapResult.size());
        for (int i = 0; i < 3; i++) {
            assertEquals(indicators.get(i+1), mapResult.values().toArray()[i]);
        }
    }

    @Test
    void checkIndicators_withIndicatorsWithSameId() {
        List<String> keywordsPolicyList = new ArrayList<>();
        keywordsPolicyList.add("policy");
        List<String> wordsToScan = Arrays.asList("food", "government", "policy", "retirement");
        List<Indicator> indicators = mockIndicatorList();
        indicators.add(Indicator.builder().id(4L).name("Number of policies/strategies/laws/regulation developed/revised for digitalization with EU support")
                .description("Digitalisation").keywords("policy").keywordsList(keywordsPolicyList).build());
        Map<Long, Indicator> mapResult = new HashMap<>();
        indicatorService.checkIndicators(wordsToScan, indicators, mapResult);

        assertEquals(3, mapResult.size());
        for (int i = 0; i < 3; i++) {
            assertEquals(indicators.get(i+1), mapResult.values().toArray()[i]);
        }
    }

    @Test
    void checkIndicators_withoutIndicators() {
        List<String> wordsToScan = Arrays.asList("food", "government", "policy", "retirement");
        List<Indicator> indicators = new ArrayList<>();
        Map<Long, Indicator> mapResult = new HashMap<>();
        indicatorService.checkIndicators(wordsToScan, indicators, mapResult);

        assertEquals(0, mapResult.size());
    }

    @Test
    void checkIndicators_withoutWordsToScan() {
        List<String> wordsToScan = new ArrayList<>();
        List<Indicator> indicators = new ArrayList<>();
        Map<Long, Indicator> mapResult = new HashMap<>();
        indicatorService.checkIndicators(wordsToScan, indicators, mapResult);

        assertTrue(mapResult.isEmpty());
    }

    @Test
    void exportIndicatorsInWordFile() throws Docx4JException, JAXBException {
        List<IndicatorResponse> indicators = createListIndicatorResponse();
        ByteArrayOutputStream result = indicatorService.exportIndicatorsInWordFile(indicators);
        assertNotNull(result);

        WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(new ByteArrayInputStream(result.toByteArray()));
        List<Object> textNodes = wordMLPackage.getMainDocumentPart().getJAXBNodesViaXPath("//w:t", true);
        boolean valid = false;
        int c = 0;
        for (Object obj : textNodes) {
            String currentText = ((Text) ((JAXBElement) obj).getValue()).getValue();
            if(currentText.equals(indicators.get(c).getName())) {
                c++;
                if(c == indicators.size()){
                    valid = true;
                    break;
                }
            }
//            System.out.println(currentText);
        }
        assertTrue(valid);
    }

    @Test
    void parseVarWithValue_withMatchingText() {
        String text = "var";
        List<IndicatorResponse> indicators = createListIndicatorResponse();
        Text textNode = new Text();
        textNode.setParent(new R());
        indicatorService.parseVarWithValue(textNode, text, indicators);
        R result = (R) textNode.getParent();
        assertEquals(5*indicators.size(), result.getContent().size());
        for (int i = 0; i < indicators.size()*5; i+=5) {
            assertEquals(indicators.get(i/5).getName(), ((Text)result.getContent().get(i)).getValue());
            assertTrue(result.getContent().get(i+1) instanceof Br);
            assertTrue(result.getContent().get(i+2) instanceof Br);
            assertTrue(result.getContent().get(i+3) instanceof Br);
            assertTrue(result.getContent().get(i+4) instanceof Br);
        }
    }

    @Test
    void parseVarWithValue_withoutMatchingText() {
        String text = "text without matching";
        List<IndicatorResponse> indicators = createListIndicatorResponse();
        Text textNode = new Text();
        textNode.setParent(new R());
        indicatorService.parseVarWithValue(textNode, text, indicators);
        R result = (R) textNode.getParent();
        assertEquals(0, result.getContent().size());
    }

    @Test
    void parseVarWithValue_withoutIndicatorResponse() {
        String text = "var";
        Text textNode = new Text();
        textNode.setParent(new R());
        indicatorService.parseVarWithValue(textNode, text, null);
        R result = (R) textNode.getParent();
        assertEquals(0, result.getContent().size());

        Text textNode2 = new Text();
        textNode2.setParent(new R());
        R result2 = (R) textNode2.getParent();
        indicatorService.parseVarWithValue(textNode2, text, Collections.emptyList());
        assertEquals(0, result2.getContent().size());
    }

    @Test
    void importIndicators() {
        //TODO this test
//        indicatorService.importIndicators(new ClassPathResource("Indicator.xlsx").getPath());

//        indicatorService.importIndicators("/home/ari/Downloads/Indicator.xlsx");
//        indicatorService.importIndicators("/home/ari/Downloads/SDGs_changed.xlsx");

    }

    @Test
    void exportIndicatorsInWorksheet() {
        List<Indicator> expectedResult = mockIndicatorList();

        when(indicatorRepository.findAllById(any())).thenReturn(expectedResult);
        ByteArrayOutputStream outputStream = indicatorService.exportIndicatorsInWorksheet(createListIndicatorResponse());
//        MultipartFile multipartFile = new MockMultipartFile("indicators_export.xlsx", outputStream.toByteArray());
//        List<Indicator> result = indicatorService.importIndicators(multipartFile);
//
//        // because Id in the result is null, and in the expected result it isn't.
//        for (int i = 0; i < expectedResult.size(); i++) {
//            assertEquals(expectedResult.get(i).getLevel(), result.get(i).getLevel());
//            assertEquals(expectedResult.get(i).getKeywordsList(), result.get(i).getKeywordsList());
//            assertEquals(expectedResult.get(i).getDisaggregation(), result.get(i).getDisaggregation());
//            assertEquals(expectedResult.get(i).getCrsCode(), result.get(i).getCrsCode());
//            assertEquals(expectedResult.get(i).getDescription(), result.get(i).getDescription());
//            assertEquals(expectedResult.get(i).getName(), result.get(i).getName());
//            assertEquals(expectedResult.get(i).getSdgCode(), result.get(i).getSdgCode());
//            assertEquals(expectedResult.get(i).getSource(), result.get(i).getSource());
//            assertEquals(expectedResult.get(i).getThemes(), result.get(i).getThemes());
//            assertEquals(expectedResult.get(i).getDataSource(), result.get(i).getDataSource());
//            assertEquals(expectedResult.get(i).getSourceVerification(), result.get(i).getSourceVerification());
//        }

//        try(OutputStream fileOutputStream = new FileOutputStream("thefilename.xlsx")) {
//            outputStream.writeTo(fileOutputStream);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    @Test
    void exportIndicatorsDFIDFormat() throws IOException {
        when(indicatorRepository.findAllById(any())).thenReturn(mockIndicatorList());
        List<Indicator> impactIndicators = mockIndicatorList().stream().filter(x -> x.getLevel().equals(mockLevels[3])).collect(Collectors.toList());
        List<Indicator> outcomeIndicators = mockIndicatorList().stream().filter(x -> x.getLevel().equals(mockLevels[1])).collect(Collectors.toList());
        List<Indicator> outputIndicators = mockIndicatorList().stream().filter(x -> x.getLevel().equals(mockLevels[0])).collect(Collectors.toList());

        ByteArrayOutputStream outputStream = indicatorService.exportIndicatorsDFIDFormat(getExpectedResult());

        assertNotNull(outputStream);
        XSSFSheet sheet = new XSSFWorkbook(new ByteArrayInputStream(outputStream.toByteArray())).getSheetAt(0);

        int rowIndex = 1;
        rowIndex = validateTemplateLevel(sheet, impactIndicators, rowIndex, IndicatorService.IMPACT_NUM_TEMP_INDIC);
        rowIndex = validateTemplateLevel(sheet, outcomeIndicators, rowIndex, IndicatorService.OUTCOME_NUM_TEMP_INDIC);
        validateTemplateLevel(sheet, outputIndicators, rowIndex, IndicatorService.OUTPUT_NUM_TEMP_INDIC);

        sheet.getWorkbook().close();
    }

    @Test
    void exportIndicatorsDFIDFormat_noImpactIndicators_newRowsOutcome() throws IOException {
        List<Indicator> mockIndicators = mockIndicatorList().stream().filter(x -> !x.getLevel().equals(mockLevels[3])).collect(Collectors.toList());
        mockIndicators.add(new Indicator(100L,"Extra indicator 1", "","", mockLevels[1], "",
                "", false, "", "", mockSourceVerification.get(0), "", null, 0));
        mockIndicators.add(new Indicator(100L,"Extra indicator 2", "","", mockLevels[1], "",
                "", false, "", "", mockSourceVerification.get(1), "", null, 0));
        mockIndicators.add(new Indicator(100L,"Extra indicator 3", "","", mockLevels[1], "",
                "", false, "", "", mockSourceVerification.get(2), "", null, 0));
        when(indicatorRepository.findAllById(any())).thenReturn(mockIndicators);

        ByteArrayOutputStream outputStream = indicatorService.exportIndicatorsDFIDFormat(mockIndicators.stream()
                .map(indicatorService::convertIndicatorToIndicatorResponse).collect(Collectors.toList()));

        assertNotNull(outputStream);
        XSSFSheet sheet = new XSSFWorkbook(new ByteArrayInputStream(outputStream.toByteArray())).getSheetAt(0);

        int rowIndex = 1;
        rowIndex = validateTemplateLevel(sheet, Collections.emptyList(), rowIndex, IndicatorService.IMPACT_NUM_TEMP_INDIC);
        rowIndex = validateTemplateLevel(sheet, mockIndicators.stream().filter(x -> x.getLevel().equals(mockLevels[1])).collect(Collectors.toList()),
                rowIndex, IndicatorService.OUTCOME_NUM_TEMP_INDIC);
        validateTemplateLevel(sheet, mockIndicators.stream().filter(x -> x.getLevel().equals(mockLevels[0])).collect(Collectors.toList()),
                rowIndex, IndicatorService.OUTPUT_NUM_TEMP_INDIC);
        sheet.getWorkbook().close();
    }

    @Test
    void exportIndicatorsDFIDFormat_noOutcomeIndicators_newRowsOutput() throws IOException {
        List<Indicator> mockIndicators = mockIndicatorList().stream().filter(x -> !x.getLevel().equals(mockLevels[1])).collect(Collectors.toList());
        mockIndicators.add(new Indicator(100L,"Extra indicator 1", "","", mockLevels[0], "",
                "", false, "", "", mockSourceVerification.get(0), "", null, 0));
        mockIndicators.add(new Indicator(100L,"Extra indicator 2", "","", mockLevels[0], "",
                "", false, "", "", mockSourceVerification.get(1), "", null, 0));
        mockIndicators.add(new Indicator(100L,"Extra indicator 3", "","", mockLevels[0], "",
                "", false, "", "", mockSourceVerification.get(2), "", null, 0));
        when(indicatorRepository.findAllById(any())).thenReturn(mockIndicators);

        ByteArrayOutputStream outputStream = indicatorService.exportIndicatorsDFIDFormat(mockIndicators.stream()
                .map(indicatorService::convertIndicatorToIndicatorResponse).collect(Collectors.toList()));

        assertNotNull(outputStream);
        XSSFSheet sheet = new XSSFWorkbook(new ByteArrayInputStream(outputStream.toByteArray())).getSheetAt(0);

        int rowIndex = 1;
        rowIndex = validateTemplateLevel(sheet, mockIndicators.stream().filter(x -> x.getLevel().equals(mockLevels[3])).collect(Collectors.toList()),
                rowIndex, IndicatorService.IMPACT_NUM_TEMP_INDIC);
        rowIndex = validateTemplateLevel(sheet, Collections.emptyList(), rowIndex, IndicatorService.OUTCOME_NUM_TEMP_INDIC);
        validateTemplateLevel(sheet, mockIndicators.stream().filter(x -> x.getLevel().equals(mockLevels[0])).collect(Collectors.toList()),
                rowIndex, IndicatorService.OUTPUT_NUM_TEMP_INDIC);
        sheet.getWorkbook().close();
    }

    @Test
    void exportIndicatorsDFIDFormat_noOutputIndicators_newRowsImpact() throws IOException {
        List<Indicator> mockIndicators = mockIndicatorList().stream().filter(x -> !x.getLevel().equals(mockLevels[0])).collect(Collectors.toList());
        mockIndicators.add(new Indicator(100L,"Extra indicator 1", "","", mockLevels[3], "",
                "", false, "", "", mockSourceVerification.get(0), "", null, 0));
        mockIndicators.add(new Indicator(100L,"Extra indicator 2", "","", mockLevels[3], "",
                "", false, "", "", mockSourceVerification.get(1), "", null, 0));
        mockIndicators.add(new Indicator(100L,"Extra indicator 3", "","", mockLevels[3], "",
                "", false, "", "", mockSourceVerification.get(2), "", null, 0));
        when(indicatorRepository.findAllById(any())).thenReturn(mockIndicators);

        ByteArrayOutputStream outputStream = indicatorService.exportIndicatorsDFIDFormat(mockIndicators.stream()
                .map(indicatorService::convertIndicatorToIndicatorResponse).collect(Collectors.toList()));

        assertNotNull(outputStream);
        XSSFSheet sheet = new XSSFWorkbook(new ByteArrayInputStream(outputStream.toByteArray())).getSheetAt(0);

        int rowIndex = 1;
        rowIndex = validateTemplateLevel(sheet, mockIndicators.stream().filter(x -> x.getLevel().equals(mockLevels[3])).collect(Collectors.toList()),
                rowIndex, IndicatorService.IMPACT_NUM_TEMP_INDIC);
        rowIndex = validateTemplateLevel(sheet, mockIndicators.stream().filter(x -> x.getLevel().equals(mockLevels[1])).collect(Collectors.toList()),
                rowIndex, IndicatorService.OUTCOME_NUM_TEMP_INDIC);
        validateTemplateLevel(sheet, Collections.emptyList(), rowIndex, IndicatorService.OUTPUT_NUM_TEMP_INDIC);
        sheet.getWorkbook().close();
    }
    @Test
    void exportIndicatorsDFIDFormat_newRowsForEveryLevel() throws IOException {
        List<Indicator> mockIndicators = mockIndicatorList();
        mockIndicators.addAll(mockIndicatorList());
        mockIndicators.addAll(mockIndicatorList());
        when(indicatorRepository.findAllById(any())).thenReturn(mockIndicators);

        ByteArrayOutputStream outputStream = indicatorService.exportIndicatorsDFIDFormat(mockIndicators.stream()
                .map(indicatorService::convertIndicatorToIndicatorResponse).collect(Collectors.toList()));

        assertNotNull(outputStream);
        XSSFSheet sheet = new XSSFWorkbook(new ByteArrayInputStream(outputStream.toByteArray())).getSheetAt(0);

        int rowIndex = 1;
        rowIndex = validateTemplateLevel(sheet, mockIndicators.stream().filter(x -> x.getLevel().equals(mockLevels[3])).collect(Collectors.toList()),
                rowIndex, IndicatorService.IMPACT_NUM_TEMP_INDIC);
        rowIndex = validateTemplateLevel(sheet, mockIndicators.stream().filter(x -> x.getLevel().equals(mockLevels[1])).collect(Collectors.toList()),
                rowIndex, IndicatorService.OUTCOME_NUM_TEMP_INDIC);
        validateTemplateLevel(sheet, mockIndicators.stream().filter(x -> x.getLevel().equals(mockLevels[0])).collect(Collectors.toList()),
                rowIndex, IndicatorService.OUTPUT_NUM_TEMP_INDIC);
        sheet.getWorkbook().close();
    }

    private List<Indicator> mockIndicatorList() {
        String keyword = "food insecurity,nutrition,farming,agriculture";
        List<Indicator> list = new ArrayList<>();

        List<String> keywordsList = new ArrayList<>();
        keywordsList.add("agriculture");
        keywordsList.add("nutrition");

        List<String> keywordsPolicyList = new ArrayList<>();
        keywordsPolicyList.add("policy");

        List<String> keywordsGovList = new ArrayList<>();
        keywordsGovList.add("government");

        List<String> keywordsGovPolicyList = new ArrayList<>();
        keywordsGovPolicyList.add("government policies");
        keywordsGovPolicyList.add("policy");

        list.add(Indicator.builder().id(1L).name("Number of food insecure people receiving EU assistance")
                .themes("Global Partnership for Sustainable Development")
                .source("UN Sustainable Development Goals")
                .disaggregation(true)
                .crsCode("51010.0")
                .sdgCode("19.4")
                .sourceVerification("Capacity4Dev")
                .dataSource("https://data.worldbank.org/indicator/SN.ITK.VITA.ZS?view=chart")
                .description("Food & Agriculture").keywords(keyword).level(mockLevels[1]).keywordsList(keywordsList).build());
        list.add(Indicator.builder().id(4L).name("Number of policies/strategies/laws/regulation developed/revised for digitalization with EU support")
                .themes("Global Partnership for Sustainable Development")
                .source("UN Sustainable Development Goals")
                .disaggregation(true)
                .crsCode("43060.0")
                .sdgCode("1.a")
                .sourceVerification("Capacity4Dev")
                .dataSource("https://data.worldbank.org/indicator/SI.POV.URGP?view=chart")
                .description("Digitalisation").keywords("policy").level(mockLevels[0]).keywordsList(keywordsPolicyList).build());
        list.add(Indicator.builder().id(5L).name("Revenue, excluding grants (% of GDP)")
                .themes("Global Partnership for Sustainable Development")
                .source("UN Sustainable Development Goals")
                .disaggregation(false)
                .crsCode("99810.0")
                .sdgCode("17.4")
                .sourceVerification("HIPSO")
                .dataSource("https://data.worldbank.org/indicator/EG.CFT.ACCS.ZS?view=chart")
                .description("Technical Note, EURF 2.01").keywords("government").level(mockLevels[3]).keywordsList(keywordsGovList).build());
        list.add(Indicator.builder().id(73L).name("Number of government policies developed or revised with civil society organisation participation through EU support")
                .description("Public Sector").keywords("government policies, policy").level(mockLevels[1]).keywordsList(keywordsGovPolicyList)
                .sourceVerification(mockSourceVerification.get(3)).build());

        return list;
    }

    private List<IndicatorResponse> createListIndicatorResponse() {
        List<IndicatorResponse> list = new ArrayList<>();
        for (int i = 1; i < 6; i++) {
            list.add(IndicatorResponse.builder().id(i).level("IMPACT").color("color").name("Label "+i)
                    .description("Description").var("var").build());
        }
        return list;
    }

    @Test
    void getIndicators() {
        List<Indicator> expectedResult = mockIndicatorList().stream()
                .filter(x -> mockThemes.contains(x.getThemes()) && mockLevelsId.contains(x.getLevel().getId()) && mockSources.contains(x.getSource())
                        && mockSdgCodes.contains(x.getSdgCode()) && mockCrsCodes.contains(x.getCrsCode())).collect(Collectors.toList());

        List<Indicator> result = indicatorService.getIndicators(Optional.of(mockThemes),
                Optional.of(mockSources), Optional.of(mockLevelsId), Optional.of(mockSdgCodes), Optional.of(mockCrsCodes));
        verify(indicatorRepository).findAll(any(Specification.class));
        verify(indicatorRepository, times(0)).findAll();
        assertEquals(expectedResult, result);
    }

    @Test
    void getIndicators_someFilters() {
        when(indicatorRepository.findAll(any(Specification.class))).
                thenReturn(mockIndicatorList().stream()
                        .filter(x -> mockThemes.contains(x.getThemes()) && mockLevelsId.contains(x.getLevel().getId()) && mockSources.contains(x.getSource())
                        ).collect(Collectors.toList()));

        List<Indicator> expectedResult = mockIndicatorList().stream()
                .filter(x -> mockThemes.contains(x.getThemes()) && mockLevelsId.contains(x.getLevel().getId()) && mockSources.contains(x.getSource())
                ).collect(Collectors.toList());

        List<Indicator> result = indicatorService.getIndicators(Optional.of(mockThemes),
                Optional.of(mockSources), Optional.of(mockLevelsId), Optional.empty(), Optional.empty());
        verify(indicatorRepository).findAll(any(Specification.class));
        verify(indicatorRepository, times(0)).findAll();
        assertEquals(expectedResult, result);
    }

    @Test
    void getIndicators_noFilter() {
        List<Indicator> expectedResult = mockIndicatorList();
        List<Indicator> result = indicatorService.getIndicators(Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty());
        verify(indicatorRepository, times(0)).findAll(any(Specification.class));
        verify(indicatorRepository).findAll();
        assertEquals(expectedResult, result);
    }

    private List<IndicatorResponse> getExpectedResult(){
        List<Indicator> indicators = mockIndicatorList();
        List<IndicatorResponse> indicatorResponses = new ArrayList<>();
        indicatorResponses.add(indicatorService.convertIndicatorToIndicatorResponse(indicators.get(2)));
        indicatorResponses.add(indicatorService.convertIndicatorToIndicatorResponse(indicators.get(1)));
        indicatorResponses.add(indicatorService.convertIndicatorToIndicatorResponse(indicators.get(3)));
        indicatorResponses.add(indicatorService.convertIndicatorToIndicatorResponse(indicators.get(0)));
        return indicatorResponses;
    }

    private Integer validateTemplateLevel(XSSFSheet sheet, List<Indicator> indicators, Integer rowIndex, Integer numberTemplateIndicators){
        List<CellRangeAddress> mergedRegions = sheet.getMergedRegions();
        Integer initialRow = rowIndex;

        for (Indicator indicator : indicators) {
            assertEquals("", sheet.getRow(rowIndex + 1).getCell(3).getStringCellValue());
            assertEquals(indicator.getName(), sheet.getRow(rowIndex + 1).getCell(2).getStringCellValue());
            assertEquals(indicator.getSourceVerification(), sheet.getRow(rowIndex + 3).getCell(3).getStringCellValue());
            rowIndex += 4;
        }

        int count = indicators.size();
        while(count<numberTemplateIndicators){
            assertEquals("", sheet.getRow(rowIndex+1).getCell(2).getStringCellValue());
            assertEquals("", sheet.getRow(rowIndex+3).getCell(3).getStringCellValue());
            rowIndex+=4;
            count++;
        }

        // check merged cells in first column
        int finalRowIndex = rowIndex;
        if(indicators.size()>numberTemplateIndicators){
            if(numberTemplateIndicators.equals(IndicatorService.OUTPUT_NUM_TEMP_INDIC)){
                assertTrue(mergedRegions.stream().anyMatch(x -> x.getLastColumn() == 0 && x.getFirstRow() == initialRow + numberTemplateIndicators * 4 - 1
                        && x.getLastRow() == finalRowIndex - 1));
            }else {
                assertTrue(mergedRegions.stream().anyMatch(x -> x.getLastColumn() == 0 && x.getFirstRow() == initialRow + numberTemplateIndicators * 3
                        && x.getLastRow() == finalRowIndex - 1));
            }
        }

        return rowIndex;
    }
}