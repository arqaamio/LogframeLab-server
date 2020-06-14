package com.arqaam.logframelab.controller;

import com.arqaam.logframelab.controller.dto.auth.GroupDto;
import com.arqaam.logframelab.controller.dto.auth.UserDto;
import com.arqaam.logframelab.controller.dto.auth.create.UserAuthProvisioningRequestDto;
import com.arqaam.logframelab.controller.dto.auth.create.UserAuthProvisioningResponseDto;
import com.arqaam.logframelab.controller.dto.auth.login.AuthenticateUserRequestDto;
import com.arqaam.logframelab.controller.dto.auth.login.JwtAuthenticationTokenResponse;
import com.arqaam.logframelab.controller.dto.auth.logout.LogoutUserRequestDto;
import com.arqaam.logframelab.controller.dto.auth.logout.LogoutUserResponseDto;
import com.arqaam.logframelab.exception.UnauthorizedException;
import com.arqaam.logframelab.model.persistence.auth.User;
import com.arqaam.logframelab.service.auth.AuthService;
import com.arqaam.logframelab.service.auth.GroupService;
import com.arqaam.logframelab.service.usermanager.UserManager;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.validation.Valid;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin("*")
@RestController
@RequestMapping(
    value = "auth",
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE)
public class AuthController {

  private final AuthService authService;
  private final UserManager userManager;
  private final GroupService groupService;

  public AuthController(AuthService authService, UserManager userManager, GroupService groupService) {
    this.authService = authService;
    this.userManager = userManager;
    this.groupService = groupService;
  }

  @PostMapping(value = "login")
  @PreAuthorize("isAnonymous()")
  public ResponseEntity<JwtAuthenticationTokenResponse> authenticateUser(
      @Valid @RequestBody AuthenticateUserRequestDto authenticateUserRequest) {
    Authentication authentication =
        authService
            .authenticateUser(authenticateUserRequest.getUsername(),
                authenticateUserRequest.getPassword())
            .orElseThrow(() -> new UnauthorizedException("Unable to authenticate user"));

    User user = (User) authentication.getPrincipal();
    SecurityContextHolder.getContext().setAuthentication(authentication);

    String token = authService.generateToken(user);
    return ResponseEntity.ok(JwtAuthenticationTokenResponse.builder().token(token)
        .tokenType(authService.getTokenType()).expiryDuration(
            authService.getTokenExpiryInMillis())
        .groups(user.getGroupMembership().stream().map(m -> m.getGroup().getName()).collect(
            Collectors.toSet())).build());
  }

  @PostMapping("logout")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<LogoutUserResponseDto> userLogout(
      @Valid @RequestBody LogoutUserRequestDto logoutUserRequest) {
    authService.logout(logoutUserRequest.getUsername());
    return ResponseEntity.ok(new LogoutUserResponseDto(true));
  }


  @PostMapping("users")
  @PreAuthorize("hasAnyAuthority('CRUD_ADMIN', 'CRUD_APP_USER')")
  public ResponseEntity<UserAuthProvisioningResponseDto> provisionUser(
      @Valid @RequestBody UserAuthProvisioningRequestDto authProvisioningRequest) {
    User user = userManager.provisionUser(authProvisioningRequest);

    return ResponseEntity.ok(new UserAuthProvisioningResponseDto(user.getUsername(),
        user.getGroupMembership().stream().map(m -> m.getGroup().getName())
            .collect(Collectors.toSet())));
  }

  @GetMapping("users")
  @PreAuthorize("hasAnyAuthority('CRUD_ADMIN', 'CRUD_APP_USER')")
  public ResponseEntity<List<UserDto>> getUsers() {
    return ResponseEntity.ok(userManager.getUsers());
  }

  @GetMapping("groups")
  @PreAuthorize("hasAnyAuthority('CRUD_ADMIN', 'CRUD_APP_USER')")
  public ResponseEntity<Set<GroupDto>> getGroups() {
    return ResponseEntity.ok(groupService.getAllGroups());
  }

  @GetMapping(value = "users/{username}")
  @PreAuthorize("hasAnyAuthority('CRUD_ADMIN', 'CRUD_APP_USER')")
  public ResponseEntity<UserDto> getUser(@PathVariable("username") String username) {
    Optional<UserDto> userDto = userManager.userExistsByUsername(username);
    return userDto.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
  }

}
