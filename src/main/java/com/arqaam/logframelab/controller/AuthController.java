package com.arqaam.logframelab.controller;

import com.arqaam.logframelab.controller.dto.AuthenticateUserRequestDto;
import com.arqaam.logframelab.controller.dto.JwtAuthenticationTokenResponse;
import com.arqaam.logframelab.exception.UnauthorizedException;
import com.arqaam.logframelab.model.persistence.auth.User;
import com.arqaam.logframelab.service.AuthService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(
    value = "auth",
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE)
public class AuthController {

  private final AuthService authService;

  @Value("${jwt.header.prefix}")
  private String tokenType;

  @Value("${jwt.expiration}")
  private Long tokenDuration;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping(value = "login")
  public ResponseEntity<?> authenticateUser(
      @RequestBody AuthenticateUserRequestDto authenticateUserRequestDto) {
    Authentication authentication =
        authService
            .authenticateUser(authenticateUserRequestDto)
            .orElseThrow(() -> new UnauthorizedException("Unable to authenticate user"));

    User user = (User) authentication.getPrincipal();
    SecurityContextHolder.getContext().setAuthentication(authentication);

    String token = authService.generateToken(user);
    return ResponseEntity.ok(new JwtAuthenticationTokenResponse(token, tokenType, tokenDuration));
  }
}
