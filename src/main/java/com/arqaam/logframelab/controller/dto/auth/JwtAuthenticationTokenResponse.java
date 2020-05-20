package com.arqaam.logframelab.controller.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class JwtAuthenticationTokenResponse {
  private String token;
  private String tokenType;
  private Long expiryDuration;

}
