package com.arqaam.indicator.repository;

import com.arqaam.indicator.model.Level;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


public interface LevelRepository extends JpaRepository<Level, Long> {

    List<Level> findAll();

    Level findLevelByName(String name);
}
