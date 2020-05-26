package com.arqaam.logframelab.service;

import com.arqaam.logframelab.controller.dto.auth.AuthenticateUserRequestDto;
import com.arqaam.logframelab.controller.dto.auth.UpdatePasswordRequestDto;
import com.arqaam.logframelab.controller.dto.auth.UserAuthProvisioningRequestDto;
import com.arqaam.logframelab.model.persistence.auth.User;
import org.springframework.security.core.Authentication;

import java.util.Optional;

public interface AuthService {

  Optional<User> registerUser();

  boolean userExists(String username);

  Optional<Authentication> authenticateUser(AuthenticateUserRequestDto loginRequest);

  Optional<User> updatePassword(User user, UpdatePasswordRequestDto updatePasswordRequest);

  String generateToken(User user);

  void updateUserGroups(UserAuthProvisioningRequestDto authProvisioningRequest);
}
