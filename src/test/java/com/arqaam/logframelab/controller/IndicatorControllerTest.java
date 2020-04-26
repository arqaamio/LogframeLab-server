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

    // Instead of mocking the repository maybe could load H2 memory with flyway
    @MockBean
    private IndicatorRepository indicatorRepository;

    @Test
    void handleFileUpload() {
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
        assertEquals(4, result.size());

        for (int i = 0; i < 4; i++) {
            assertEquals(indicators.get(i).getName(), result.get(i).getLabel());
            assertEquals(indicators.get(i).getDescription(), result.get(i).getDescription());
            assertEquals(indicators.get(i).getLevel().getTemplateVar(), result.get(i).getVar());
        }
    }


    @Test
    void handleFileUpload_indicatorsWithSameId() {
        List<String> keywordList = new ArrayList<>();
        keywordList.add("agriculture");
        List<Indicator> indicators = mockIndicatorList();
        indicators.add(new Indicator(1L, "Name", "Description", "agriculture", new Level(), keywordList));
        indicators.add(new Indicator(4L, "Name2", "Description2", "", new Level(), null));

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
        assertEquals(4, result.size());

        for (int i = 0; i < 4; i++) {
            assertEquals(indicators.get(i).getName(), result.get(i).getLabel());
            assertEquals(indicators.get(i).getDescription(), result.get(i).getDescription());
            assertEquals(indicators.get(i).getLevel().getTemplateVar(), result.get(i).getVar());
        }
    }

    @Test
    void handleFileUpload_withIndicatorsWithoutKeywords() {
        List<String> keywordList = new ArrayList<>();
        keywordList.add("agriculture");
        List<Indicator> indicators = mockIndicatorList();
        // This showcases how keywords property is irrelevant, only keywordList is taken into consideration
        indicators.add(new Indicator(5L, "Name 5", "Description", "",
                new Level(1L, "OUTPUT", "OUTPUT", "{output}", "green"), keywordList));
        indicators.add(new Indicator(2L, "Name 2", "Description", "", new Level(), null));
        indicators.add(new Indicator(3L, "Name 3", "Description", "agriculture", new Level(), null));


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
        assertEquals(5, result.size());

        for (int i = 0; i < 4; i++) {
            assertEquals(indicators.get(i).getName(), result.get(i).getLabel());
            assertEquals(indicators.get(i).getDescription(), result.get(i).getDescription());
            assertEquals(indicators.get(i).getLevel().getTemplateVar(), result.get(i).getVar());
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
        Level level1 = new Level(1L, "OUTPUT", "OUTPUT", "{output}", "green");
        Level level2 = new Level(2L, "OUTCOME", "OUTCOME", "{outcomes}", "red");
        Level level3 = new Level(3L, "OTHER_OUTCOMES", "OTHER OUTCOMES", "{otheroutcomes}", "orange");
        Level level4 = new Level(4L, "IMPACT", "IMPACT", "{impact}", "purple");
        String keyword = "food insecurity,nutrition,farming,agriculture";
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

        list.add(new Indicator(4L,"Number of policies/strategies/laws/regulation developed/revised for digitalisation with EU support",
                "Digitalization", "policy", level1, keywordsPolicyList));
        list.add(new Indicator(73L, "Number of government policies developed or revised with civil society organisation participation through EU support",
                "Public Sector", "government policies, policy", level2, keywordsGovPolicyList));
        list.add(new Indicator(5L, "Revenue, excluding grants (% of GDP)",
                "Public Sector", "government", level4, keywordsGovList));
        list.add(new Indicator(1L,"Number of food insecure people receiving EU assistance",
                "Food & Agriculture", keyword, level2, keywordsFoodList));
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