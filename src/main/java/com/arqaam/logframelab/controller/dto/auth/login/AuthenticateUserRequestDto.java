package com.arqaam.logframelab.controller.dto.auth.login;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@AllArgsConstructor
public class AuthenticateUserRequestDto {

    @NotBlank
    private String username;

    @NotBlank
  private String password;
}
