package com.arqaam.logframelab.repository;

import com.arqaam.logframelab.model.persistence.TempIndicator;
import java.util.Collection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TempIndicatorRepository extends JpaRepository<TempIndicator, Long> {

  void deleteByIdIn(Collection<Long> id);
}
