package com.arqaam.logframelab.service.auth;

import com.arqaam.logframelab.controller.dto.auth.GroupDto;
import com.arqaam.logframelab.model.persistence.auth.Group;
import com.arqaam.logframelab.repository.GroupRepository;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class GroupService {

  private final GroupRepository groupRepository;

  public GroupService(GroupRepository groupRepository) {
    this.groupRepository = groupRepository;
  }

  public Group findByGroupName(String groupName) {
    return groupRepository.findByName(groupName);
  }

  public Set<GroupDto> getAllGroups() {
    return groupRepository.findAllGroupsBy();
  }

}
