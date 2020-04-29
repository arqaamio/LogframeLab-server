package com.arqaam.logframelab.auth;

import com.arqaam.logframelab.model.persistence.auth.Group;
import com.arqaam.logframelab.model.persistence.auth.User;
import com.arqaam.logframelab.repository.GroupRepository;
import com.arqaam.logframelab.repository.UserRepository;
import org.flywaydb.test.annotation.FlywayTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.logging.Logger;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
//@FlywayTest
public class AuthTests {

  private final Logger logger = Logger.getLogger(AuthTests.class.getName());
  @Autowired private GroupRepository groupRepository;
  @Autowired private UserRepository userRepository;

  @Test
  public void createUserInGroupTest() {
    User user = User.builder().username("user").password("password").enabled(true).build();
    Group userGroup =
        groupRepository
            .findById(2)
            .orElseThrow(() -> new IllegalStateException("Group not found for 2"));
    user.addGroup(userGroup);

    logger.info(userRepository.save(user).toString());
  }
}
