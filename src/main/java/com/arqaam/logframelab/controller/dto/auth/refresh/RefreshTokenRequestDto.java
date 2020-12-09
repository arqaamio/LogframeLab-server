package com.arqaam.logframelab.controller.dto.auth.refresh;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenRequestDto {
    @NotBlank
    String token;
}
