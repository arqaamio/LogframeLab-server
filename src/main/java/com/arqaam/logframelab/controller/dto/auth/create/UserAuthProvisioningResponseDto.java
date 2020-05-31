package com.arqaam.logframelab.controller.dto.auth.create;

import java.util.Collection;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
public class UserAuthProvisioningResponseDto {

  String username;
  Collection<String> groups;
}
