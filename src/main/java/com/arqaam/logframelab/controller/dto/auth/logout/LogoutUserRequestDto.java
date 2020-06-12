package com.arqaam.logframelab.controller.dto.auth.logout;

import javax.validation.constraints.NotBlank;
import lombok.Value;

@Value
public class LogoutUserRequestDto {
  @NotBlank
  String username;
}
