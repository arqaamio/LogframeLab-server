package com.arqaam.logframelab.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.arqaam.logframelab.model.MLScanIndicatorResponse;
import com.arqaam.logframelab.model.MLStatementQualityRequest;
import com.arqaam.logframelab.model.MLStatementResponse;
import com.arqaam.logframelab.model.MLScanIndicatorResponse.MLScanIndicator;
import com.arqaam.logframelab.model.MLStatementResponse.MLStatement;
import com.arqaam.logframelab.model.persistence.Indicator;
import com.arqaam.logframelab.service.IndicatorService;
import com.arqaam.logframelab.service.MachineLearningService;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

public class MachineLearningControllerTest extends BaseControllerTest {
    
    @MockBean
    private IndicatorService indicatorService;

    @MockBean
    private MachineLearningService machineLearningService;
    
    @Test
    void scanForIndicators() {
        String indicatorName = "Indicator 1";
        String indicatorName2 = "Indicator 2";
        MLScanIndicator indicator1 = new MLScanIndicator(indicatorName, 1L, new MLScanIndicatorResponse.MLSearchResult(38.123456));
        MLScanIndicator indicator2 = new MLScanIndicator(indicatorName2, 2L, new MLScanIndicatorResponse.MLSearchResult(40.123456));
        
        List<MLScanIndicator> mlIndicators = new ArrayList<>();
        mlIndicators.add(indicator1);
        mlIndicators.add(indicator2);
        
        List<Indicator> indicators = new ArrayList<>();
        indicators.add(Indicator.builder().id(1L).name(indicatorName).build());
        indicators.add(Indicator.builder().id(2L).name(indicatorName2).build());
        
        when(machineLearningService.scanForIndicators(any(), any())).thenReturn(mlIndicators);
        when(indicatorService.getIndicatorWithId(any())).thenReturn(indicators);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ClassPathResource("test_doc.docx"));

        ResponseEntity<List<Indicator>> response = testRestTemplate
                .exchange("/ml/indicators", HttpMethod.POST,
                        new HttpEntity<>(body, headers), new ParameterizedTypeReference<List<Indicator>>(){});

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(indicators.size(), response.getBody().size());
        assertEquals(indicatorName, response.getBody().get(0).getName());
        assertEquals(38, response.getBody().get(0).getScore());
        assertEquals(indicatorName2, response.getBody().get(1).getName());
        assertEquals(40, response.getBody().get(1).getScore());
    }

    @Test
    void scanForStatements() {
        List<MLStatement> impactStatements = new ArrayList<>();
        impactStatements.add(new MLStatement("Statement 1", "good", 90.123456));
        List<MLStatement> outcomeStatements = new ArrayList<>();
        outcomeStatements.add(new MLStatement("Statement 2", "good", 80.123456));
        outcomeStatements.add(new MLStatement("Statement 3", "good", 70.123456));
        List<MLStatement> outputStatements = new ArrayList<>();
        outputStatements.add(new MLStatement("Statement 4", "bad", 40.123456));

        MLStatementResponse expected = new MLStatementResponse(impactStatements, outcomeStatements, outputStatements);
        when(machineLearningService.scanForStatements(any())).thenReturn(expected);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ClassPathResource("test_doc.docx"));

        ResponseEntity<MLStatementResponse> response = testRestTemplate
                .exchange("/ml/statements", HttpMethod.POST,
                        new HttpEntity<>(body, headers), MLStatementResponse.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(expected, response.getBody());
    }
  
    @Test
    void statementQualityCheck() {
        String statement = "this is an unit test";
        String level = "impact";
        Double score = 90.1;
        String status = "good";
        MLStatementQualityRequest body = new MLStatementQualityRequest(statement, level);
        
        when(machineLearningService.qualityCheckStatement(body)).thenReturn(new MLStatement(statement, status, score));
        ResponseEntity<MLStatement> response = testRestTemplate
                .exchange("/ml/statement-quality", HttpMethod.POST,
                        new HttpEntity<>(body, new HttpHeaders()),MLStatement.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(score, response.getBody().getScore());
        assertEquals(statement, response.getBody().getStatement());
        assertEquals(status, response.getBody().getStatus());
    }
}
