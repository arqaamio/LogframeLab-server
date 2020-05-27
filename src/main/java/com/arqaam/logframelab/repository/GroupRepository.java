package com.arqaam.logframelab.repository;

import com.arqaam.logframelab.model.persistence.auth.Group;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupRepository extends JpaRepository<Group, Integer> {
  Group findByName(String name);

  Collection<Group> findAllById(Collection<Integer> ids);
}
