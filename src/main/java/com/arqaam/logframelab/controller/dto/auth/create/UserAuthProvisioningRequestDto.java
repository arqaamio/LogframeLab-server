package com.arqaam.logframelab.controller.dto.auth.create;

import java.util.Collection;
import javax.validation.constraints.NotBlank;
import lombok.Value;

@Value
public class UserAuthProvisioningRequestDto {

  @NotBlank
  String username;
  String password;
  Collection<Integer> groupIds;
}
