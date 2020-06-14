package com.arqaam.logframelab.controller.dto.auth.logout;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LogoutUserResponseDto {
  private boolean isLoggedOut;
}
