package com.arqaam.logframelab.service.auth;

import com.arqaam.logframelab.configuration.security.jwt.JwtTokenProvider;
import com.arqaam.logframelab.exception.LogoutWrongUserException;
import com.arqaam.logframelab.exception.PasswordResetException;
import com.arqaam.logframelab.exception.WrongCredentialsException;
import com.arqaam.logframelab.model.persistence.auth.User;
import com.arqaam.logframelab.util.Logging;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService implements Logging {

  private final AuthenticationManager authenticationManager;
  private final UserService userService;
  private final PasswordEncoder passwordEncoder;
  private final JwtTokenProvider tokenProvider;

  public AuthService(
          AuthenticationManager authenticationManager,
          UserService userService,
          PasswordEncoder passwordEncoder,
          JwtTokenProvider tokenProvider) {
    this.authenticationManager = authenticationManager;
    this.userService = userService;
    this.passwordEncoder = passwordEncoder;
    this.tokenProvider = tokenProvider;
  }

  /**
   * Authenticates user
   *
   * @param username Username
   * @param password Password
   * @return Authentication information
   */
  public Authentication authenticateUser(String username, String password) {
    logger().info("Authenticating user with username: {}", username);
    try {
      return authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
    } catch (BadCredentialsException e) {
      logger().info("Failed to authenticate user with username: {}", username);
      throw new WrongCredentialsException();
    }
  }

  /**
   * Updates password of a user
   *
   * @param user        User
   * @param oldPassword Old passowrd
   * @param newPassword New password
   * @return Updated user
   */
  public Optional<User> updatePassword(User user, String oldPassword, String newPassword) {
    logger().info("Verifying if user exists with username: {}", user.getUsername());
    User currentUser =
            userService
                    .findByUsername(user.getUsername())
                    .orElseThrow(() -> new PasswordResetException("Specified user not found."));
    logger().info("Verifying if passwords match");
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
    return Optional.ofNullable(userService.createOrUpdateUser(currentUser));
  }

  /**
   * Generates JWT token
   *
   * @param user User
   * @return New JWT token
   */
  public String generateToken(User user) {
    return tokenProvider.generateToken(user);
  }

  /**
   * Retrieves the time it takes for JWT tokens to expiry
   *
   * @return Time it takes for token to expiry
   */
  public Long getTokenExpirationInSeconds() {
    return tokenProvider.getExpirationInSeconds();
  }

  /**
   * Retrieves the token type
   *
   * @return Token type
   */
  public String getTokenType() {
    return tokenProvider.getTokenType();
  }

  /**
   * Logs out the user by its username
   *
   * @param username Username of the user that logs out
   */
  public void logout(String username) {
    logger().info("Logging out user with username: {}", username);
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    logger().info("principal: {}, actual: {}", authentication.getPrincipal(), username);
    logger().info("test: {}", authentication.getPrincipal().equals(username));
    //if (!authentication.getPrincipal().equals(username)) {
    if (!((User) authentication.getPrincipal()).getUsername().equals(username)) {
      logger().error("Failed to log out user since username doesn't match. Username: {}",
              username, authentication.getPrincipal());
      throw new LogoutWrongUserException();
    }

    SecurityContextHolder.clearContext();
  }

  /**
   * Refresh token
   *
   * @param token Valid token
   * @return token
   */
  public String refreshToken(String token){
    return tokenProvider.refreshToken(token);
  }

}
