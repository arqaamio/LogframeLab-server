package com.arqaam.logframelab.controller;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.arqaam.logframelab.model.persistence.Indicator;
import com.arqaam.logframelab.repository.initializer.BaseDatabaseTest;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.hamcrest.Matchers.is;

public class IndicatorsManagementControllerTest extends BaseControllerTest implements
    BaseDatabaseTest {

  @Test
  void whenIndicatorsByPageRequested_thenVerifyResult() {
    Map<String, Object> requestParams = new HashMap<>();
    ResponseEntity<Page<Indicator>> indicatorsPerPage = testRestTemplate
        .exchange("/indicators", HttpMethod.GET, null,
            new ParameterizedTypeReference<Page<Indicator>>() {
            }, requestParams);

    assertAll(
        () -> assertThat(indicatorsPerPage.getStatusCode(), is(HttpStatus.OK))
    );
  }
}
