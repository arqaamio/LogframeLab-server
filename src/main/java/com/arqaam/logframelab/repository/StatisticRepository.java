package com.arqaam.logframelab.repository;

import com.arqaam.logframelab.model.persistence.Statistic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.Optional;

@Repository
public interface StatisticRepository extends JpaRepository<Statistic, String> {

    Optional<Statistic> findByDate(Date date);
}
