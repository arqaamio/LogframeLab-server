package com.arqaam.logframelab.controller.dto.auth.login;

import java.util.Collection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JwtAuthenticationTokenResponse {
  private String token;
  private String tokenType;
  private Long expiryDuration;
  private Collection<String> groups;
}
