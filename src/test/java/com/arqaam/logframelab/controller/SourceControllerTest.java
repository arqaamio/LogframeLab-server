package com.arqaam.logframelab.controller;

import com.arqaam.logframelab.model.persistence.Source;
import com.arqaam.logframelab.service.SourceService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class SourceControllerTest extends BaseControllerTest {

    @MockBean
    private SourceService sourceService;
    @InjectMocks
    private SourceController sourceController;

    @Test
    void retrieveAllSources() {
        List<Source> expected = new ArrayList<>();
        expected.add(new Source(1L,"Fake Source 1"));
        expected.add(new Source(2L,"Fake Source 2"));
        when(sourceService.getSources()).thenReturn(expected);
        ResponseEntity<List<Source>> response = testRestTemplate
                .exchange("/source", HttpMethod.GET,
                        new HttpEntity<>(null), new ParameterizedTypeReference<List<Source>>() {
                        });
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(expected, response.getBody());
    }

    @Test
    void retrieveSourceById() {
        Source expected = new Source(1L, "Fake Source 1");
        when(sourceService.getSourceById(1L)).thenReturn(expected);
        ResponseEntity<Source> response = testRestTemplate
                .exchange("/source/1", HttpMethod.GET,
                        new HttpEntity<>(null), Source.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(expected, response.getBody());
    }

    @Test
    void createSource() {
        Source expected = new Source(1L, "Fake Source 1");
        when(sourceService.createSource("Fake Source 1")).thenReturn(expected);
        ResponseEntity<Source> response = testRestTemplate
                .exchange("/source", HttpMethod.POST,
                        new HttpEntity<>(expected, new HttpHeaders()), Source.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(expected, response.getBody());
    }

    @Test
    void updateSource() {
        Source expected = new Source(1L, "Fake Source 2");
        when(sourceService.updateSource(1L, "Fake Source 2")).thenReturn(expected);
        ResponseEntity<Source> response = testRestTemplate
                .exchange("/source", HttpMethod.PUT,
                        new HttpEntity<>(expected, new HttpHeaders()), Source.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(expected, response.getBody());
    }

    @Test
    void deleteSource() {
        Source expected = new Source(1L, "Fake Source 1");
        when(sourceService.deleteSourceById(1L)).thenReturn(expected);
        ResponseEntity<Source> response = testRestTemplate
                .exchange("/source/1", HttpMethod.DELETE,
                        new HttpEntity<>(null), Source.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(expected, response.getBody());
    }
}