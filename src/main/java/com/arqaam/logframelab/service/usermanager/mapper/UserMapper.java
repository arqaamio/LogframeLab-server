package com.arqaam.logframelab.service.usermanager.mapper;

import com.arqaam.logframelab.controller.dto.auth.UserDto;
import com.arqaam.logframelab.model.persistence.auth.GroupMember;
import com.arqaam.logframelab.model.persistence.auth.User;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

  @Mapping(source = "groupMembership", target = "groups")
  UserDto userToDto(User user);

  default Collection<String> groupMembershipToGroups(Set<GroupMember> groupMembership) {
    return groupMembership.stream().map(membership -> membership.getGroup().getName())
        .collect(Collectors.toSet());
  }
}
