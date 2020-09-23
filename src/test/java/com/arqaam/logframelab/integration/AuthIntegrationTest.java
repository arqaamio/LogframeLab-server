package com.arqaam.logframelab.integration;

import com.arqaam.logframelab.controller.dto.auth.GroupDto;
import com.arqaam.logframelab.controller.dto.auth.UserDto;
import com.arqaam.logframelab.controller.dto.auth.create.UserAuthProvisioningRequestDto;
import com.arqaam.logframelab.controller.dto.auth.create.UserAuthProvisioningResponseDto;
import com.arqaam.logframelab.controller.dto.auth.login.AuthenticateUserRequestDto;
import com.arqaam.logframelab.controller.dto.auth.login.JwtAuthenticationTokenResponse;
import com.arqaam.logframelab.controller.dto.auth.logout.LogoutUserRequestDto;
import com.arqaam.logframelab.controller.dto.auth.logout.LogoutUserResponseDto;
import com.arqaam.logframelab.exception.LogoutWrongUserException;
import com.arqaam.logframelab.exception.UserNotFoundException;
import com.arqaam.logframelab.exception.WrongCredentialsException;
import com.arqaam.logframelab.model.Error;
import com.arqaam.logframelab.model.persistence.auth.Group;
import com.arqaam.logframelab.model.persistence.auth.GroupMember;
import com.arqaam.logframelab.model.persistence.auth.User;
import com.arqaam.logframelab.repository.GroupRepository;
import com.arqaam.logframelab.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;

import java.util.*;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

public class AuthIntegrationTest extends BaseIntegrationTest {
  private static final String TOKEN_TYPE = "Bearer";
  private static final String APP_USER_USERNAME = "user";
  private static final String APP_USER_PASSWORD = "Password";
  private static final Collection<Integer> APP_USER_GROUP_IDS = Collections.singleton(2);
  private static final String AUTH_USER_URL = "/auth/users";
  private static final int APP_USER_GROUP_SIZE = 1;
  private static final String APP_USER_GROUP_NAME = "APP_USER";
  private static final String SEC_ADMIN_GROUP = "SEC_ADMIN";


  private static final int FIRST_IN_LIST = 0;
  private static final int SEC_ADMIN_GROUP_ID = 1, INDICATOR_ADMIN_GROUP_ID = 3, APP_USER_GROUP_ID = 2;
  private static final int SIZE_TWO = 2, SIZE_FOUR = 4, SIZE_THREE = 3, SIZE_ONE = 1;

  @Autowired
  private GroupRepository groupRepository;

  @Autowired
  private UserRepository userRepository;

  @BeforeEach
  void setup() {
      generateAuthToken();
  }
  
