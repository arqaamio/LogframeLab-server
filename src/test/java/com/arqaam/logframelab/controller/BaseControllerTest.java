package com.arqaam.logframelab.controller;

import com.arqaam.logframelab.controller.dto.auth.AuthenticateUserRequestDto;
import com.arqaam.logframelab.controller.dto.auth.JwtAuthenticationTokenResponse;
import com.arqaam.logframelab.model.Error;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(profiles = "integration")
public class BaseControllerTest {

  private static final String SEC_ADMIN_USERNAME = "secadmin";
  private static final String SEC_ADMIN_PASSWORD = "password";

  static String bearerToken = null;

  @Autowired
  protected TestRestTemplate testRestTemplate;

  void assertEqualsException(
      ResponseEntity<Error> response, HttpStatus httpStatus, Integer code, Class exception) {
    assertEquals(httpStatus, response.getStatusCode());
    assertEquals(code, response.getBody().getCode());
    assertEquals(exception.getSimpleName(), response.getBody().getException());
  }

  String getAuthToken() {
    ResponseEntity<JwtAuthenticationTokenResponse> tokenResponseEntity =
        testRestTemplate.postForEntity(
            "/auth/login",
            new AuthenticateUserRequestDto(SEC_ADMIN_USERNAME, SEC_ADMIN_PASSWORD),
            JwtAuthenticationTokenResponse.class);

    JwtAuthenticationTokenResponse tokenResponse = tokenResponseEntity.getBody();
    assert tokenResponse != null;
    return tokenResponse.getToken();
  }
}
