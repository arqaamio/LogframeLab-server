package com.arqaam.logframelab.controller;

import com.arqaam.logframelab.controller.dto.auth.GroupDto;
import com.arqaam.logframelab.controller.dto.auth.UserDto;
import com.arqaam.logframelab.controller.dto.auth.create.UserAuthProvisioningRequestDto;
import com.arqaam.logframelab.controller.dto.auth.create.UserAuthProvisioningResponseDto;
import com.arqaam.logframelab.controller.dto.auth.login.AuthenticateUserRequestDto;
import com.arqaam.logframelab.controller.dto.auth.login.JwtAuthenticationTokenResponse;
import com.arqaam.logframelab.controller.dto.auth.logout.LogoutUserRequestDto;
import com.arqaam.logframelab.controller.dto.auth.logout.LogoutUserResponseDto;
import com.arqaam.logframelab.exception.UserNotFoundException;
import com.arqaam.logframelab.model.Error;
import com.arqaam.logframelab.model.persistence.auth.User;
import com.arqaam.logframelab.service.auth.AuthService;
import com.arqaam.logframelab.service.auth.GroupService;
import com.arqaam.logframelab.service.auth.UserService;
import com.arqaam.logframelab.util.Logging;
import io.swagger.annotations.*;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@CrossOrigin("*")
@RestController
@RequestMapping(
        value = "auth",
        produces = MediaType.APPLICATION_JSON_VALUE)
@Api(tags = "Authentication")
public class AuthController implements Logging {

  private final AuthService authService;
  private final UserService userService;
  private final GroupService groupService;

  public AuthController(AuthService authService, UserService userService, GroupService groupService) {
    this.authService = authService;
    this.userService = userService;
    this.groupService = groupService;
  }

