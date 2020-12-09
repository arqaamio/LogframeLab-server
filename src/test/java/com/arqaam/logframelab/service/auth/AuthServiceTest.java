package com.arqaam.logframelab.service.auth;

import com.arqaam.logframelab.configuration.security.jwt.JwtTokenProvider;
import com.arqaam.logframelab.exception.LogoutWrongUserException;
import com.arqaam.logframelab.exception.PasswordResetException;
import com.arqaam.logframelab.exception.WrongCredentialsException;
import com.arqaam.logframelab.model.persistence.auth.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {
  @Mock
  AuthenticationManager authenticationManager;
  @Mock
  UserService userService;
  @Mock
  PasswordEncoder passwordEncoder;
  @Mock
  JwtTokenProvider jwtTokenProvider;
  
  @InjectMocks
  AuthService authService;

  @Test
  void authenticateUserTest(){
    String username = "username";
    String password = "password";
    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(username, password);
    Authentication expected = new UsernamePasswordAuthenticationToken(new Object(), new Object());
    when(authenticationManager.authenticate(auth)).thenReturn(expected);
    authService.authenticateUser(username, password);
    verify(authenticationManager).authenticate(any());
  }

  @Test
  void authenticateUserTest_badCredentials(){
    String username = "username";
    String password = "password";
    when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException(""));

    assertThrows(WrongCredentialsException.class, ()->authService.authenticateUser(username, password));
  }

  @Test
  void updatePasswordTest(){
    String username = "username";
    String oldPassword = "old password";
    String password = "password";
    User user = new User();
    user.setUsername(username);
    user.setPassword(passwordEncoder.encode(password));
    when(userService.findByUsername(username)).thenReturn(Optional.of(user));
    when(passwordEncoder.matches(oldPassword, user.getPassword())).thenReturn(true);
    when(passwordEncoder.matches(password, user.getPassword())).thenReturn(false);
    when(userService.createOrUpdateUser(any())).thenReturn(user);
    Optional<User> result = authService.updatePassword(user, oldPassword, password);
    verify(userService).findByUsername(username);
    verify(userService).createOrUpdateUser(any());

    assertEquals(Optional.of(user), result);
  }

  @Test
  void updatePasswordTest_oldPasswordDoesntMatchNew(){
    String username = "username";
    String oldPassword = "old password";
    String password = "password";
    User user = new User();
    user.setUsername(username);
    user.setPassword(passwordEncoder.encode(password));
    when(userService.findByUsername(username)).thenReturn(Optional.of(user));
    when(passwordEncoder.matches(oldPassword, user.getPassword())).thenReturn(false);
    assertThrows(PasswordResetException.class, ()-> authService.updatePassword(user, oldPassword, password));

    verify(userService).findByUsername(username);
    verify(userService, times(0)).createOrUpdateUser(any());
  }

  @Test
  void updatePasswordTest_newPasswordSameAsOldOne(){
    String username = "username";
    String oldPassword = "old password";
    String password = "password";
    User user = new User();
    user.setUsername(username);
    user.setPassword("other password");
    when(userService.findByUsername(username)).thenReturn(Optional.of(user));
    when(passwordEncoder.matches(oldPassword, user.getPassword())).thenReturn(true);
    when(passwordEncoder.matches(password, "other password")).thenReturn(true);
    assertThrows(PasswordResetException.class, ()-> authService.updatePassword(user, oldPassword, password));
    verify(userService).findByUsername(username);
    verify(userService, times(0)).createOrUpdateUser(any());
  }

  @Test
  void generateTokenTest(){
    String expect = "token";
    when(jwtTokenProvider.generateToken(any())).thenReturn(expect);
    String result = authService.generateToken(new User());
    verify(jwtTokenProvider).generateToken(any());
    assertEquals(expect, result);
  }

  @Test
  void getTokenExpirationInSecondsTest(){
    Long expect = 3600L;
    when(jwtTokenProvider.getExpirationInSeconds()).thenReturn(expect);
    Long result = authService.getTokenExpirationInSeconds();
    verify(jwtTokenProvider).getExpirationInSeconds();
    assertEquals(expect, result);
  }

  @Test
  void getTokenTypeTest(){
    String expect = "Bearer";
    when(jwtTokenProvider.getTokenType()).thenReturn(expect);
    String result = authService.getTokenType();
    verify(jwtTokenProvider).getTokenType();
    assertEquals(expect, result);
  }

  @Test
  void logoutTest(){
    String username = "username";
    String password = "password";
    SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(new User(username, password, true), password));
    authService.logout(username);
  }

  @Test
  void logoutTest_otherUser(){
    String username = "username";
    String password = "password";
    SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(new User(username, password, true), password));
    assertThrows(LogoutWrongUserException.class, ()->authService.logout("otheruser"));
  }

  @Test
  void refreshTokenTest(){
    String expect = "token";
    String previous = authService.generateToken(new User());
    when(jwtTokenProvider.refreshToken(previous)).thenReturn(expect);

    String result = authService.refreshToken(previous);
    verify(jwtTokenProvider).refreshToken(previous);
    assertEquals(expect,result);
  }

}