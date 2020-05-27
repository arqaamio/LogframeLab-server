package com.arqaam.logframelab.service.auth;

import com.arqaam.logframelab.configuration.security.jwt.JwtTokenProvider;
import com.arqaam.logframelab.controller.dto.auth.AuthenticateUserRequestDto;
import com.arqaam.logframelab.controller.dto.auth.UpdatePasswordRequestDto;
import com.arqaam.logframelab.exception.PasswordResetException;
import com.arqaam.logframelab.model.persistence.auth.User;
import java.util.Optional;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
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
    throw new NotImplementedException("Not yet implemented");
  }

  @Override
  public Optional<Authentication> authenticateUser(AuthenticateUserRequestDto loginRequest) {
    Authentication authentication =
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                loginRequest.getUsername(), loginRequest.getPassword()));
    return Optional.ofNullable(authentication);
  }

  @Override
  public Optional<User> updatePassword(User user, UpdatePasswordRequestDto updatePasswordRequest) {
    User currentUser =
        userService
            .findByUsername(user.getUsername())
            .orElseThrow(() -> new PasswordResetException("Specified user not found."));

    if (!passwordEncoder.matches(
        updatePasswordRequest.getOldPassword(), currentUser.getPassword())) {
      throw new PasswordResetException(
          "Specified old password does not match the current password.");
    }

    if (passwordEncoder.matches(
        updatePasswordRequest.getNewPassword(), currentUser.getPassword())) {
      throw new PasswordResetException(
          "Specified new password cannot be the same as current password.");
    }

    currentUser.setPassword(passwordEncoder.encode(updatePasswordRequest.getNewPassword()));
    return Optional.ofNullable(userService.save(currentUser));
  }

  @Override
  public String generateToken(User user) {
    return tokenProvider.generateJwsToken(user);
  }
}
