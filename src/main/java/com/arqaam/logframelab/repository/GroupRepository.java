package com.arqaam.logframelab.repository;

import com.arqaam.logframelab.model.persistence.auth.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupRepository extends JpaRepository<Group, Integer> {}
