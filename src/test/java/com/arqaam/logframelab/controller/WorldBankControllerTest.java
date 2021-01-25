package com.arqaam.logframelab.controller;

import com.arqaam.logframelab.exception.IndicatorNotFoundException;
import com.arqaam.logframelab.exception.InvalidDataSourceException;
import com.arqaam.logframelab.exception.WorldBankAPIRequestFailedException;
import com.arqaam.logframelab.model.Error;
import com.arqaam.logframelab.model.WorldBankIndicator;
import com.arqaam.logframelab.model.persistence.Indicator;
import com.arqaam.logframelab.repository.IndicatorRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class WorldBankControllerTest extends BaseControllerTest {

    @MockBean
    private IndicatorRepository indicatorRepository;

    @Test
    void getCountry() {
        ResponseEntity<Map<String, String>> response = testRestTemplate.exchange("/worldbank/country", HttpMethod.GET,
                null, new ParameterizedTypeReference<Map<String, String>>() {});
        assertEquals(HttpStatus.OK,response.getStatusCode());
        assertNotNull(response.getBody());
//        assertEquals(302, response.getBody().size());
        assertTrue(response.getBody().containsKey("NZL"));
        assertTrue(response.getBody().containsValue("New Zealand"));
    }

    @Test
    void getIndicatorValues() {
        when(indicatorRepository.findById(any())).thenReturn(Optional.of(Indicator.builder().dataSource("https://data.worldbank.org/indicator/EG.CFT.ACCS.ZS?view=chart").build()));
        ResponseEntity<List<WorldBankIndicator>> response = testRestTemplate.exchange("/worldbank/values?countryId=NZL&indicatorId=42", HttpMethod.GET,
                null, new ParameterizedTypeReference<List<WorldBankIndicator>>() {});
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().stream().anyMatch(x -> x == null || !x.getValue().equals("100")));

        for (int i = 0; i < response.getBody().size(); i++) {
            assertEquals(response.getBody().get(i).getDate(), String.valueOf(2000+i));
        }
    }

    @Test
    void getIndicatorValues_withDates() {
        when(indicatorRepository.findById(any())).thenReturn(Optional.of(Indicator.builder().dataSource("https://data.worldbank.org/indicator/EG.CFT.ACCS.ZS?view=chart").build()));
        ResponseEntity<List<WorldBankIndicator>> response = testRestTemplate.exchange("/worldbank/values?countryId=NZL&indicatorId=42&years=2000,2001", HttpMethod.GET,
                null, new ParameterizedTypeReference<List<WorldBankIndicator>>() {});
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertFalse(response.getBody().stream().anyMatch(x -> x == null || !x.getValue().equals("100")));

        for (int i = 0; i < response.getBody().size(); i++) {
            assertEquals(response.getBody().get(i).getDate(), String.valueOf(2000+i));
        }
    }

    @Test
    void getIndicatorValues_indicatorDoesntExist() {
        when(indicatorRepository.findById(any())).thenReturn(Optional.empty());
        ResponseEntity<Error> response = testRestTemplate.exchange("/worldbank/values?countryId=NZL&indicatorId=0", HttpMethod.GET,
                null, Error.class);
        assertEqualsException(response, HttpStatus.NOT_FOUND, 10, IndicatorNotFoundException.class);
    }

    @Test
    void getIndicatorValues_indicatorMissingDataSource() {
        when(indicatorRepository.findById(any())).thenReturn(Optional.of(Indicator.builder().build()));
        ResponseEntity<Error> response = testRestTemplate.exchange("/worldbank/values?countryId=NZL&indicatorId=42", HttpMethod.GET,
                null, Error.class);
        assertEqualsException(response, HttpStatus.UNPROCESSABLE_ENTITY, 13, InvalidDataSourceException.class);
    }

    @Test
    void getIndicatorValues_indicatorDataSourceDoesntMatch() {
        when(indicatorRepository.findById(any())).thenReturn(Optional.of(Indicator.builder().dataSource("https://randomwebsite.com").build()));
        ResponseEntity<Error> response = testRestTemplate.exchange("/worldbank/values?countryId=NZL&indicatorId=42", HttpMethod.GET,
                null, Error.class);
        assertEqualsException(response, HttpStatus.UNPROCESSABLE_ENTITY, 13, InvalidDataSourceException.class);
    }

    @Test
    void getIndicatorValues_wrongCountryId() {
        when(indicatorRepository.findById(any())).thenReturn(Optional.of(Indicator.builder().dataSource("https://data.worldbank.org/indicator/EG.CFT.ACCS.ZS?view=chart").build()));
        ResponseEntity<Error> response = testRestTemplate.exchange("/worldbank/values?countryId=AAA&indicatorId=42", HttpMethod.GET,
                null, Error.class);
        assertEqualsException(response, HttpStatus.INTERNAL_SERVER_ERROR, 11, WorldBankAPIRequestFailedException.class);
    }

    @Test
    void getIndicatorValues_wrongIndicatorId() {
        when(indicatorRepository.findById(any())).thenReturn(Optional.of(Indicator.builder().dataSource("https://data.worldbank.org/indicator/AAA.AAAA?view=chart").build()));
        ResponseEntity<Error> response = testRestTemplate.exchange("/worldbank/values?countryId=NZL&indicatorId=42", HttpMethod.GET,
                null, Error.class);
        assertEqualsException(response, HttpStatus.INTERNAL_SERVER_ERROR, 11, WorldBankAPIRequestFailedException.class);
    }
}