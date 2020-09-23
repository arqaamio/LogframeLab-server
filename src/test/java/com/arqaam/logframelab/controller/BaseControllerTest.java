package com.arqaam.logframelab.controller;

import com.arqaam.logframelab.controller.dto.auth.login.AuthenticateUserRequestDto;
import com.arqaam.logframelab.controller.dto.auth.login.JwtAuthenticationTokenResponse;
import com.arqaam.logframelab.model.Error;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(profiles = "test")
public class BaseControllerTest {

  private static final String SEC_ADMIN_USERNAME = "secadmin";
  private static final String SEC_ADMIN_PASSWORD = "Passw0rdArqaam1234@";

  protected String bearerToken;

  @Autowired
  protected TestRestTemplate testRestTemplate;

  void assertEqualsException(ResponseEntity<Error> response, HttpStatus httpStatus, Integer code,
                             Class exception) {
    assertEquals(httpStatus, response.getStatusCode());
    assertEquals(code, response.getBody().getCode());
    assertEquals(exception.getSimpleName(), response.getBody().getException());
  }

  protected void generateAuthToken(String... credentials) {
    bearerToken = token(credentials);
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

    return tokenResponse.getToken();
  }

  protected HttpHeaders headersWithAuth() {
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(bearerToken);
    headers.setContentType(MediaType.APPLICATION_JSON);
    return headers;
  }

}