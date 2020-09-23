package com.arqaam.logframelab.controller.dto.auth.logout;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LogoutUserRequestDto {
  @NotBlank
  String username;
}
