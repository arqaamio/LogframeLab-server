package com.arqaam.logframelab.controller;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;

import com.arqaam.logframelab.controller.dto.FiltersDto;
import com.arqaam.logframelab.controller.dto.auth.create.UserAuthProvisioningRequestDto;
import com.arqaam.logframelab.controller.dto.auth.create.UserAuthProvisioningResponseDto;
import com.arqaam.logframelab.model.IndicatorResponse;

import java.util.*;
import java.util.stream.Collectors;

import com.arqaam.logframelab.model.persistence.*;
import org.apache.commons.collections4.map.MultiKeyMap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class IndicatorManagementControllerIntegrationTest extends BaseControllerTest {

    private static final String USERNAME = "indicator";
    private static final String PASSWORD = "indicatorPassword";

    @BeforeAll
    void setupClass(){
        UserAuthProvisioningRequestDto body = new UserAuthProvisioningRequestDto(USERNAME, PASSWORD, Arrays.asList(1,2,3));

        generateAuthToken();
        ResponseEntity<UserAuthProvisioningResponseDto> response = testRestTemplate
                .exchange("/auth/users", HttpMethod.POST,
                        new HttpEntity<>(body, headersWithAuth()), new ParameterizedTypeReference<>() {});
        generateAuthToken(USERNAME, PASSWORD);
    }

    @Test
    void getIndicators() {
        ResponseEntity<ResponsePage<Indicator>> response = testRestTemplate
                .exchange("/indicators?filters.indicatorName=NUMBER&filters.sectors=Poverty&page=1&pageSize=10", HttpMethod.GET,
                        defaultHttpEntity, new ParameterizedTypeReference<>() {});
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(5,response.getBody().getTotalElements());
        assertEquals(5,response.getBody().getContent().size());
        assertTrue(response.getBody().getContent().stream().anyMatch(i -> i.getName().contains("Number")));
        assertTrue(response.getBody().getContent().stream().allMatch(i -> i.getName().toLowerCase().contains("number")));
        assertTrue(response.getBody().getContent().stream().allMatch(i -> i.getSector().toLowerCase().contains("poverty")));
    }

    @Test
    void getIndicator() {
        ResponseEntity<Indicator> response = testRestTemplate
                .exchange("/indicators/47", HttpMethod.GET,
                        defaultHttpEntity, Indicator.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        assertEquals(47, response.getBody().getId());
        assertEquals("Adequacy of social protection and labor programs (% of total welfare of beneficiary households)", response.getBody().getName());
        assertEquals("https://data.worldbank.org/indicator/per_allsp.adq_pop_tot?view=chart", response.getBody().getDataSource());
        assertEquals("social security,social protection,social insurance,labor,labour,labor protection,labour protection,social welfare", response.getBody().getKeywords());
        assertEquals("Social Protection & Labour", response.getBody().getSector());
        assertEquals("World Bank Data", response.getBody().getSourceVerification());
        assertTrue(response.getBody().getSource().stream().allMatch(x->x.getName().equals("World Bank")));
        assertEquals(false, response.getBody().getDisaggregation());
        assertEquals(4, response.getBody().getLevel().getId());
        assertTrue(response.getBody().getCrsCode().isEmpty());
        assertTrue(response.getBody().getSdgCode().isEmpty());
        assertEquals("", response.getBody().getDescription());
    }
}
