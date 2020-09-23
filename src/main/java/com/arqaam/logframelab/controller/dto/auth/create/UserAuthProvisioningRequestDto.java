package com.arqaam.logframelab.controller.dto.auth.create;

import lombok.Value;

import javax.validation.constraints.NotBlank;
import java.util.Collection;

@Value
public class UserAuthProvisioningRequestDto {

    @NotBlank
    String username;
    String password;
    Collection<Integer> groupIds;
}
