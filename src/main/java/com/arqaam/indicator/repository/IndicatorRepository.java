package com.arqaam.indicator.repository;

import com.arqaam.indicator.model.Indicator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IndicatorRepository extends JpaRepository<Indicator,Long> {

    List<Indicator> findAll();
}