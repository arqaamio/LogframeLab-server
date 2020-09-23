package com.arqaam.logframelab.service.auth;

import com.arqaam.logframelab.controller.dto.auth.UserDto;
import com.arqaam.logframelab.controller.dto.auth.create.UserAuthProvisioningRequestDto;
import com.arqaam.logframelab.exception.UserProvisioningException;
import com.arqaam.logframelab.model.persistence.auth.User;
import com.arqaam.logframelab.repository.GroupRepository;
import com.arqaam.logframelab.repository.UserRepository;
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
    String userId = "1";
    userService.deleteUserById(userId);
    verify(userRepository).deleteById(userId);
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
    when(userRepository.findByUsername(username)).thenReturn(Optional.empty());
    when(groupRepository.findAllById(groups)).thenReturn(Collections.emptyList());
    when(userRepository.save(any())).thenReturn(expected);

    User result = userService.provisionUser(dto);
    verify(userRepository).findByUsername(username);
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
    when(userRepository.findByUsername(username)).thenReturn(Optional.of(expected));
    when(groupRepository.findAllById(groups)).thenReturn(Collections.emptyList());
    when(userRepository.save(any())).thenReturn(expected);

    User result = userService.provisionUser(dto);
    verify(userRepository).findByUsername(username);
    verify(groupRepository).findAllById(groups);
    verify(userRepository).save(any());
    assertEquals(expected, result);
  }

  @Test
  void provisionUserTest_noPassword(){
    String username = "username";
    List<Integer> groups = Collections.singletonList(1);
    UserAuthProvisioningRequestDto dto = new UserAuthProvisioningRequestDto(username, "", groups);

    when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

    assertThrows(UserProvisioningException.class, () -> userService.provisionUser(dto));
    verify(userRepository).findByUsername(username);
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
