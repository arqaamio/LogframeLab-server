package com.arqaam.logframelab.service.auth;

import com.arqaam.logframelab.model.persistence.auth.Group;
import com.arqaam.logframelab.repository.GroupRepository;
import org.springframework.stereotype.Service;

@Service
public class GroupServiceImpl implements GroupService {

  private final GroupRepository groupRepository;

  public GroupServiceImpl(GroupRepository groupRepository) {
    this.groupRepository = groupRepository;
  }

  @Override
  public Group findByGroupName(String groupName) {
    return groupRepository.findByName(groupName);
  }
}
