package com.arqaam.logframelab.controller.dto.auth.refresh;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshTokenResponseDto {
    private String token;
    private String tokenType;
    private Collection<String> groups;
}
