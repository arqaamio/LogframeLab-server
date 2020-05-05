package com.arqaam.logframelab.repository;

import com.arqaam.logframelab.model.persistence.Indicator;
import com.arqaam.logframelab.model.projection.IndicatorFilters;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface IndicatorRepository extends JpaRepository<Indicator, Long> {

  List<Indicator> findAll();

  List<Indicator> findIndicatorByIdIn(Collection<Long> id);

  @Query(value = "select * from IND_INDICATOR where DESCRIPTION in (:themes)", nativeQuery = true)
  List<Indicator> getIndicatorsByThemes(@Param("themes") List<String> themesList);

  @Query(value = "select distinct(THEMES) from IND_INDICATOR where THEMES <> ''", nativeQuery = true)
  List<String> getThemes();

  List<IndicatorFilters> getAllBy();

  List<Indicator> getAllByThemesInAndSourceInAndLevel_IdInAndSdgCodeInAndCrsCodeIn(
      Collection<Object> themes,
      Collection<Object> source,
      Collection<Object> level,
      Collection<Object> sdg_code,
      Collection<Object> crs_code);
}
