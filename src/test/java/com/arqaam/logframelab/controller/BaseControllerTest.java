package com.arqaam.logframelab.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.arqaam.logframelab.controller.dto.auth.login.AuthenticateUserRequestDto;
import com.arqaam.logframelab.controller.dto.auth.login.JwtAuthenticationTokenResponse;
import com.arqaam.logframelab.model.Error;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(profiles = "integration")
public class BaseControllerTest {

  private static final String SEC_ADMIN_USERNAME = "secadmin";
  private static final String SEC_ADMIN_PASSWORD = "Passw0rdArqaam1234@";

  protected String bearerToken;

  protected HttpEntity defaultHttpEntity;

  private HttpHeaders httpHeaders;

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

    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.setBearerAuth(tokenResponse.getToken());
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
/*
 * This class is necessary for test cases that expect Spring's Page<T> response. Jackson is unable
 * to serialize the default PageImpl.
 * Solution found at https://stackoverflow.com/a/52509886/2211446
 */
class ResponsePage<T> extends PageImpl<T> {

  @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
  public ResponsePage(@JsonProperty("content") List<T> content, @JsonProperty("number") int number,
                      @JsonProperty("size") int size,
                      @JsonProperty("totalElements") Long totalElements,
                      @JsonProperty("pageable") JsonNode pageable, @JsonProperty("last") boolean last,
                      @JsonProperty("totalPages") int totalPages, @JsonProperty("sort") JsonNode sort,
                      @JsonProperty("first") boolean first,
                      @JsonProperty("numberOfElements") int numberOfElements) {
    super(content, PageRequest.of(number, size), totalElements);
  }

  public ResponsePage(List<T> content, Pageable pageable, long total) {
    super(content, pageable, total);
  }

  public ResponsePage(List<T> content) {
    super(content);
  }

  public ResponsePage() {
    super(new ArrayList<T>());
  }
}