package com.arqaam.logframelab.service;

import com.arqaam.logframelab.model.persistence.auth.Group;

public interface GroupService {

  Group findByGroupName(String groupName);

}
