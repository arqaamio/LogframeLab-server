package com.arqaam.logframelab.controller.dto.auth.login;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JwtAuthenticationTokenResponse {
    private String token;
    private String tokenType;
    private Collection<String> groups;
}
