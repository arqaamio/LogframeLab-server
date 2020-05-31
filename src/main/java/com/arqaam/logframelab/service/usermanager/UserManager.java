package com.arqaam.logframelab.service.usermanager;

import com.arqaam.logframelab.controller.dto.auth.create.UserAuthProvisioningRequestDto;
import com.arqaam.logframelab.model.persistence.auth.User;

public interface UserManager {

  User provisionUser(UserAuthProvisioningRequestDto authProvisioningRequest);

}
