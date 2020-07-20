package com.arqaam.logframelab.service.usermanager;

import com.arqaam.logframelab.controller.dto.auth.UserDto;
import com.arqaam.logframelab.controller.dto.auth.create.UserAuthProvisioningRequestDto;
import com.arqaam.logframelab.exception.UserProvisioningException;
import com.arqaam.logframelab.model.persistence.auth.User;
import com.arqaam.logframelab.repository.GroupRepository;
import com.arqaam.logframelab.service.auth.UserService;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserManagerImpl implements UserManager {

  private final UserService userService;
  private final GroupRepository groupRepository;
  private final PasswordEncoder passwordEncoder;

  public UserManagerImpl(UserService userService,
      GroupRepository groupRepository,
      PasswordEncoder passwordEncoder) {
    this.userService = userService;
    this.groupRepository = groupRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  public User provisionUser(UserAuthProvisioningRequestDto authProvisioningRequest) {
    Optional<User> optionalUser = userService.findByUsername(authProvisioningRequest.getUsername());

    User user;
    if (optionalUser.isPresent()) {
      user = optionalUser.get();
      user.removeAllGroups();
    } else if (StringUtils.isBlank(authProvisioningRequest.getPassword())) {
      throw new UserProvisioningException("user.provisioning.exception.no-password");
    } else {
      user = User.builder().enabled(true).username(authProvisioningRequest.getUsername())
          .password(passwordEncoder.encode(authProvisioningRequest.getPassword())).build();
    }

    user.addGroups(groupRepository.findAllById(authProvisioningRequest.getGroupIds()));

    return userService.save(user);
  }

  @Override
  public List<UserDto> getUsers() {
    //TODO use an optimised query that returns only username and group names
    Map<String, List<String>> userToGroups = new HashMap<>();
    userService.getAllUsers().forEach(user -> {
      if (!userToGroups.containsKey(user.getUsername())) {
        userToGroups.put(user.getUsername(),
            user.getGroupMembership().stream().map(m -> m.getGroup().getName())
                .collect(Collectors.toList()));
      }
    });

    List<UserDto> userDto = new ArrayList<>();
    userToGroups.forEach((username, groups) -> userDto
        .add(UserDto.builder().username(username).groups(groups).build()));
    userDto.sort(Comparator.comparing(UserDto::getUsername));

    return userDto;
  }

  @Override
  public Optional<UserDto> userExistsByUsername(String username) {
    Optional<User> user = userService.findByUsername(username);
    return user.map(value -> UserDto.builder().username(value.getUsername())
        .groups(value.getGroupMembership().stream().map(gm -> gm.getGroup().getName()).collect(
            Collectors.toList())).build());
  }
}
