package com.arqaam.logframelab.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.arqaam.logframelab.model.Error;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(profiles = "integration")
@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)
public class BaseControllerTest {

  @Autowired
  protected TestRestTemplate testRestTemplate;

  void assertEqualsException(ResponseEntity<Error> response, HttpStatus httpStatus, Integer code,
      Class exception) {
    assertEquals(httpStatus, response.getStatusCode());
    assertEquals(code, response.getBody().getCode());
    assertEquals(exception.getSimpleName(), response.getBody().getException());
  }
}
