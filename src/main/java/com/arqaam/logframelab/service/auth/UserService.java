package com.arqaam.logframelab.service.auth;

import com.arqaam.logframelab.controller.dto.auth.UserDto;
import com.arqaam.logframelab.controller.dto.auth.create.UserAuthProvisioningRequestDto;
import com.arqaam.logframelab.exception.UserProvisioningException;
import com.arqaam.logframelab.model.persistence.auth.User;
import com.arqaam.logframelab.repository.GroupRepository;
import com.arqaam.logframelab.repository.UserRepository;
import com.arqaam.logframelab.util.Logging;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService implements Logging {

  private final UserRepository userRepository;
  private final GroupRepository groupRepository;
  private final PasswordEncoder passwordEncoder;

  public UserService(UserRepository userRepository, GroupRepository groupRepository, PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.groupRepository = groupRepository;
    this.passwordEncoder = passwordEncoder;
  }

  public List<User> getUserByGroupName(String groupName) {
    logger().info("Retrieving user by group name: {}", groupName);
    return userRepository.findUserByGroupMembership(groupName);
  }

  public User createOrUpdateUser(User user) {
    logger().info("Starting to create or save user: {}", user);
    return userRepository.save(user);
  }

  public void deleteUserById(String id) {
    logger().info("Deleting user by its id: {}", id);
    userRepository.deleteById(id);
  }

  public User deleteUserByUsername(String username) {
    logger().info("Deleting user by its username: {}", username);
    return userRepository.deleteUserByUsername(username);
  }

  public Optional<User> findByUsername(String username) {
    logger().info("Retrieving user by username: {}", username);
    return userRepository.findByUsername(username);
  }

  public boolean existsUserByUsername(String username) {
    logger().info("Verifying the existence of a user with username: {}", username);
    return userRepository.existsByUsername(username);
  }

  public User provisionUser(UserAuthProvisioningRequestDto authProvisioningRequest) {
    Optional<User> optionalUser = findByUsername(authProvisioningRequest.getUsername());

    User user;
    if (optionalUser.isPresent()) {
      user = optionalUser.get();
      user.removeAllGroups();
    } else if (StringUtils.isEmpty(authProvisioningRequest.getPassword())) {
      throw new UserProvisioningException();
    } else {
      user = new User(authProvisioningRequest.getUsername(),
              passwordEncoder.encode(authProvisioningRequest.getPassword()), true);
    }

    user.addGroups(groupRepository.findAllById(authProvisioningRequest.getGroupIds()));

    return createOrUpdateUser(user);
  }

  public List<UserDto> getUsersAndGroups() {
    logger().info("Retrieving all users with its groups");

    //TODO use an optimised query that returns only username and group names
    Map<String, List<String>> userToGroups = new HashMap<>();
    userRepository.getAllUsersWithTheirGroups().forEach(user -> {
      if (!userToGroups.containsKey(user.getUsername())) {
        userToGroups.put(user.getUsername(),
                user.getGroupMembership().stream().map(m -> m.getGroup().getName())
                        .collect(Collectors.toList()));
      }
    });

    List<UserDto> userDto = new ArrayList<>();
    userToGroups.forEach((username, groups) -> userDto
            .add(new UserDto(username, groups)));
    userDto.sort(Comparator.comparing(UserDto::getUsername));

    return userDto;
  }
}
