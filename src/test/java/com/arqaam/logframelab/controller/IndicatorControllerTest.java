package com.arqaam.logframelab.controller;

import com.arqaam.logframelab.exception.WrongFileExtensionException;
import com.arqaam.logframelab.model.Error;
import com.arqaam.logframelab.model.IndicatorResponse;
import com.arqaam.logframelab.model.persistence.Indicator;
import com.arqaam.logframelab.model.persistence.Level;
import com.arqaam.logframelab.repository.IndicatorRepository;
import com.arqaam.logframelab.repository.LevelRepository;
import com.arqaam.logframelab.service.IndicatorService;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Text;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class IndicatorControllerTest extends BaseControllerTest {

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

    // Instead of mocking the repository maybe could load H2 memory with flyway
    @MockBean
    private LevelRepository levelRepository;

    @MockBean
    private IndicatorRepository indicatorRepository;

    @Autowired
    private IndicatorService indicatorService;

    @BeforeEach
    void setup(){
        when(levelRepository.findAllByOrderByPriority()).thenReturn(Arrays.stream(mockLevels).sorted().collect(Collectors.toList()));
        when(indicatorRepository.findAll(any(Specification.class))).
                thenReturn(mockIndicatorList().stream()
                        .filter(x -> mockThemes.contains(x.getThemes()) && mockLevelsId.contains(x.getLevel().getId()) && mockSources.contains(x.getSource())
                                && mockSdgCodes.contains(x.getSdgCode()) && mockCrsCodes.contains(x.getCrsCode())).collect(Collectors.toList()));

        when(indicatorRepository.findAll()).thenReturn(mockIndicatorList());
    }

    @Test
//    @Sql("/test_indicators.sql")
    void handleFileUpload() {
        List<IndicatorResponse> expectedResult = getExpectedResult();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ClassPathResource("test_doc.docx"));
        ResponseEntity<List<IndicatorResponse>> response = testRestTemplate.exchange("/indicator/upload", HttpMethod.POST,
                new HttpEntity<>(body, headers), new ParameterizedTypeReference<List<IndicatorResponse>>() {});

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEqualsIndicator(expectedResult, response.getBody());
    }


    @Test
    void handleFileUpload_indicatorsWithSameId() {
        List<String> keywordsFoodList = new ArrayList<>();
        keywordsFoodList.add("agriculture");
        keywordsFoodList.add("food");
        List<IndicatorResponse> expectedResult = getExpectedResult();
        List<Indicator> indicators = mockIndicatorList();
        indicators.add(Indicator.builder().id(1L).name("Name 1").description("Description").level(mockLevels[1])
                .keywords("agriculture").keywordsList(keywordsFoodList).build());
        indicators.add(Indicator.builder().id(4L).name("Name 4").description("Description").level(mockLevels[0]).build());

        when(indicatorRepository.findAll()).thenReturn(indicators);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ClassPathResource("test_doc.docx"));
        ResponseEntity<List<IndicatorResponse>> response = testRestTemplate.exchange("/indicator/upload", HttpMethod.POST,
                new HttpEntity<>(body, headers), new ParameterizedTypeReference<List<IndicatorResponse>>() {});

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEqualsIndicator(expectedResult, response.getBody());
    }

    @Test
    void handleFileUpload_withIndicatorsWithoutKeywords() {
        List<String> keywordsFoodList = new ArrayList<>();
        keywordsFoodList.add("food");
        List<String> keywordsList = new ArrayList<>();
        keywordsList.add(keywordsFoodList.get(0));
        List<IndicatorResponse> expectedResult = getExpectedResult();
        expectedResult.add(IndicatorResponse.builder().id(6L).level(mockLevels[2].getName()).color(mockLevels[2].getColor())
                .name("Name 6").description("Description").var(mockLevels[2].getTemplateVar()).build());
        expectedResult.add(IndicatorResponse.builder().id(5L).level(mockLevels[2].getName()).color(mockLevels[2].getColor())
                .name("Name 3").description("Description").var(mockLevels[2].getTemplateVar()).build());
        List<Indicator> indicators = mockIndicatorList();
        // This showcases how keywords property is irrelevant, only keywordList is taken into consideration
        indicators.add(Indicator.builder().id(6L).name("Name 6").description("Description").level(mockLevels[2]).keywordsList(keywordsList).build());
        indicators.add(Indicator.builder().id(2L).name("Name 2").description("Description").level(mockLevels[1]).build());
        indicators.add(Indicator.builder().id(3L).name("Name 3").description("Description").level(mockLevels[2]).keywords("agriculture").build());


        when(indicatorRepository.findAll()).thenReturn(indicators);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ClassPathResource("test_doc.docx"));
        ResponseEntity<List<IndicatorResponse>> response = testRestTemplate.exchange("/indicator/upload", HttpMethod.POST,
                new HttpEntity<>(body, headers), new ParameterizedTypeReference<List<IndicatorResponse>>() {});

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEqualsIndicator(expectedResult, response.getBody());
    }

    @Test
    void handleFileUpload_wrongFileFormat() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ClassPathResource("application.properties"));
        ResponseEntity<Error> response = testRestTemplate.exchange("/indicator/upload", HttpMethod.POST,
                new HttpEntity<>(body, headers), Error.class);
        assertEqualsException(response, HttpStatus.CONFLICT, 1, WrongFileExtensionException.class);
    }

    //TODO
    @Disabled("Needs fixing. If debugged it works, but just running it doesn't")
    @Test
    void downloadIndicators() throws Docx4JException, JAXBException, IOException {
        List<IndicatorResponse> indicators = createIndicatorResponseList(3);
        ResponseEntity<Resource> response = testRestTemplate.exchange("/indicator/download", HttpMethod.POST,
                new HttpEntity<>(indicators), Resource.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(response.getBody().getInputStream());
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
            System.out.println(currentText);
        }
        assertTrue(valid);
    }

    @Test
    void downloadIndicators_DFIDFormat() {
        List<IndicatorResponse> indicators = createIndicatorResponseList(3);
        when(indicatorRepository.findAllById(any())).thenReturn(mockIndicatorList());
        ResponseEntity<Resource> response = testRestTemplate.exchange("/indicator/download?format=dfid", HttpMethod.POST,
                new HttpEntity<>(indicators), Resource.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

    }

    @Test
    void downloadIndicators_emptyIndicators() {
        List<IndicatorResponse> indicators = new ArrayList<>();
        ResponseEntity<Error> response = testRestTemplate.exchange("/indicator/download", HttpMethod.POST,
                new HttpEntity<>(indicators), Error.class);
        assertEqualsException(response, HttpStatus.CONFLICT, 6, IllegalArgumentException.class);
    }

    @Test
    void getIndicators() {
        List<IndicatorResponse> expectedResult = getExpectedResult();
        ResponseEntity<List<IndicatorResponse>> response = testRestTemplate.exchange("/indicator?themes="+String.join(",", mockThemes)+
                        "&levels=" +mockLevelsId.stream().map(String::valueOf).collect(Collectors.joining(",")) +"&sources="+String.join(",", mockSources) +
                        "&sdgCodes=" + String.join(",", mockSdgCodes) + "&crsCodes="+String.join(",", mockCrsCodes), HttpMethod.GET,
                new HttpEntity<>(null), new ParameterizedTypeReference<List<IndicatorResponse>>() {});

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(indicatorRepository).findAll(any(Specification.class));
        verify(indicatorRepository, times(0)).findAll();
        assertEqualsIndicator(Arrays.asList(expectedResult.get(3), expectedResult.get(1), expectedResult.get(0), expectedResult.get(2)), response.getBody());
    }

    @Test
    void getIndicators_someFilters() {
        List<IndicatorResponse> expectedResult = getExpectedResult();
        ResponseEntity<List<IndicatorResponse>> response = testRestTemplate.exchange("/indicator?themes="+String.join(",", mockThemes)+
                        "&levels=" +mockLevelsId.stream().map(String::valueOf).collect(Collectors.joining(",")) +"&sources="+String.join(",", mockSources),
                HttpMethod.GET, new HttpEntity<>(null), new ParameterizedTypeReference<List<IndicatorResponse>>() {});

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(indicatorRepository).findAll(any(Specification.class));
        verify(indicatorRepository, times(0)).findAll();
        assertEqualsIndicator(Arrays.asList(expectedResult.get(3), expectedResult.get(1), expectedResult.get(0), expectedResult.get(2)), response.getBody());
    }

    @Test
    void getIndicators_noFilters() {
        List<IndicatorResponse> expectedResult = mockIndicatorList().stream()
                .map(indicatorService::convertIndicatorToIndicatorResponse).collect(Collectors.toList());
        ResponseEntity<List<IndicatorResponse>> response = testRestTemplate.exchange("/indicator", HttpMethod.GET,
                new HttpEntity<>(null), new ParameterizedTypeReference<List<IndicatorResponse>>() {});

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(indicatorRepository, times(0)).findAll(any(Specification.class));
        verify(indicatorRepository).findAll();
        assertEqualsIndicator(expectedResult, response.getBody());
    }

    private List<Indicator> mockIndicatorList() {
        String keyword = "food insecurity,agriculture";
        List<Indicator> list = new ArrayList<>();

        List<String> keywordsFoodList = new ArrayList<>();
        keywordsFoodList.add("agriculture");
        keywordsFoodList.add("food");

        List<String> keywordsPolicyList = new ArrayList<>();
        keywordsPolicyList.add("policy");

        List<String> keywordsGovList = new ArrayList<>();
        keywordsGovList.add("government");

        List<String> keywordsGovPolicyList = new ArrayList<>();
        keywordsGovPolicyList.add("government policies");
        keywordsGovPolicyList.add("policy");

        list.add(Indicator.builder().id(4L).name("Number of policies/strategies/laws/regulation developed/revised for digitalisation with EU support")
                .description("Digitalisation").level(mockLevels[0]).keywords("policy").keywordsList(keywordsPolicyList)
                .source(mockSources.get(0)).themes(mockThemes.get(0)).sdgCode(mockSdgCodes.get(0)).crsCode(mockCrsCodes.get(0)).build());
        list.add(Indicator.builder().id(73L).name("Number of government policies developed or revised with civil society organisation participation through EU support")
                .description("Public Sector").level(mockLevels[1]).keywords("government policies, policy").keywordsList(keywordsGovPolicyList)
                .source(mockSources.get(1)).themes(mockThemes.get(1)).sdgCode(mockSdgCodes.get(1)).crsCode(mockCrsCodes.get(1)).build());
        list.add(Indicator.builder().id(5L).name("Revenue, excluding grants (% of GDP)")
                .description("Public Sector").level(mockLevels[3]).keywords("government").keywordsList(keywordsGovList)
                .source(mockSources.get(2)).themes(mockThemes.get(2)).sdgCode(mockSdgCodes.get(2)).crsCode(mockCrsCodes.get(2)).build());
        list.add(Indicator.builder().id(1L).name("Number of food insecure people receiving EU assistance")
                .description("Food & Agriculture").level(mockLevels[1]).keywords(keyword).keywordsList(keywordsFoodList)
                .source(mockSources.get(3)).themes(mockThemes.get(3)).sdgCode(mockSdgCodes.get(3)).crsCode(mockCrsCodes.get(3)).build());

        return list;
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

    private List<IndicatorResponse> createIndicatorResponseList(int size){
        List<IndicatorResponse> list = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            int level = new Random().ints(0, 4).findFirst().getAsInt();

            list.add(IndicatorResponse.builder()
                    .id(i)
                    .level(mockLevels[level].getName())
                    .themes("Theme")
                    .source("Source")
                    .sdgCode("1.a")
                    .crsCode("CRS CODE")
                    .color(mockLevels[level].getColor())
                    .description("Description")
                    .disaggregation(level > 1) // Reusing random values
                    .name("Indicator Label "+ i)
                    .var(mockLevels[level].getTemplateVar())
                    .build());
        }
        return list;
    }

    private void assertEqualsIndicator(List<IndicatorResponse> expectedResult, List<IndicatorResponse> result){
        assertNotNull(result);
        assertEquals(expectedResult.size(), result.size());

        for (int i = 0; i <expectedResult.size(); i++) {
            assertEquals(expectedResult.get(i), result.get(i));
        }
    }

//    private Integer validateTemplateLevel(XSSFSheet sheet, List<Indicator> indicators, Integer rowIndex, Integer numberTemplateIndicators){
//        List<CellRangeAddress> mergedRegions = sheet.getMergedRegions();
//        Integer initialRow = rowIndex;
//
//        for (Indicator indicator : indicators) {
//            assertEquals("", sheet.getRow(rowIndex + 1).getCell(3).getStringCellValue());
//            assertEquals(indicator.getName(), sheet.getRow(rowIndex + 1).getCell(2).getStringCellValue());
//            assertEquals(indicator.getSourceVerification(), sheet.getRow(rowIndex + 3).getCell(3).getStringCellValue());
//            rowIndex += 4;
//        }
//
//        int count = indicators.size();
//        while(count<numberTemplateIndicators){
//            assertEquals("", sheet.getRow(rowIndex+1).getCell(2).getStringCellValue());
//            assertEquals("", sheet.getRow(rowIndex+3).getCell(3).getStringCellValue());
//            rowIndex+=4;
//            count++;
//        }
//
//        // check merged cells in first column
//        int finalRowIndex = rowIndex;
//        if(indicators.size()>numberTemplateIndicators){
//            if(numberTemplateIndicators.equals(IndicatorService.OUTPUT_NUM_TEMP_INDIC)){
//                assertTrue(mergedRegions.stream().anyMatch(x -> x.getLastColumn() == 0 && x.getFirstRow() == initialRow + numberTemplateIndicators * 3 - 1
//                        && x.getLastRow() == finalRowIndex - 1));
//            }else {
//                assertTrue(mergedRegions.stream().anyMatch(x -> x.getLastColumn() == 0 && x.getFirstRow() == initialRow + numberTemplateIndicators * 3
//                        && x.getLastRow() == finalRowIndex - 1));
//            }
//        }
//
//        return rowIndex;
//    }
}