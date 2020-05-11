package com.arqaam.logframelab.controller;

import com.arqaam.logframelab.controller.dto.FiltersDto;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Objects;

import static org.hamcrest.CoreMatchers.equalTo;

public class IndicatorControllerIntegrationTest extends BaseControllerTest {

  private static final int DATABASE_THEMES_SIZE = 42;
  private static final int DATABASE_CRS_CODE_SIZE = 76;
  private static final int DATABASE_SOURCE_SIZE = 26;
  private static final int DATABASE_SDG_CODE_SIZE = 168;
  private static final int DATABASE_LEVEL_SIZE = 3;

  @Test
  public void whenFiltersRequested_ThenFiltersReturned() {
    ResponseEntity<FiltersDto> filters =
        testRestTemplate.getForEntity("/indicator/filters", FiltersDto.class);
    FiltersDto filtersDto = Objects.requireNonNull(filters.getBody());

    collector.checkThat(filters.getStatusCode(), equalTo(HttpStatus.OK));
    collector.checkThat(filtersDto.getThemes().size(), equalTo(DATABASE_THEMES_SIZE));
    collector.checkThat(filtersDto.getCrsCode().size(), equalTo(DATABASE_CRS_CODE_SIZE));
    collector.checkThat(filtersDto.getSource().size(), equalTo(DATABASE_SOURCE_SIZE));
    collector.checkThat(filtersDto.getSdgCode().size(), equalTo(DATABASE_SDG_CODE_SIZE));
    collector.checkThat(filtersDto.getLevel().size(), equalTo(DATABASE_LEVEL_SIZE));
  }
}
