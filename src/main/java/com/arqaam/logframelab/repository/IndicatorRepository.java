package com.arqaam.logframelab.repository;

import com.arqaam.logframelab.model.persistence.Indicator;
import com.arqaam.logframelab.model.projection.IndicatorFilters;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IndicatorRepository extends JpaRepository<Indicator, Long> {

  List<Indicator> findAll();

  @Query(value = "select * from IND_INDICATOR where THEMES in (:themes)" , nativeQuery = true)
  List<Indicator> getIndicatorsByThemes(@Param("themes") List<String> themesList);

  @Query(value = "select distinct(THEMES) from IND_INDICATOR where THEMES <> ''", nativeQuery = true)
  List<String> getThemes();

  List<IndicatorFilters> getAllBy();

  /**
   * Returns all the indicators that match the specification. Used for filters.
   * @return List of the indicators
   */
  List<Indicator> findAll(Specification<Indicator> specification);
}
