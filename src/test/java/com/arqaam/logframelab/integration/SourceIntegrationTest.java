package com.arqaam.logframelab.integration;

import com.arqaam.logframelab.model.persistence.Source;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class SourceIntegrationTest extends BaseIntegrationTest {

    private static final Source SOURCE_ID_1 = new Source(1L, "UN Sustainable Development Goals");

    @Test
    void retrieveAllSources() {
        Integer sizeSources = 48;
        ResponseEntity<List<Source>> response = testRestTemplate
                .exchange("/source", HttpMethod.GET,
                        new HttpEntity<>(null), new ParameterizedTypeReference<List<Source>>() {
                        });
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(sizeSources, response.getBody().size());
        for (Source source : response.getBody()) {
            assertNotNull(source.getId());
            assertNotNull(source.getName());
        }
    }

    @Test
    void retrieveSourceById() {
        ResponseEntity<Source> response = testRestTemplate
                .exchange("/source/1", HttpMethod.GET,
                        new HttpEntity<>(null), Source.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(SOURCE_ID_1, response.getBody());
    }

    @Test
    void createSource() {
        Source body = new Source(null, "Fake Source 1");
        Source expected = new Source(100L, body.getName());

        ResponseEntity<Source> response = testRestTemplate
                .exchange("/source", HttpMethod.POST,
                        new HttpEntity<>(body, new HttpHeaders()), Source.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(expected.getName(), response.getBody().getName());

        // Deleting newly created source
        ResponseEntity<Source> response2 = testRestTemplate
                .exchange("/source/"+response.getBody().getId(), HttpMethod.DELETE,
                        new HttpEntity<>(null), Source.class);
        assertEquals(HttpStatus.OK, response2.getStatusCode());
        assertNotNull(response2.getBody());
        assertEquals(expected.getName(), response2.getBody().getName());
    }

    @Test
    void updateSource() {
        Source expected = new Source(99L, "Fake Source 2");
        ResponseEntity<Source> response = testRestTemplate
                .exchange("/source", HttpMethod.PUT,
                        new HttpEntity<>(expected, new HttpHeaders()), Source.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(expected, response.getBody());

        // Putting the original value back
        Source expected2 = new Source(99L, "Other");
        ResponseEntity<Source> response2 = testRestTemplate
                .exchange("/source", HttpMethod.PUT,
                        new HttpEntity<>(expected2, new HttpHeaders()), Source.class);
        assertEquals(HttpStatus.OK, response2.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(expected2, response2.getBody());
    }

    @Test
    void deleteSource() {
        Source expected = new Source(99L, "Other");
        ResponseEntity<Source> response = testRestTemplate
                .exchange("/source/99", HttpMethod.DELETE,
                        new HttpEntity<>(null), Source.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(expected, response.getBody());

        // Create deleted source, although it will have a different id
        Source body = new Source(null, "Other");
        Source expected2 = new Source(100L, body.getName());

        ResponseEntity<Source> response2 = testRestTemplate
                .exchange("/source", HttpMethod.POST,
                        new HttpEntity<>(body, new HttpHeaders()), Source.class);
        assertEquals(HttpStatus.OK, response2.getStatusCode());
        assertNotNull(response2.getBody());
        assertEquals(expected2.getName(), response.getBody().getName());
    }
}
