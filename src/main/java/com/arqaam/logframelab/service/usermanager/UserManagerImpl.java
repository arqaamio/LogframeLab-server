package com.arqaam.logframelab.service.usermanager;

import com.arqaam.logframelab.controller.dto.auth.UserDto;
import com.arqaam.logframelab.controller.dto.auth.create.UserAuthProvisioningRequestDto;
import com.arqaam.logframelab.exception.UserProvisioningException;
import com.arqaam.logframelab.model.persistence.auth.User;
import com.arqaam.logframelab.repository.GroupRepository;
import com.arqaam.logframelab.service.auth.UserService;
import com.arqaam.logframelab.service.usermanager.mapper.UserMapper;
import java.util.List;
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
  private final UserMapper userMapper;

  public UserManagerImpl(UserService userService,
      GroupRepository groupRepository,
      PasswordEncoder passwordEncoder,
      UserMapper userMapper) {
    this.userService = userService;
    this.groupRepository = groupRepository;
    this.passwordEncoder = passwordEncoder;
    this.userMapper = userMapper;
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
    return userService.getAllUsers().stream().map(userMapper::userToDto)
        .collect(Collectors.toList());
  }
}
