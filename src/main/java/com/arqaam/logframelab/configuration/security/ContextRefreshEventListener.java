package com.arqaam.logframelab.configuration.security;

import com.arqaam.logframelab.model.persistence.auth.User;
import com.arqaam.logframelab.service.auth.GroupService;
import com.arqaam.logframelab.service.auth.UserService;
import java.util.List;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ContextRefreshEventListener {

  private static final String SEC_ADMIN_GROUP_NAME = "SEC_ADMIN";
  private static final String PASSWORD_TO_BE_CHANGED = "password";
  private static final String DEFAULT_SEC_ADMIN_USERNAME = "secadmin";
  private static final String SYSTEM = "SYSTEM";

  private final UserService userService;
  private final GroupService groupService;
  private final PasswordEncoder passwordEncoder;

  public ContextRefreshEventListener(
      UserService userService, GroupService groupService, PasswordEncoder passwordEncoder) {
    this.userService = userService;
    this.groupService = groupService;
    this.passwordEncoder = passwordEncoder;
  }

  @EventListener
  public void initSecurityAdminUser(ContextRefreshedEvent event) {
    List<User> userByGroupName = userService.getFirstUserByGroupName(SEC_ADMIN_GROUP_NAME);

    if (!userByGroupName.isEmpty()) {
      User secAdminUser =
          User.builder()
              .username(DEFAULT_SEC_ADMIN_USERNAME)
              .password(passwordEncoder.encode(PASSWORD_TO_BE_CHANGED))
              .enabled(true)
              .createdBy(SYSTEM)
              .build();
      secAdminUser.addGroup(groupService.findByGroupName(SEC_ADMIN_GROUP_NAME));

      userService.save(secAdminUser);
    }
  }
}
