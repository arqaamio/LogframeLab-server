package com.arqaam.logframelab.repository;

import com.arqaam.logframelab.model.persistence.Level;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LevelRepository extends JpaRepository<Level, Long> {

  List<Level> findAll();

  List<Level> findAllByOrderByPriority();
}
