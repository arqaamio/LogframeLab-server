package com.arqaam.logframelab.service.auth;

import com.arqaam.logframelab.controller.dto.auth.UserDto;
import com.arqaam.logframelab.controller.dto.auth.create.UserAuthProvisioningRequestDto;
import com.arqaam.logframelab.exception.OnlySecAdminUserException;
import com.arqaam.logframelab.exception.UserNotFoundException;
import com.arqaam.logframelab.exception.UserProvisioningException;
import com.arqaam.logframelab.model.persistence.auth.User;
import com.arqaam.logframelab.repository.GroupRepository;
import com.arqaam.logframelab.repository.UserRepository;
import com.arqaam.logframelab.util.Constants;
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
    List<User> secAdminUsers = userRepository.findUserByGroupMembership(Constants.SEC_ADMIN_GROUP_NAME);
    if(secAdminUsers.size() == 1 && secAdminUsers.get(0).getUsername().equals(user.getUsername()) &&
      !user.getGroupMembership().stream().anyMatch(x->x.getGroup().getName().equalsIgnoreCase(Constants.SEC_ADMIN_GROUP_NAME))) {
      logger().error("Failed to update user because it's only sec admin user. User: {}", user);
      throw new OnlySecAdminUserException();
    }

    return userRepository.save(user);
  }

  public void deleteUserById(String id) {
    Optional<User> user = findByUsername(id);
    if (user.isEmpty()) {
      logger().error("Failed to find user with username: {}", id);
      throw new UserNotFoundException();
    }
    if(userRepository.findUserByGroupMembership(Constants.SEC_ADMIN_GROUP_NAME).size() == 1 &&
            user.get().getGroupMembership().stream().anyMatch(x->x.getGroup().getName().equalsIgnoreCase(Constants.SEC_ADMIN_GROUP_NAME))) {
      logger().error("Failed to delete user because it's only sec admin user. User: {}", user);
      throw new OnlySecAdminUserException();
    }
    logger().info("Deleting user by its id: {}", id);
    userRepository.deleteById(id);
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
    User user;
    if (StringUtils.isEmpty(authProvisioningRequest.getPassword())) {
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
