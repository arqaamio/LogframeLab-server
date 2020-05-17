package com.arqaam.logframelab.auth;

import com.arqaam.logframelab.model.persistence.auth.Group;
import com.arqaam.logframelab.model.persistence.auth.GroupMember;
import com.arqaam.logframelab.model.persistence.auth.User;
import com.arqaam.logframelab.repository.GroupRepository;
import com.arqaam.logframelab.repository.UserRepository;
import com.arqaam.logframelab.repository.initializer.BaseDatabaseTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.*;

@DataJpaTest
public class AuthTests implements BaseDatabaseTest {

  private static final int USER_GROUP_ID = 2;
  private static final int SIZE_ONE = 1;
  private static final int FIRST_IN_LIST = 0;
  private static final int ADMIN_GROUP_ID = 1;
  private static final int SEC_ADMIN_GROUP_ID = 3;
  private static final int SIZE_TWO = 2;
  private static final int SIZE_THREE = 3;

  @Autowired
  private GroupRepository groupRepository;

  @Autowired
  private UserRepository userRepository;

  @Test
  public void whenUserIsCreatedInGroup_ThenCheckMembership() {
    User user = User.builder().username("user").password("password").enabled(true).build();
    Group userGroup =
        groupRepository
            .findById(USER_GROUP_ID)
            .orElseThrow(() -> new IllegalStateException("Group not found for " + USER_GROUP_ID));
    user.addGroup(userGroup);

    User savedUser = userRepository.save(user);

    Set<GroupMember> memberships = savedUser.getGroupMembership();

    collector.checkThat(memberships, hasSize(SIZE_ONE));
    collector.checkThat(
        memberships.toArray(new GroupMember[] {})[FIRST_IN_LIST].getGroup(), equalTo(userGroup));
    collector.checkThat(savedUser.getAuthorities(), hasSize(SIZE_THREE));
  }

  @Test
  public void whenUserIsCreatedInGroups_ThenCheckMembership() {
    User userInGroups = createUserInGroups();

    Set<GroupMember> memberships = userInGroups.getGroupMembership();

    collector.checkThat(memberships, hasSize(SIZE_TWO));
    collector.checkThat(
        memberships.stream()
            .map(groupMember -> groupMember.getGroup().getId())
            .collect(Collectors.toSet()),
        containsInAnyOrder(ADMIN_GROUP_ID, SEC_ADMIN_GROUP_ID));
    collector.checkThat(userInGroups.getAuthorities(), hasSize(SIZE_TWO));
  }

  @Test
  public void whenUserIsRemovedFromGroup_ThenCheckMembershipPersisted() {
    User userInGroups = createUserInGroups();

    userInGroups.removeGroup(
        groupRepository
            .findById(SEC_ADMIN_GROUP_ID)
            .orElseThrow(
                () -> new IllegalStateException("Group not found for " + SEC_ADMIN_GROUP_ID)));

    User userWithSingleGroup = userRepository.save(userInGroups);

    Set<GroupMember> groupMembership = userWithSingleGroup.getGroupMembership();

    collector.checkThat(groupMembership, hasSize(1));
    collector.checkThat(
        groupMembership.toArray(new GroupMember[] {})[FIRST_IN_LIST].getGroup().getId(),
        is(ADMIN_GROUP_ID));
  }

  private User createUserInGroups() {
    User adminUser = User.builder().username("admin").password("admin").enabled(true).build();
    List<Group> adminGroups =
        groupRepository.findAllById(Arrays.asList(ADMIN_GROUP_ID, SEC_ADMIN_GROUP_ID));
    adminUser.addGroups(adminGroups);

    return userRepository.save(adminUser);
  }
}
