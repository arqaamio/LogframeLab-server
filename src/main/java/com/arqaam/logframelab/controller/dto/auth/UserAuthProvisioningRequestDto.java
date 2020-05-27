package com.arqaam.logframelab.controller.dto.auth;

import lombok.Value;

import java.util.Collection;

@Value
public class UserAuthProvisioningRequestDto {
  String username;
  String password;
  Collection<Integer> groupIds;
}
