package com.arqaam.logframelab.service.auth;

import com.arqaam.logframelab.model.persistence.auth.User;

import java.util.Optional;

public interface UserService {

  Optional<User> getFirstUserByGroupName(String groupName);

  User save(User user);

  Optional<User> findByUsername(String username);

  public boolean userWithUsernameExists(String username);
}
