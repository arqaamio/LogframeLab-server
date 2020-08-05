package com.arqaam.logframelab.repository;

import com.arqaam.logframelab.model.persistence.Indicator;
import com.arqaam.logframelab.model.projection.IndicatorFilters;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface IndicatorRepository extends JpaRepository<Indicator, Long>, JpaSpecificationExecutor<Indicator> {

  List<Indicator> findAll();

  List<Indicator> findAllByTempEquals(Boolean temp);

  List<Indicator> findIndicatorByIdIn(Collection<Long> id);

  @Query(value = "select * from IND_INDICATOR where THEMES in (:themes)", nativeQuery = true)
  List<Indicator> getIndicatorsByThemes(@Param("themes") List<String> themesList);

  @Query(value = "select distinct(THEMES) from IND_INDICATOR where THEMES <> ''", nativeQuery = true)
  List<String> getThemes();

  List<IndicatorFilters> getAllBy();

  /**
   * Returns all the indicators that match the specification. Used for filters.
   *
   * @return List of the indicators
   */
  List<Indicator> findAll(Specification<Indicator> specification);

  /*
   * TODO Currently, a call to this method generates one query for indicators and
   *  another for level. An improvement could be
   *  @Query(value = "from Indicator i join i.level l"). However, that results in a
   *  slightly different result. Reason for this should be investigated.
   */
  Page<Indicator> findAll(Pageable page);

  @Override
  Page<Indicator> findAll(Specification<Indicator> specification, Pageable page);

  @Modifying
  @Query(value = "DELETE FROM Indicator ind WHERE ind.id in :ids")
  void deleteDisapprovedByIds(@Param("ids") Collection<Long> ids);

  @Modifying
  @Query(value = "UPDATE Indicator ind set ind.temp = false WHERE ind.id in :ids")
  void updateToApproved(@Param("ids") Collection<Long> ids);

  @Override
  <S extends Indicator> boolean exists(Example<S> example);

  /**
   * Returns all indicators that have a name in the iterable
   * @param names Names to be searched
   * @return Indicators that have the names searched
   */
  List<Indicator> findAllByNameIn(Iterable<String> names);
}
