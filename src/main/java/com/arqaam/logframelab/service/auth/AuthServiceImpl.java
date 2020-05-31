package com.arqaam.logframelab.service.auth;

import com.arqaam.logframelab.configuration.security.jwt.JwtTokenProvider;
import com.arqaam.logframelab.exception.LogoutUserException;
import com.arqaam.logframelab.exception.PasswordResetException;
import com.arqaam.logframelab.model.persistence.auth.User;
import java.util.Optional;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

  private final AuthenticationManager authenticationManager;
  private final UserService userService;
  private final PasswordEncoder passwordEncoder;
  private final JwtTokenProvider tokenProvider;

  public AuthServiceImpl(
      AuthenticationManager authenticationManager,
      UserService userService,
      PasswordEncoder passwordEncoder,
      JwtTokenProvider tokenProvider) {
    this.authenticationManager = authenticationManager;
    this.userService = userService;
    this.passwordEncoder = passwordEncoder;
    this.tokenProvider = tokenProvider;
  }

  @Override
  public boolean userExists(String username) {
    return userService.userWithUsernameExists(username);
  }

  @Override
  public Optional<Authentication> authenticateUser(String username, String password) {
    Authentication authentication =
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(username, password));
    return Optional.ofNullable(authentication);
  }

  @Override
  public Optional<User> updatePassword(User user, String oldPassword, String newPassword) {
    User currentUser =
        userService
            .findByUsername(user.getUsername())
            .orElseThrow(() -> new PasswordResetException("Specified user not found."));

    if (!passwordEncoder.matches(
        oldPassword, currentUser.getPassword())) {
      throw new PasswordResetException(
          "Specified old password does not match the current password.");
    }

    if (passwordEncoder.matches(
        newPassword, currentUser.getPassword())) {
      throw new PasswordResetException(
          "Specified new password cannot be the same as current password.");
    }

    currentUser.setPassword(passwordEncoder.encode(newPassword));
    return Optional.ofNullable(userService.save(currentUser));
  }

  @Override
  public String generateToken(User user) {
    return tokenProvider.generateJwsToken(user);
  }

  @Override
  public Long getTokenExpiryInMillis() {
    return tokenProvider.getJwtExpirationInMillis();
  }

  @Override
  public String getTokenType() {
    return tokenProvider.getTokenType();
  }

  @Override
  public void logout(String username) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (!authentication.getPrincipal().equals(username)) {
      throw new LogoutUserException("Attempting to logout wrong user");
    }

    SecurityContextHolder.clearContext();
  }
}
