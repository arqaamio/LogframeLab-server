package com.arqaam.logframelab;

import com.arqaam.logframelab.model.persistence.auth.User;
import com.arqaam.logframelab.service.GroupService;
import com.arqaam.logframelab.service.UserService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.security.crypto.password.PasswordEncoder;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import javax.annotation.PostConstruct;
import java.util.Optional;

@SpringBootApplication
@EnableSwagger2
@ConfigurationPropertiesScan("com.arqaam.logframelab.model.properties")
public class Application {

  private static final String SEC_ADMIN_GROUP_NAME = "SEC_ADMIN";
  private static final String PASSWORD_TO_BE_CHANGED = "password";
  private static final String DEFAULT_SEC_ADMIN_USERNAME = "secadmin";

  private final UserService userService;
  private final GroupService groupService;
  private final PasswordEncoder passwordEncoder;

  public Application(
      UserService userService, GroupService groupService, PasswordEncoder passwordEncoder) {
    this.userService = userService;
    this.groupService = groupService;
    this.passwordEncoder = passwordEncoder;
  }

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }

  @PostConstruct
  private void initSecurityAdminUser() {
    Optional<User> userByGroupName = userService.getFirstUserByGroupName(SEC_ADMIN_GROUP_NAME);

    if (!userByGroupName.isPresent()) {
      User secAdminUser =
          User.builder()
              .username(DEFAULT_SEC_ADMIN_USERNAME)
              .password(passwordEncoder.encode(PASSWORD_TO_BE_CHANGED))
              .enabled(true)
              .build();
      secAdminUser.addGroup(groupService.findByGroupName(SEC_ADMIN_GROUP_NAME));
    }
  }
}
