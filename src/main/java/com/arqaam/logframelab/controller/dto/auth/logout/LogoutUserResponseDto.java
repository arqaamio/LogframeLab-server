package com.arqaam.logframelab.controller.dto.auth.logout;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LogoutUserResponseDto {
    private Boolean isLoggedOut;
}
