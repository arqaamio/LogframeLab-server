package com.arqaam.logframelab.service.auth;

import com.arqaam.logframelab.model.persistence.auth.User;

import java.util.List;
import java.util.Optional;

public interface UserService {

  Optional<User> getFirstUserByGroupName(String groupName);

  User save(User user);

  Optional<User> findByUsername(String username);

  boolean userWithUsernameExists(String username);

  List<User> getAllUsers();
}
