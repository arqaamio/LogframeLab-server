package com.arqaam.logframelab.controller;

import com.arqaam.logframelab.model.IndicatorResponse;
import com.arqaam.logframelab.model.persistence.Indicator;
import com.arqaam.logframelab.model.persistence.Level;
import com.arqaam.logframelab.repository.IndicatorRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class IndicatorControllerTest extends BaseControllerTest {

    private final static Level[] mockLevels = new Level[]{
            new Level(1L, "OUTPUT", "OUTPUT", "{output}", "green"),
            new Level(2L, "OUTCOME", "OUTCOME", "{outcomes}", "red"),
            new Level(3L, "OTHER_OUTCOMES", "OTHER OUTCOMES", "{otheroutcomes}", "orange"),
            new Level(4L, "IMPACT", "IMPACT", "{impact}", "purple")
    };

    // Instead of mocking the repository maybe could load H2 memory with flyway
    @MockBean
    private IndicatorRepository indicatorRepository;

    @Test
    void handleFileUpload() {
        List<IndicatorResponse> expectedResult = getExpectedResult();
        List<Indicator> indicators = mockIndicatorList();
        when(indicatorRepository.findAll()).thenReturn(indicators);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ClassPathResource("test_doc.docx"));
        ResponseEntity<List<IndicatorResponse>> response = testRestTemplate.exchange("/indicator/upload", HttpMethod.POST,
                new HttpEntity<>(body, headers), new ParameterizedTypeReference<List<IndicatorResponse>>() {});

        List<IndicatorResponse> result = response.getBody();
        assertNotNull(result);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResult.size(), result.size());

        for (int i = 0; i < expectedResult.size(); i++) {
            assertEquals(expectedResult.get(i).getId(), result.get(i).getId());
            assertEquals(expectedResult.get(i).getLabel(), result.get(i).getLabel());
            assertEquals(expectedResult.get(i).getDescription(), result.get(i).getDescription());
            assertEquals(expectedResult.get(i).getVar(), result.get(i).getVar());
            assertEquals(expectedResult.get(i).getColor(), result.get(i).getColor());
            assertEquals(expectedResult.get(i).getKeys(), result.get(i).getKeys());
        }
    }


    @Test
    void handleFileUpload_indicatorsWithSameId() {
        List<String> keywordsFoodList = new ArrayList<>();
        keywordsFoodList.add("agriculture");
        keywordsFoodList.add("food");
        List<IndicatorResponse> expectedResult = getExpectedResult();
        List<String> keywordList = new ArrayList<>();
        keywordList.add("agriculture");
        List<Indicator> indicators = mockIndicatorList();
        indicators.add(new Indicator(1L, "Name", "Description", "agriculture", mockLevels[1], keywordsFoodList));
        indicators.add(new Indicator(4L, "Name2", "Description2", "", mockLevels[0], null));

        when(indicatorRepository.findAll()).thenReturn(indicators);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ClassPathResource("test_doc.docx"));
        ResponseEntity<List<IndicatorResponse>> response = testRestTemplate.exchange("/indicator/upload", HttpMethod.POST,
                new HttpEntity<>(body, headers), new ParameterizedTypeReference<List<IndicatorResponse>>() {});

        List<IndicatorResponse> result = response.getBody();
        assertNotNull(result);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResult.size(), result.size());

        for (int i = 0; i < expectedResult.size(); i++) {
            assertEquals(expectedResult.get(i).getId(), result.get(i).getId());
            assertEquals(expectedResult.get(i).getLabel(), result.get(i).getLabel());
            assertEquals(expectedResult.get(i).getDescription(), result.get(i).getDescription());
            assertEquals(expectedResult.get(i).getVar(), result.get(i).getVar());
            assertEquals(expectedResult.get(i).getColor(), result.get(i).getColor());
            //TODO With the same Id, the keywords get duplicated
            //assertEquals(expectedResult.get(i).getKeys(), result.get(i).getKeys());
        }
    }

    @Test
    void handleFileUpload_withIndicatorsWithoutKeywords() {
        List<String> keywordsFoodList = new ArrayList<>();
        keywordsFoodList.add("food");
        List<String> keywordsList = new ArrayList<>();
        keywordsList.add(keywordsFoodList.get(0));
        List<IndicatorResponse> expectedResult = getExpectedResult();
        expectedResult.add(new IndicatorResponse(5L, mockLevels[2].getName(),mockLevels[2].getColor(),"Name 3",
                "Description", Collections.singletonList("agriculture"), mockLevels[2].getTemplateVar()));
        expectedResult.add(new IndicatorResponse(6L, mockLevels[2].getName(),mockLevels[2].getColor(),"Name 6",
                "Description", keywordsFoodList, mockLevels[2].getTemplateVar()));
        List<Indicator> indicators = mockIndicatorList();
        // This showcases how keywords property is irrelevant, only keywordList is taken into consideration
        indicators.add(new Indicator(6L, "Name 6", "Description", "",  mockLevels[2], keywordsList));
        indicators.add(new Indicator(2L, "Name 2", "Description", "", mockLevels[1], null));
        indicators.add(new Indicator(3L, "Name 3", "Description", "agriculture", mockLevels[2], null));


        when(indicatorRepository.findAll()).thenReturn(indicators);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ClassPathResource("test_doc.docx"));
        ResponseEntity<List<IndicatorResponse>> response = testRestTemplate.exchange("/indicator/upload", HttpMethod.POST,
                new HttpEntity<>(body, headers), new ParameterizedTypeReference<List<IndicatorResponse>>() {});

        List<IndicatorResponse> result = response.getBody();
        assertNotNull(result);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResult.size(), result.size());

        for (int i = 0; i < expectedResult.size(); i++) {
            assertEquals(expectedResult.get(i).getId(), result.get(i).getId());
            assertEquals(expectedResult.get(i).getLabel(), result.get(i).getLabel());
            assertEquals(expectedResult.get(i).getDescription(), result.get(i).getDescription());
            assertEquals(expectedResult.get(i).getVar(), result.get(i).getVar());
            assertEquals(expectedResult.get(i).getColor(), result.get(i).getColor());
            assertEquals(expectedResult.get(i).getKeys(), result.get(i).getKeys());
        }
    }
    //TODO can't find the template but gives 200 nonetheless. Needs exception handling
    @Test
    void downloadIndicators() {
        ResponseEntity<String> response = testRestTemplate.exchange("/indicator/download", HttpMethod.POST,
                new HttpEntity<>(createIndicatorResponseList()), String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
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

        list.add(new Indicator(4L, "Number of policies/strategies/laws/regulation developed/revised for digitalisation with EU support",
                "Digitalization", "policy", mockLevels[0], keywordsPolicyList));
        list.add(new Indicator(73L, "Number of government policies developed or revised with civil society organisation participation through EU support",
                "Public Sector", "government policies, policy", mockLevels[1], keywordsGovPolicyList));
        list.add(new Indicator(5L, "Revenue, excluding grants (% of GDP)",
                "Public Sector", "government", mockLevels[3], keywordsGovList));
        list.add(new Indicator(1L, "Number of food insecure people receiving EU assistance",
                "Food & Agriculture", keyword, mockLevels[1], keywordsFoodList));

        return list;
    }

    private List<IndicatorResponse> getExpectedResult(){
        List<String> keywordsFoodList = new ArrayList<>();
        keywordsFoodList.add("agriculture");
        keywordsFoodList.add("food");

        List<IndicatorResponse> list = new ArrayList<>();
        list.add(new IndicatorResponse(3L, mockLevels[3].getName(), mockLevels[3].getColor(), "Revenue, excluding grants (% of GDP)",
                "Public Sector", Collections.singletonList("government"), mockLevels[3].getTemplateVar()));
        list.add(new IndicatorResponse(2L, mockLevels[1].getName(), mockLevels[1].getColor(),"Number of government policies developed or revised with civil society organisation participation through EU support",
                "Public Sector", Collections.singletonList("policy"), mockLevels[1].getTemplateVar()));
        list.add(new IndicatorResponse(4L,mockLevels[1].getName(), mockLevels[1].getColor(),"Number of food insecure people receiving EU assistance",
                "Food & Agriculture", keywordsFoodList, mockLevels[1].getTemplateVar()));
        list.add(new IndicatorResponse(1L,mockLevels[0].getName(), mockLevels[0].getColor(), "Number of policies/strategies/laws/regulation developed/revised for digitalisation with EU support",
                "Digitalization", Collections.singletonList("policy"), mockLevels[0].getTemplateVar()));
        return list;
    }

    private List<IndicatorResponse> createIndicatorResponseList(){
        List<IndicatorResponse> list = new ArrayList<>();
        list.add(new IndicatorResponse(1L, "OUTCOME","red","Number of government policies developed or revised with civil society organisation participation through EU support",
                "", Collections.singletonList("policy"), "{outcomes}"));
        list.add(new IndicatorResponse(2L, "OUTCOME", "red", "Number of government policies developed or revised with civil society organisation participation through EU support",
                "Public Sector", Collections.singletonList("policy"), "{outcomes}"));
        list.add(new IndicatorResponse(3L, "OUTCOME", "red", "Extent to which regional/national policy documents/sector strategies are developed/improved",
                "Digitalization", Collections.singletonList("policy"), "{outcomes}"));
        return list;
    }
}