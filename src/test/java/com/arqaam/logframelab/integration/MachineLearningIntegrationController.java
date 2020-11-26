package com.arqaam.logframelab.integration;

import static org.junit.jupiter.api.Assertions.*;

import com.arqaam.logframelab.controller.BaseControllerTest;

import java.util.List;

import com.arqaam.logframelab.model.MLStatementQualityRequest;
import com.arqaam.logframelab.model.MLStatementResponse;
import com.arqaam.logframelab.model.SimilarityResponse;
import com.arqaam.logframelab.model.MLStatementResponse.MLStatement;
import com.arqaam.logframelab.model.persistence.Indicator;

import org.junit.jupiter.api.Test;
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

public class MachineLearningIntegrationController extends BaseControllerTest {

  @Test
  void getSimilarIndicators() {
    ResponseEntity<List<SimilarityResponse>> response = testRestTemplate
            .exchange("/ml/similarity?threshold=0.8", HttpMethod.GET,
                    new HttpEntity<>(new HttpHeaders()),new ParameterizedTypeReference<List<SimilarityResponse>>(){});

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertFalse(response.getBody().isEmpty());
    response.getBody().forEach(x -> {
      assertNotNull(x);
      assertNotNull(x.getIndicator());
      //assertFalse(x.getSimilarIndicators().isEmpty());
    });
  }

  @Test
  void scanForIndicators() {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);

    MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
    body.add("file", new ClassPathResource("test_doc.docx"));

    ResponseEntity<List<Indicator>> response = testRestTemplate
            .exchange("/ml/indicators", HttpMethod.POST,
                    new HttpEntity<>(body, headers), new ParameterizedTypeReference<List<Indicator>>(){});

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertFalse(response.getBody().isEmpty());
    for (Indicator indicator : response.getBody()) {
      assertNotNull(indicator);
      assertTrue(indicator.getScore()> -1 && indicator.getScore() <= 100);
    }
  }

  @Test
  void scanForStatements() {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);

    MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
    body.add("file", new ClassPathResource("test_doc.docx"));

    ResponseEntity<MLStatementResponse> response = testRestTemplate
            .exchange("/ml/statements", HttpMethod.POST,
                    new HttpEntity<>(body, headers), MLStatementResponse.class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertFalse(response.getBody().getImpact().isEmpty());
    assertFalse(response.getBody().getOutcome().isEmpty());
    assertFalse(response.getBody().getOutput().isEmpty());

    for (MLStatement statement : response.getBody().getImpact()) {
      assertNotNull(statement);
      assertNotNull(statement.getScore());
      assertTrue(statement.getStatus().matches("good|bad"));
    }
    
    for (MLStatement statement : response.getBody().getOutcome()) {
      assertNotNull(statement);
      assertNotNull(statement.getScore());
      assertTrue(statement.getStatus().matches("good|bad"));
    }
    
    for (MLStatement statement : response.getBody().getOutput()) {
      assertNotNull(statement);
      assertNotNull(statement.getScore());
      assertTrue(statement.getStatus().matches("good|bad"));
    }
  }

  @Test
  void statementQualityCheck() {
    String statement = "this is an integration test";
    String level = "impact";
    MLStatementQualityRequest body = new MLStatementQualityRequest(statement, level);
    ResponseEntity<MLStatement> response = testRestTemplate
            .exchange("/ml/statement-quality", HttpMethod.POST,
                    new HttpEntity<>(body, new HttpHeaders()),MLStatement.class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getScore());
    // assertEquals(statement, response.getBody().getStatement());
    assertTrue(response.getBody().getStatus().matches("good|bad"));
  }
  
}

