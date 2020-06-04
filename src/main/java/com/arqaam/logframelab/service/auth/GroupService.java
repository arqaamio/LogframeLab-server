package com.arqaam.logframelab.service.auth;

import com.arqaam.logframelab.controller.dto.auth.GroupDto;
import com.arqaam.logframelab.model.persistence.auth.Group;
import java.util.Set;

public interface GroupService {

  Group findByGroupName(String groupName);

  Set<GroupDto> getAllGroups();
}
