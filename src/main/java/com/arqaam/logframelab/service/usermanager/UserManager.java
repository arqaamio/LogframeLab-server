package com.arqaam.logframelab.service.usermanager;

import com.arqaam.logframelab.controller.dto.auth.UserDto;
import com.arqaam.logframelab.controller.dto.auth.create.UserAuthProvisioningRequestDto;
import com.arqaam.logframelab.model.persistence.auth.User;
import java.util.List;
import java.util.Optional;

public interface UserManager {

  User provisionUser(UserAuthProvisioningRequestDto authProvisioningRequest);

  List<UserDto> getUsers();

  Optional<UserDto> userExistsByUsername(String username);
}
