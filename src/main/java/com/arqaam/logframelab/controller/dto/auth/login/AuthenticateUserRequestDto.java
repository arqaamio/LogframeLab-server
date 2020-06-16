package com.arqaam.logframelab.controller.dto.auth.login;

import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AuthenticateUserRequestDto {

  @NotBlank
  private String username;

  @NotBlank
  private String password;
}
