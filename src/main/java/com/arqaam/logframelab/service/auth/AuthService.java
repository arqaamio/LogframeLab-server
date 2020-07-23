package com.arqaam.logframelab.service.auth;

import com.arqaam.logframelab.model.persistence.auth.User;
import java.util.Optional;
import org.springframework.security.core.Authentication;

public interface AuthService {

  boolean userExists(String username);

  Optional<Authentication> authenticateUser(String username, String password);

  Optional<User> updatePassword(User user, String oldPassword, String newPassword);

  String generateToken(User user);

  Long getTokenExpiryInSeconds();

  String getTokenType();

  void logout(String username);
}
