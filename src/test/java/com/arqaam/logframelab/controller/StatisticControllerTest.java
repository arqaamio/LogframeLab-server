package com.arqaam.logframelab.controller;

import com.arqaam.logframelab.model.persistence.Statistic;
import com.arqaam.logframelab.service.StatisticService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class StatisticControllerTest extends BaseControllerTest {

    @MockBean
    private StatisticService statisticService;

    @Test
    void retrieveAllStatistics() {
        List<Statistic> expected = new ArrayList<>();
        expected.add(new Statistic(1L,1,1,1,2, null));
        expected.add(new Statistic(2L,0,1,1,1, null));
        when(statisticService.getAllStatistics()).thenReturn(expected);
        ResponseEntity<List<Statistic>> response = testRestTemplate
                .exchange("/statistic", HttpMethod.GET,
                        new HttpEntity<>(null), new ParameterizedTypeReference<List<Statistic>>() {
                        });
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(expected, response.getBody());
    }
}