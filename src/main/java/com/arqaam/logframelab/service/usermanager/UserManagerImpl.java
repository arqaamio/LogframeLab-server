package com.arqaam.logframelab.service.usermanager;

import com.arqaam.logframelab.controller.dto.auth.UserAuthProvisioningRequestDto;
import com.arqaam.logframelab.exception.UserProvisioningException;
import com.arqaam.logframelab.model.persistence.auth.User;
import com.arqaam.logframelab.repository.GroupRepository;
import com.arqaam.logframelab.service.auth.UserService;
import java.util.Optional;
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
  public Optional<User> provisionUser(UserAuthProvisioningRequestDto authProvisioningRequest) {
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

    return Optional.of(userService.save(user));
  }
}
