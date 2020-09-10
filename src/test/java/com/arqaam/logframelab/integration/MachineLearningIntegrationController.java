package com.arqaam.logframelab.integration;

import static org.junit.jupiter.api.Assertions.*;

import com.arqaam.logframelab.controller.BaseControllerTest;

import java.util.List;

import com.arqaam.logframelab.model.SimilarityResponse;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class MachineLearningIntegrationController extends BaseControllerTest {

  @Test
  void getSimilarIndicators() {
    ResponseEntity<List<SimilarityResponse>> response = testRestTemplate

            .exchange("/ml/similarity?threshold=0.8", HttpMethod.GET,
                    defaultHttpEntity, new ParameterizedTypeReference<>(){});

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertFalse(response.getBody().isEmpty());
    response.getBody().forEach(x -> {
      assertNotNull(x);
      assertNotNull(x.getIndicator());
      assertFalse(x.getSimilarIndicators().isEmpty());
    });
  }
}

