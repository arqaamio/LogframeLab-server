package com.arqaam.logframelab.service.auth;

import com.arqaam.logframelab.controller.dto.auth.UserDto;
import com.arqaam.logframelab.controller.dto.auth.create.UserAuthProvisioningRequestDto;
import com.arqaam.logframelab.exception.OnlySecAdminUserException;
import com.arqaam.logframelab.exception.UserNotFoundException;
import com.arqaam.logframelab.exception.UserProvisioningException;
import com.arqaam.logframelab.model.persistence.auth.Group;
import com.arqaam.logframelab.model.persistence.auth.User;
import com.arqaam.logframelab.repository.GroupRepository;
import com.arqaam.logframelab.repository.UserRepository;
import com.arqaam.logframelab.util.Constants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
  
  @Mock
  UserRepository userRepository;
  @Mock
  GroupRepository groupRepository;
  @Mock
  PasswordEncoder passwordEncoder;
  
  @InjectMocks
  UserService userService;

  @Test
  void getUserByGroupNameTest(){
    List<User> expected = Collections.singletonList(new User());
    String groupName = "GROUP NAME";
    when(userRepository.findUserByGroupMembership(groupName)).thenReturn(expected);

    List<User> result = userService.getUserByGroupName(groupName);
    verify(userRepository).findUserByGroupMembership(groupName);
    assertEquals(expected, result);
  }

  @Test
  void createOrUpdateUserTest(){
    User expected = new User();
    when(userRepository.save(expected)).thenReturn(expected);

    User result = userService.createOrUpdateUser(expected);
    verify(userRepository).save(expected);
    assertEquals(expected, result);
  }

  @Test
  void deleteUserByIdTest(){
    String userId = "secadmin";
    User user = User.builder().username(userId).build();
    when(userRepository.findByUsername(userId)).thenReturn(Optional.of(user));
    userService.deleteUserById(userId);
    verify(userRepository).deleteById(userId);
  }

  @Test
  void deleteUserByIdTest_userNotFound(){
    assertThrows(UserNotFoundException.class, ()->{
      String userId = "secadmin";
      when(userRepository.findByUsername(userId)).thenReturn(Optional.empty());
      userService.deleteUserById(userId);
    });
  }

  @Test
  void deleteUserByIdTest_onlySecAdmin(){
    assertThrows(OnlySecAdminUserException.class, ()->{
      String userId = "secadmin";
      User user = User.builder().username(userId).build();
      Group group = new Group();
      group.setName(Constants.SEC_ADMIN_GROUP_NAME);
      user.addGroup(group);
      when(userRepository.findByUsername(userId)).thenReturn(Optional.of(user));
      when(userRepository.findUserByGroupMembership(any())).thenReturn(Collections.singletonList(user));

      userService.deleteUserById(userId);
    });
  }

  @Test
  void findByUsernameTest(){
    String username = "username";
    Optional<User> expected = Optional.of(new User());
    when(userRepository.findByUsername(username)).thenReturn(expected);

    Optional<User> result = userService.findByUsername(username);
    verify(userRepository).findByUsername(username);
    assertEquals(expected, result);
  }

  @Test
  void existsUserByUsernameTest(){
    String username = "username";
    boolean expected = true;
    when(userRepository.existsByUsername(username)).thenReturn(expected);

    boolean result = userService.existsUserByUsername(username);
    verify(userRepository).existsByUsername(username);
    assertEquals(expected, result);
  }

  @Test
  void provisionUserTest(){
    String username = "username";
    List<Integer> groups = Collections.emptyList();
    UserAuthProvisioningRequestDto dto = new UserAuthProvisioningRequestDto(username, "password", groups);

    User expected = new User();
    expected.setUsername(username);
    expected.addGroups(Collections.emptyList());
    when(groupRepository.findAllById(groups)).thenReturn(Collections.emptyList());
    when(userRepository.save(any())).thenReturn(expected);

    User result = userService.provisionUser(dto);
    verify(groupRepository).findAllById(groups);
    verify(userRepository).save(any());
    assertEquals(expected, result);
  }

  @Test
  void provisionUserTest_alreadyExisting(){
    String username = "username";
    List<Integer> groups = Collections.singletonList(1);
    UserAuthProvisioningRequestDto dto = new UserAuthProvisioningRequestDto(username, "password", groups);

    User expected = new User();
    expected.setUsername(username);
    expected.addGroups(Collections.emptyList());
    when(groupRepository.findAllById(groups)).thenReturn(Collections.emptyList());
    when(userRepository.save(any())).thenReturn(expected);

    User result = userService.provisionUser(dto);
    verify(groupRepository).findAllById(groups);
    verify(userRepository).save(any());
    assertEquals(expected, result);
  }

  @Test
  void provisionUserTest_noPassword(){
    String username = "username";
    List<Integer> groups = Collections.singletonList(1);
    UserAuthProvisioningRequestDto dto = new UserAuthProvisioningRequestDto(username, "", groups);
    assertThrows(UserProvisioningException.class, () -> userService.provisionUser(dto));
    verify(groupRepository, times(0)).findAllById(groups);
    verify(userRepository, times(0)).save(any());
  }

  @Test
  void getUsersAndGroupsTest(){
    User user = new User();
    user.setUsername("Username");
    user.addGroups(Collections.emptyList());
    List<User> users = Collections.singletonList(user);
    List<UserDto> expected = Collections.singletonList(new UserDto("Username", Collections.emptyList()));
    when(userRepository.getAllUsersWithTheirGroups()).thenReturn(users);

    List<UserDto> result = userService.getUsersAndGroups();
    verify(userRepository).getAllUsersWithTheirGroups();
    assertEquals(expected, result);
  }
}
