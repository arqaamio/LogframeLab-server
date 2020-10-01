package com.arqaam.logframelab.controller;

import com.arqaam.logframelab.configuration.security.ContextRefreshEventListener;
import com.arqaam.logframelab.controller.dto.auth.login.AuthenticateUserRequestDto;
import com.arqaam.logframelab.controller.dto.auth.login.JwtAuthenticationTokenResponse;
import com.arqaam.logframelab.model.persistence.auth.User;
import com.arqaam.logframelab.service.auth.AuthService;
import com.arqaam.logframelab.service.auth.GroupService;
import com.arqaam.logframelab.service.auth.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class AuthControllerTest extends BaseControllerTest {
  private static final String TOKEN_HEADER = "Authorization";
  private static final String TOKEN_TYPE = "";
  private static final String TOKEN_HEADER_PREFIX = "Bearer";
  private static final Long TOKEN_EXPIRATION = 3*3600L;
  @MockBean
  private AuthService authService;
  @MockBean
  private UserService userService;
  @MockBean
  private GroupService groupService;
  @MockBean
  private ContextRefreshEventListener listener;
  //@MockBean
  //private JwtTokenProvider jwtTokenProvider;
  //@MockBean
  //private UserDetailsServiceImpl userDetailsService;
  
  @BeforeEach
  void setup() {

    when(userService.getUserByGroupName(any())).thenReturn(Collections.singletonList(new User()));
  }

  @Test
  void authenticateUserTest() {
    String username = "username";
    String password = "password";
    String token = "token";
    String tokenType = "tokenType";
    User user = new User(username, password, true);
    when(authService.authenticateUser(username, password)).thenReturn(new UsernamePasswordAuthenticationToken(user, password));
    when(authService.generateToken(any())).thenReturn(token);
    when(authService.getTokenType()).thenReturn(tokenType);
    JwtAuthenticationTokenResponse expected =  new JwtAuthenticationTokenResponse(token, tokenType, Collections.emptyList());
    AuthenticateUserRequestDto body = new AuthenticateUserRequestDto(username, password);
    ResponseEntity<JwtAuthenticationTokenResponse> response =
        testRestTemplate.exchange(
            "/auth/login",
            HttpMethod.POST,
            new HttpEntity<>(body), JwtAuthenticationTokenResponse.class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(expected, response.getBody());
  }

//  @Test
//  @Disabled
//  void getUserTest() {
//    HttpHeaders headers = new HttpHeaders();
//    headers.setBearerAuth("token");
//
//    String username = "username";
//    String groupName = "Group name";
//    List<String> groups = Collections.singletonList(groupName);
//    UserDto expected = new UserDto(username, groups);
//    Group group = new Group();
//    group.setName(groupName);
//    User user = new User();
//    user.setUsername(username);
//    user.addGroup(group);
//    when(jwtTokenProvider.isTokenValid(any())).thenReturn(true);
//    when(jwtTokenProvider.getTokenHeader()).thenReturn(TOKEN_HEADER);
//    when(jwtTokenProvider.getTokenType()).thenReturn(TOKEN_TYPE);
//    when(jwtTokenProvider.getTokenHeaderPrefix()).thenReturn(TOKEN_HEADER_PREFIX);
//    when(jwtTokenProvider.getExpirationInSeconds()).thenReturn(TOKEN_EXPIRATION*3600);
//    when(userDetailsService.loadUserByUsername(username)).thenReturn(user);
//    when(userService.findByUsername(username)).thenReturn(Optional.of(user));
//    ResponseEntity<UserDto> response = testRestTemplate.exchange(
//        "/users/"+username, HttpMethod.GET, new HttpEntity<>(headers), UserDto.class);
//
//    assertEquals(HttpStatus.OK, response.getStatusCode());
//    assertNotNull(response.getBody());
//    assertEquals(expected, response.getBody());
//  }
}