  @Test
  void authenticateUserTest() {
    AuthenticateUserRequestDto requestDto =
        new AuthenticateUserRequestDto(SEC_ADMIN_USERNAME, SEC_ADMIN_PASSWORD);

    ResponseEntity<JwtAuthenticationTokenResponse> response = testRestTemplate
        .exchange("/auth/login", HttpMethod.POST, new HttpEntity<>(requestDto),
        JwtAuthenticationTokenResponse.class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertFalse(response.getBody().getToken().isBlank());
    assertEquals(TOKEN_TYPE, response.getBody().getTokenType());
    assertEquals(Collections.singletonList(SEC_ADMIN_GROUP), response.getBody().getGroups());
  }

  @Test
  void authenticateUserTest_WrongCredentials() {
    AuthenticateUserRequestDto requestDto =
        new AuthenticateUserRequestDto(SEC_ADMIN_USERNAME, "Wrong password");

    ResponseEntity<Error> response = testRestTemplate
        .exchange("/auth/login", HttpMethod.POST, new HttpEntity<>(requestDto), Error.class);
    assertEqualsException(response, HttpStatus.UNPROCESSABLE_ENTITY, 16, WrongCredentialsException.class);
  }

  @Test
  void userLogoutTest() {
    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.setBearerAuth(bearerToken);
    
    LogoutUserRequestDto requestDto = new LogoutUserRequestDto(SEC_ADMIN_USERNAME);

    ResponseEntity<LogoutUserResponseDto> response = testRestTemplate
        .exchange("/auth/logout", HttpMethod.POST, new HttpEntity<>(requestDto, httpHeaders),
        LogoutUserResponseDto.class);


    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertTrue(response.getBody().getIsLoggedOut());
  }

  @Test
  void userLogoutTest_userMismatch() {
    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.setBearerAuth(bearerToken);
    
    LogoutUserRequestDto requestDto = new LogoutUserRequestDto("Other User");

    ResponseEntity<Error> response = testRestTemplate
        .exchange("/auth/logout", HttpMethod.POST, new HttpEntity<>(requestDto, httpHeaders),
        Error.class);

    assertEqualsException(response, HttpStatus.UNPROCESSABLE_ENTITY, 15, LogoutWrongUserException.class);
  }

  @Test
  void createUserTest() {
    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.setBearerAuth(bearerToken);

    UserAuthProvisioningRequestDto requestDto =
        new UserAuthProvisioningRequestDto(APP_USER_USERNAME, APP_USER_PASSWORD, APP_USER_GROUP_IDS);

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

  @Test
  void getUsersTest() {
    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.setBearerAuth(bearerToken);
    httpHeaders.setContentType(MediaType.APPLICATION_JSON);
    
    ResponseEntity<List<UserDto>> response = testRestTemplate
        .exchange("/auth/users", HttpMethod.GET, new HttpEntity<>(httpHeaders),
        new ParameterizedTypeReference<List<UserDto>>(){});
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertFalse(response.getBody().isEmpty());
    assertTrue(response.getBody().stream().anyMatch(x->x.getUsername().equals(SEC_ADMIN_USERNAME)));
  }

  @Test
  void getGroupsTest() {
    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.setBearerAuth(bearerToken);
    httpHeaders.setContentType(MediaType.APPLICATION_JSON);
    
    ResponseEntity<Set<GroupDto>> response = testRestTemplate
        .exchange("/auth/groups", HttpMethod.GET, new HttpEntity<>(httpHeaders),
        new ParameterizedTypeReference<Set<GroupDto>>(){});
    System.out.println(response);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertFalse(response.getBody().isEmpty());
    assertEquals(3, response.getBody().size());
    assertTrue(response.getBody().stream().anyMatch(x->x.getName().equals(SEC_ADMIN_GROUP)));
  }

  @Test
  void getUserTest() {
    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.setBearerAuth(bearerToken);
    httpHeaders.setContentType(MediaType.APPLICATION_JSON);
    
    ResponseEntity<UserDto> response = testRestTemplate
        .exchange("/auth/users/"+SEC_ADMIN_USERNAME, HttpMethod.GET, new HttpEntity<>(httpHeaders), UserDto.class);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(SEC_ADMIN_USERNAME, response.getBody().getUsername());
    assertEquals(Collections.singletonList(SEC_ADMIN_GROUP), response.getBody().getGroups());
  }

  @Test
  void getUserTest_userNotFound() {
    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.setBearerAuth(bearerToken);
    httpHeaders.setContentType(MediaType.APPLICATION_JSON);
    
    ResponseEntity<Error> response = testRestTemplate
        .exchange("/auth/users/username", HttpMethod.GET, new HttpEntity<>(httpHeaders), Error.class);
    assertEqualsException(response, HttpStatus.NOT_FOUND, 14, UserNotFoundException.class);
  }

  /****************************************************
   * REPOSITORY TESTS
   ***************************************************/
  @Test
  public void whenUserIsCreatedInGroup_ThenCheckMembership() {
    User user = User.builder().username("user").password("password").enabled(true).build();
    Group userGroup =
        groupRepository
            .findById(APP_USER_GROUP_ID)
            .orElseThrow(
                () -> new IllegalStateException("Group not found for " + APP_USER_GROUP_IDS));
    user.addGroup(userGroup);

    User savedUser = userRepository.save(user);

    Set<GroupMember> memberships = savedUser.getGroupMembership();

    assertAll(
        () -> assertThat(memberships, hasSize(SIZE_ONE)),
        () ->
            assertThat(
                memberships.toArray(new GroupMember[]{})[FIRST_IN_LIST].getGroup(), is(userGroup)),
        () -> assertThat(savedUser.getAuthorities(), hasSize(SIZE_THREE)));
  }

  @Test
  public void whenUserIsCreatedInGroups_ThenCheckMembership() {
    User userInGroups = createUserInGroups();

    Set<GroupMember> memberships = userInGroups.getGroupMembership();

    assertAll(
        () -> assertThat(memberships, hasSize(SIZE_TWO)),
        () ->
            assertThat(
                memberships.stream()
                    .map(groupMember -> groupMember.getGroup().getId())
                    .collect(Collectors.toSet()),
                containsInAnyOrder(INDICATOR_ADMIN_GROUP_ID, SEC_ADMIN_GROUP_ID)),
        () -> assertThat(userInGroups.getAuthorities(), hasSize(SIZE_FOUR)));
  }

  @Test
  public void whenUserIsRemovedFromGroup_ThenCheckMembershipPersisted() {
    User userInGroups = createUserInGroups();

    userInGroups.removeGroup(
        groupRepository
            .findById(SEC_ADMIN_GROUP_ID)
            .orElseThrow(
                () -> new IllegalStateException("Group not found for " + SEC_ADMIN_GROUP_ID)));

    User userWithSingleGroup = userRepository.save(userInGroups);

    Set<GroupMember> groupMembership = userWithSingleGroup.getGroupMembership();

    assertAll(
        () -> assertThat(groupMembership, hasSize(SIZE_ONE)),
        () -> assertThat(
            groupMembership.toArray(new GroupMember[]{})[FIRST_IN_LIST].getGroup().getId(),
            is(INDICATOR_ADMIN_GROUP_ID)));
  }

  private User createUserInGroups() {
    User adminUser = User.builder().username("admin").password("admin").enabled(true).build();
    Collection<Group> adminGroups =
        groupRepository.findAllById(Arrays.asList(INDICATOR_ADMIN_GROUP_ID, SEC_ADMIN_GROUP_ID));
    adminUser.addGroups(adminGroups);

    return userRepository.save(adminUser);
  }
}
