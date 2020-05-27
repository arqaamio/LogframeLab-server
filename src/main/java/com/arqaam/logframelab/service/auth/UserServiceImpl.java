package com.arqaam.logframelab.service.auth;

import com.arqaam.logframelab.model.persistence.auth.User;
import com.arqaam.logframelab.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;

  public UserServiceImpl(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public Optional<User> getFirstUserByGroupName(String groupName) {
    return userRepository.findUserByGroupName(groupName);
  }

  @Override
  public User save(User user) {
    return userRepository.save(user);
  }

  @Override
  public Optional<User> findByUsername(String username) {
    return userRepository.findByUsername(username);
  }
}
