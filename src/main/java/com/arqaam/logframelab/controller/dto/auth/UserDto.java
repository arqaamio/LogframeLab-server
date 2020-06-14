package com.arqaam.logframelab.controller.dto.auth;

import java.util.Collection;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UserDto {
  String username;
  Collection<String> groups;
}
