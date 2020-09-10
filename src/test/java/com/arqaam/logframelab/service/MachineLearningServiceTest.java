package com.arqaam.logframelab.service;

import com.arqaam.logframelab.exception.InvalidDataSourceException;
import com.arqaam.logframelab.exception.WorldBankAPIRequestFailedException;
import com.arqaam.logframelab.model.WorldBankIndicator;
import com.arqaam.logframelab.model.persistence.Indicator;
import org.assertj.core.util.Strings;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(profiles = "integration")
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
}