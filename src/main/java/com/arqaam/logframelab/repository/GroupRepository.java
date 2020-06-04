package com.arqaam.logframelab.repository;

import com.arqaam.logframelab.controller.dto.auth.GroupDto;
import com.arqaam.logframelab.model.persistence.auth.Group;
import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupRepository extends JpaRepository<Group, Integer> {

  Group findByName(String name);

  Set<GroupDto> findAllGroupsBy();

}
