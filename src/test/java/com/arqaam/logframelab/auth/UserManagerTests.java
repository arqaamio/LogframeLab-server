package com.arqaam.logframelab.auth;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.arqaam.logframelab.controller.BaseControllerTest;
import com.arqaam.logframelab.controller.dto.auth.create.UserAuthProvisioningRequestDto;
import com.arqaam.logframelab.controller.dto.auth.create.UserAuthProvisioningResponseDto;
import com.arqaam.logframelab.repository.initializer.BaseDatabaseTest;
import java.util.Collection;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class UserManagerTests extends BaseControllerTest implements BaseDatabaseTest  {

  private static final String APP_USER_USERNAME = "user";
  private static final String APP_USER_PASSWORD = "Password";
  private static final Collection<Integer> APP_USER_GROUP_ID = Collections.singleton(2);
  private static final String AUTH_USER_URL = "/auth/users";
  private static final int APP_USER_GROUP_SIZE = 1;
  private static final String APP_USER_GROUP_NAME = "APP_USER";

  @BeforeEach
  void setup() {
    generateAuthToken();
  }

  @Test
  void userManager() {
    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.setBearerAuth(bearerToken);

    UserAuthProvisioningRequestDto requestDto =
        new UserAuthProvisioningRequestDto(APP_USER_USERNAME, APP_USER_PASSWORD, APP_USER_GROUP_ID);

    ResponseEntity<UserAuthProvisioningResponseDto> response = testRestTemplate
        .exchange(AUTH_USER_URL, HttpMethod.POST, new HttpEntity<>(requestDto, httpHeaders),
            UserAuthProvisioningResponseDto.class);

    assertAll(
        () -> assertThat(response.getStatusCode(), is(HttpStatus.OK)),
        () -> assertThat(response.getBody(), notNullValue()),
        () -> assertThat(response.getBody().getUsername(), is(APP_USER_USERNAME)),
        () -> assertThat(response.getBody().getGroups(), hasSize(APP_USER_GROUP_SIZE)),
        () -> assertThat(response.getBody().getGroups(), hasItem(APP_USER_GROUP_NAME))
    );
  }

}
