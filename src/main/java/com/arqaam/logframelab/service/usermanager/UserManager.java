package com.arqaam.logframelab.service.usermanager;

import com.arqaam.logframelab.controller.dto.auth.UserAuthProvisioningRequestDto;
import com.arqaam.logframelab.model.persistence.auth.User;
import java.util.Optional;

public interface UserManager {

  Optional<User> provisionUser(UserAuthProvisioningRequestDto authProvisioningRequest);

}
