package com.arqaam.logframelab.controller;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class IndicatorControllerTest extends BaseControllerTest {

//    @Before
    public void setup() {

    }

    @Test
    void handleFileUpload() throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
//        MultipartFile file = new MockMultipartFile("indicatorsExportTemplate", "indicatorsExportTemplate.docx", headers.getContentType().toString(), "coisas".getBytes());
        ClassPathResource classPathResource = new ClassPathResource("indicatorsExportTemplate.docx");
        if (classPathResource.getFile() == null){
            throw new RuntimeException();
        }
        MultiValueMap<String, Object> body
                = new LinkedMultiValueMap<>();
        body.add("file", classPathResource);
        ResponseEntity<String> response = testRestTemplate.exchange("/indicator/upload", HttpMethod.POST,
                new HttpEntity<>(body, headers), String.class);

        assertNotNull(response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void downloadIndicators() {
    }
}