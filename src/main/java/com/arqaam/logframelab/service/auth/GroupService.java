package com.arqaam.logframelab.service.auth;

import com.arqaam.logframelab.model.persistence.auth.Group;

public interface GroupService {

  Group findByGroupName(String groupName);
}
