package com.arqaam.logframelab.service.auth;

import com.arqaam.logframelab.controller.dto.auth.AuthenticateUserRequestDto;
import com.arqaam.logframelab.controller.dto.auth.UpdatePasswordRequestDto;
import com.arqaam.logframelab.model.persistence.auth.User;
import java.util.Optional;
import org.springframework.security.core.Authentication;

public interface AuthService {

  boolean userExists(String username);

  Optional<Authentication> authenticateUser(AuthenticateUserRequestDto loginRequest);

  Optional<User> updatePassword(User user, UpdatePasswordRequestDto updatePasswordRequest);

  String generateToken(User user);

  Long getTokenExpiryInMillis();

  String getTokenType();
}
