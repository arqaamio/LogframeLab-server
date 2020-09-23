package com.arqaam.logframelab.integration;

import com.arqaam.logframelab.controller.dto.auth.login.AuthenticateUserRequestDto;
import com.arqaam.logframelab.controller.dto.auth.login.JwtAuthenticationTokenResponse;
import com.arqaam.logframelab.model.Error;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(profiles = "integration")
public class BaseIntegrationTest {

  protected static final String SEC_ADMIN_USERNAME = "secadmin";
  protected static final String SEC_ADMIN_PASSWORD = "Passw0rdArqaam1234@";

  protected String bearerToken;

  protected HttpEntity<?> defaultHttpEntity;

  @Autowired
  protected TestRestTemplate testRestTemplate;

  void assertEqualsException(ResponseEntity<Error> response, HttpStatus httpStatus, Integer code,
      Class<?> exception) {
    assertEquals(httpStatus, response.getStatusCode());
    assertEquals(code, response.getBody().getCode());
    assertEquals(exception.getSimpleName(), response.getBody().getException());
  }

  protected void generateAuthToken() {
    bearerToken = token();
  }

  protected String token(String... credentials) {
    AuthenticateUserRequestDto loginRequest =
        credentials.length > 0 ? new AuthenticateUserRequestDto(credentials[0], credentials[1])
            : new AuthenticateUserRequestDto(SEC_ADMIN_USERNAME, SEC_ADMIN_PASSWORD);

    ResponseEntity<JwtAuthenticationTokenResponse> tokenResponseEntity =
        testRestTemplate.postForEntity("/auth/login", loginRequest,
            JwtAuthenticationTokenResponse.class);

    JwtAuthenticationTokenResponse tokenResponse = tokenResponseEntity.getBody();
    assert tokenResponse != null;

    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.setBearerAuth(bearerToken);
    defaultHttpEntity = new HttpEntity<>(httpHeaders);
    return tokenResponse.getToken();
  }

  protected HttpHeaders headersWithAuth() {
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(bearerToken);
    headers.setContentType(MediaType.APPLICATION_JSON);
    return headers;
  }

}
