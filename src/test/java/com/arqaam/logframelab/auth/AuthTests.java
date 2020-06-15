package com.arqaam.logframelab.auth;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.arqaam.logframelab.model.persistence.auth.Group;
import com.arqaam.logframelab.model.persistence.auth.GroupMember;
import com.arqaam.logframelab.model.persistence.auth.User;
import com.arqaam.logframelab.repository.GroupRepository;
import com.arqaam.logframelab.repository.UserRepository;
import com.arqaam.logframelab.repository.initializer.BaseDatabaseTest;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
public class AuthTests implements BaseDatabaseTest {

  private static final int FIRST_IN_LIST = 0;
  private static final int SEC_ADMIN_GROUP_ID = 1, INDICATOR_ADMIN_GROUP_ID = 3, APP_USER_GROUP_ID = 2;
  private static final int SIZE_TWO = 2, SIZE_FOUR = 4, SIZE_THREE = 3, SIZE_ONE = 1;

  @Autowired
  private GroupRepository groupRepository;

  @Autowired
  private UserRepository userRepository;

  @Test
  public void whenUserIsCreatedInGroup_ThenCheckMembership() {
    User user = User.builder().username("user").password("password").enabled(true).build();
    Group userGroup =
        groupRepository
            .findById(APP_USER_GROUP_ID)
            .orElseThrow(
                () -> new IllegalStateException("Group not found for " + APP_USER_GROUP_ID));
    user.addGroup(userGroup);

    User savedUser = userRepository.save(user);

    Set<GroupMember> memberships = savedUser.getGroupMembership();

    assertAll(
        () -> assertThat(memberships, hasSize(SIZE_ONE)),
        () ->
            assertThat(
                memberships.toArray(new GroupMember[]{})[FIRST_IN_LIST].getGroup(), is(userGroup)),
        () -> assertThat(savedUser.getAuthorities(), hasSize(SIZE_THREE)));
  }

  @Test
  public void whenUserIsCreatedInGroups_ThenCheckMembership() {
    User userInGroups = createUserInGroups();

    Set<GroupMember> memberships = userInGroups.getGroupMembership();

    assertAll(
        () -> assertThat(memberships, hasSize(SIZE_TWO)),
        () ->
            assertThat(
                memberships.stream()
                    .map(groupMember -> groupMember.getGroup().getId())
                    .collect(Collectors.toSet()),
                containsInAnyOrder(INDICATOR_ADMIN_GROUP_ID, SEC_ADMIN_GROUP_ID)),
        () -> assertThat(userInGroups.getAuthorities(), hasSize(SIZE_FOUR)));
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

    assertAll(
        () -> assertThat(groupMembership, hasSize(SIZE_ONE)),
        () -> assertThat(
            groupMembership.toArray(new GroupMember[]{})[FIRST_IN_LIST].getGroup().getId(),
            is(INDICATOR_ADMIN_GROUP_ID)));
  }

  private User createUserInGroups() {
    User adminUser = User.builder().username("admin").password("admin").enabled(true).build();
    Collection<Group> adminGroups =
        groupRepository.findAllById(Arrays.asList(INDICATOR_ADMIN_GROUP_ID, SEC_ADMIN_GROUP_ID));
    adminUser.addGroups(adminGroups);

    return userRepository.save(adminUser);
  }
}
