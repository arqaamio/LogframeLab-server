package com.arqaam.logframelab.controller.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AuthenticateUserRequestDto {

  private String username;

  private String password;
}