  @PostMapping(value = "login", consumes = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("isAnonymous()")
  @ApiOperation(value = "${AuthController.authenticateUser.value}", nickname = "authenticateUser", response = JwtAuthenticationTokenResponse.class)
  @ApiResponses({
          @ApiResponse(code = 200, message = "The user was authenticated", response = JwtAuthenticationTokenResponse.class),
          @ApiResponse(code = 500, message = "Failed to authenticate the user", response = Error.class)
  })
  public ResponseEntity<JwtAuthenticationTokenResponse> authenticateUser(
          @Valid @RequestBody AuthenticateUserRequestDto request) {
    logger().info("Starting to authenticate user with username: {}", request.getUsername());
    Authentication authentication = authService.authenticateUser(request.getUsername(), request.getPassword());

    User user = (User) authentication.getPrincipal();
    SecurityContextHolder.getContext().setAuthentication(authentication);

    String token = authService.generateToken(user);
    return ResponseEntity.ok(JwtAuthenticationTokenResponse.builder().token(token)
            .tokenType(authService.getTokenType())
            .groups(user.getGroupMembership().stream().map(m -> m.getGroup().getName()).collect(
                    Collectors.toSet())).build());
  }

  @PostMapping(value = "logout", consumes = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("isAuthenticated()")
  @ApiOperation(value = "${AuthController.userLogout.value}", nickname = "userLogout",
          response = LogoutUserResponseDto.class, authorizations = {@Authorization(value = "jwtToken")})
  @ApiResponses({
          @ApiResponse(code = 200, message = "The user was logged out", response = LogoutUserResponseDto.class),
          @ApiResponse(code = 500, message = "Failed to log out the user", response = Error.class)
  })
  public ResponseEntity<LogoutUserResponseDto> userLogout(
          @Valid @RequestBody LogoutUserRequestDto request) {
    logger().info("Starting to log out user with username: {}", request.getUsername());
    authService.logout(request.getUsername());
    return ResponseEntity.ok(new LogoutUserResponseDto(true));
  }


  @PostMapping(value = "users", consumes = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasAnyAuthority('CRUD_ADMIN', 'CRUD_APP_USER')")
  @ApiOperation(value = "${AuthController.createUser.value}", nickname = "createUser",
          response = UserAuthProvisioningResponseDto.class, authorizations = {@Authorization(value = "jwtToken")})
  @ApiResponses({
          @ApiResponse(code = 200, message = "The user was created", response = UserAuthProvisioningResponseDto.class),
          @ApiResponse(code = 500, message = "Failed to create a user", response = Error.class)
  })
  public ResponseEntity<UserAuthProvisioningResponseDto> createUser(
          @Valid @RequestBody UserAuthProvisioningRequestDto request) {
    logger().info("Starting to create a new user with username: {} and groupIds: {}",
            request.getUsername(), request.getGroupIds());
    User user = userService.provisionUser(request);

    return ResponseEntity.ok(new UserAuthProvisioningResponseDto(user.getUsername(),
            user.getGroupMembership().stream().map(m -> m.getGroup().getName())
                    .collect(Collectors.toSet())));
  }

  @GetMapping("users")
  @PreAuthorize("hasAnyAuthority('CRUD_ADMIN', 'CRUD_APP_USER')")
  @ApiOperation(value = "${AuthController.getUsers.value}", nickname = "getUsers",
          response = UserDto.class, authorizations = {@Authorization(value = "jwtToken")}, responseContainer = "List")
  @ApiResponses({
          @ApiResponse(code = 200, message = "Retrieved all users", response = UserDto.class, responseContainer = "List"),
          @ApiResponse(code = 500, message = "Failed to retrieve the users", response = Error.class)
  })
  public ResponseEntity<List<UserDto>> getUsers() {
    logger().info("Starting to retrieve all users");
    return ResponseEntity.ok(userService.getUsersAndGroups());
  }

  @GetMapping("groups")
  @PreAuthorize("hasAnyAuthority('CRUD_ADMIN', 'CRUD_APP_USER')")
  @ApiOperation(value = "${AuthController.getGroups.value}", nickname = "getGroups",
          response = GroupDto.class,responseContainer = "Set", authorizations = { @Authorization(value="jwtToken") })
  @ApiResponses({
          @ApiResponse(code = 200, message = "Retrieved all groups", response = GroupDto.class, responseContainer = "Set"),
          @ApiResponse(code = 500, message = "Failed to retrieve the groups", response = Error.class)
  })
  public ResponseEntity<Set<GroupDto>> getGroups() {
    logger().info("Starting to retrieve all groups");
    return ResponseEntity.ok(groupService.getAllGroups());
  }

  @GetMapping(value = "users/{username}")
  @PreAuthorize("hasAnyAuthority('CRUD_ADMIN', 'CRUD_APP_USER')")
  @ApiOperation(value = "${AuthController.getUser.value}", nickname = "getUser", response = UserDto.class, authorizations = { @Authorization(value="jwtToken") })
  @ApiResponses({
          @ApiResponse(code = 200, message = "Retrieved the user", response = UserDto.class),
          @ApiResponse(code = 404, message = "User not found", response = Error.class),
          @ApiResponse(code = 500, message = "Failed to retrieve the user", response = Error.class)
  })
  public ResponseEntity<UserDto> getUser(@PathVariable("username") String username) {
    logger().info("Starting to retrieve user with username: {}", username);
    Optional<User> user = userService.findByUsername(username);
    if (user.isEmpty()) {
      logger().error("Failed to find user with username: {}", username);
      throw new UserNotFoundException();
    }
    return ResponseEntity.ok(new UserDto(user.get().getUsername(),
            user.get().getGroupMembership().stream().map(gm -> gm.getGroup().getName()).collect(Collectors.toList())));
  }

  @DeleteMapping(value = "users/{username}")
  @PreAuthorize("hasAnyAuthority('CRUD_ADMIN', 'CRUD_APP_USER')")
  @ApiOperation(value = "${AuthController.deleteUser.value}", nickname = "deleteUser", authorizations = { @Authorization(value="jwtToken") })
  @ApiResponses({
          @ApiResponse(code = 200, message = "Deleted the user"),
          @ApiResponse(code = 404, message = "User not found", response = Error.class),
          @ApiResponse(code = 500, message = "Failed to delete the user", response = Error.class)
  })
  public ResponseEntity<Void> deleteUser(@PathVariable("username") String username) {
    logger().info("Deleting the user with username: {}", username);
    Optional<User> user = userService.findByUsername(username);
    if (user.isEmpty()) {
      logger().error("Failed to find user with username: {}", username);
      throw new UserNotFoundException();
    }
    userService.deleteUserById(username);
    return ResponseEntity.ok().build();
  }
}
