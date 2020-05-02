package com.arqaam.logframelab.repository;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import com.arqaam.logframelab.model.persistence.Indicator;
import com.arqaam.logframelab.model.projection.IndicatorFilters;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface IndicatorRepository extends JpaRepository<Indicator, Long> {

  List<Indicator> findAll();

  @Query(value = "select * from IND_INDICATOR where DESCRIPTION in (:themes)", nativeQuery = true)
  List<Indicator> getIndicatorsByThemes(@Param("themes") List<String> themesList);

  @Query(value = "select distinct(DESCRIPTION) from IND_INDICATOR where DESCRIPTION <> ''", nativeQuery = true)
  List<String> getThemes();

  List<IndicatorFilters> getAllBy();

  @Query(value = "select * from IND_INDICATOR where :filters", nativeQuery=true)
  List<Indicator> findAllByFilters(@Param("filters") String filters);

  default List<Indicator> findAllByFilters(EntityManager entityManager, String whereClause) {
    TypedQuery<Indicator> query = entityManager.createQuery("select i from IND_INDICATOR i" + (whereClause.isEmpty() ? "" : " where " + whereClause), Indicator.class);
    return query.getResultList();
  }

  default String toSqlConditions(Map<String, Collection<Object>> filterOptions) {
    final StringBuilder queryConditions = new StringBuilder();
    filterOptions.forEach(
        (key, value) -> {
          if (value != null && !value.isEmpty()) {
            queryConditions
                .append(key)
                .append(" in ")
                .append("(")
                .append(value.stream().map(val -> (val instanceof Number) ? val.toString() : ("'" + val.toString() + "'")).collect(Collectors.joining(",")))
                .append(") and ");
          }
        });
    if (queryConditions.indexOf(" and ") > 0) {
      queryConditions.delete(queryConditions.lastIndexOf(" and "), queryConditions.length());
    }
    
    return queryConditions.toString();
  }
}
