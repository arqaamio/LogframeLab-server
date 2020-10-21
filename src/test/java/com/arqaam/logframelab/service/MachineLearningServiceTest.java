package com.arqaam.logframelab.service;

import com.arqaam.logframelab.exception.MLAPIRequestFailedException;
import com.arqaam.logframelab.model.MLStatementQualityRequest;
import com.arqaam.logframelab.model.MLStatementResponse;
import com.arqaam.logframelab.model.MLStatementResponse.MLStatement;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(value = "test")
class MachineLearningServiceTest {

    @Autowired
    private MachineLearningService machineLearningService;
    
    @Test
    void getSimilarIndicators() {
        String indicatorName = "Revenue, excluding grants (% of GDP)";
        List<String> result = machineLearningService.getSimilarIndicators(indicatorName, 0.8);
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void getSimilarIndicators_noResults() {
        String indicatorName = "Number of food insecure people receiving EU assistance";
        List<String> result = machineLearningService.getSimilarIndicators(indicatorName, 0.8);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void scanForIndicators() {
        String text = "agriculture, women, poverty";
        List<List<String>> result = machineLearningService.scanForIndicators(text);
        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (List<String> list : result) {
            assertFalse(list.get(0)==null || list.get(0).isEmpty() || list.get(0).isBlank());
            assertFalse(list.get(1)==null);
            assertTrue(Double.parseDouble(list.get(1))> -1 && Double.parseDouble(list.get(1)) <= 100);
        }
    }

    @Test
    void scanForStatements() {
        String text = "agriculture, women, poverty";
        MLStatementResponse result = machineLearningService.scanForStatements(text);
        assertNotNull(result);
        assertNotNull(result.getImpact());
        assertNotNull(result.getOutcome());
        assertNotNull(result.getOutput());
        
        assertEquals(0, result.getImpact().size());
        assertEquals(0, result.getOutcome().size());
        assertEquals(0, result.getOutput().size());
    }

    @Test
    void qualityCheckStatement() {
        String statement = "agriculture, women, poverty";
        String level = "impact";
        MLStatementQualityRequest request = new MLStatementQualityRequest(statement, level);
        MLStatement result = machineLearningService.qualityCheckStatement(request);
        assertNotNull(result);
        assertNotNull(result.getScore());
        // assertEquals(statement, result.getStatement());
        assertTrue(result.getStatus().matches("good|bad"));
    }

    @Test
    void qualityCheckStatement_wrongLevel() {
        String statement = "agriculture, women, poverty";
        String level = "level";
        MLStatementQualityRequest request = new MLStatementQualityRequest(statement, level);
        assertThrows(MLAPIRequestFailedException.class, () -> machineLearningService.qualityCheckStatement(request));
    }
}