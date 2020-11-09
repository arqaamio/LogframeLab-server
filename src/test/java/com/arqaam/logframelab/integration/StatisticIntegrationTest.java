package com.arqaam.logframelab.integration;

import com.arqaam.logframelab.model.persistence.Statistic;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class StatisticIntegrationTest extends BaseIntegrationTest {

    @Test
    void retrieveAllStatistics() {
        ResponseEntity<List<Statistic>> response = testRestTemplate
                .exchange("/statistic", HttpMethod.GET,
                        new HttpEntity<>(null), new ParameterizedTypeReference<List<Statistic>>() {
                        });
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }
}
