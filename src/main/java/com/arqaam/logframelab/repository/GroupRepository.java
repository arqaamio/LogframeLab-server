package com.arqaam.logframelab.repository;

import com.arqaam.logframelab.controller.dto.auth.GroupDto;
import com.arqaam.logframelab.model.persistence.auth.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface GroupRepository extends JpaRepository<Group, Integer> {

    Group findByName(String name);

    Set<GroupDto> findAllGroupsBy();

}
