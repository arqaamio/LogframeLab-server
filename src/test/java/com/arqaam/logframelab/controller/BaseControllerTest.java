package com.arqaam.logframelab.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.arqaam.logframelab.controller.dto.auth.login.AuthenticateUserRequestDto;
import com.arqaam.logframelab.controller.dto.auth.login.JwtAuthenticationTokenResponse;
import com.arqaam.logframelab.model.Error;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(profiles = "integration")
@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)
public class BaseControllerTest {

  private static final String SEC_ADMIN_USERNAME = "secadmin";
  private static final String SEC_ADMIN_PASSWORD = "password";

  protected String bearerToken;

  protected final HttpEntity defaultHttpEntity = new HttpEntity(new HttpHeaders());

  @Autowired
  protected TestRestTemplate testRestTemplate;

  void assertEqualsException(ResponseEntity<Error> response, HttpStatus httpStatus, Integer code,
      Class exception) {
    assertEquals(httpStatus, response.getStatusCode());
    assertEquals(code, response.getBody().getCode());
    assertEquals(exception.getSimpleName(), response.getBody().getException());
  }

  protected void generateAuthToken() {
    ResponseEntity<JwtAuthenticationTokenResponse> tokenResponseEntity =
        testRestTemplate.postForEntity(
            "/auth/login",
            new AuthenticateUserRequestDto(SEC_ADMIN_USERNAME, SEC_ADMIN_PASSWORD),
            JwtAuthenticationTokenResponse.class);

    JwtAuthenticationTokenResponse tokenResponse = tokenResponseEntity.getBody();
    assert tokenResponse != null;
    bearerToken = tokenResponse.getToken();
  }
}
