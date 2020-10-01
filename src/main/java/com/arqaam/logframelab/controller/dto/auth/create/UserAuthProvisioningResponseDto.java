package com.arqaam.logframelab.controller.dto.auth.create;

import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.Collection;

@Value
@AllArgsConstructor
public class UserAuthProvisioningResponseDto {

    String username;
    Collection<String> groups;
}
