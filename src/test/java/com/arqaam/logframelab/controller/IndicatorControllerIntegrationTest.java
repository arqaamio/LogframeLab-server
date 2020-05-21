package com.arqaam.logframelab.controller;

import com.arqaam.logframelab.controller.dto.FiltersDto;
import com.arqaam.logframelab.repository.initializer.BaseDatabaseTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.migrationsupport.rules.EnableRuleMigrationSupport;
import org.springframework.http.*;

import java.util.Objects;

import static org.hamcrest.CoreMatchers.equalTo;

@EnableRuleMigrationSupport
public class IndicatorControllerIntegrationTest extends BaseControllerTest
    implements BaseDatabaseTest {

  private static final int DATABASE_THEMES_SIZE = 42;
  private static final int DATABASE_CRS_CODE_SIZE = 76;
  private static final int DATABASE_SOURCE_SIZE = 26;
  private static final int DATABASE_SDG_CODE_SIZE = 168;
  private static final int DATABASE_LEVEL_SIZE = 3;

  @BeforeEach
  void setup() {
    if (bearerToken == null) {
      bearerToken = getAuthToken();
    }
  }

  @Test
  public void whenFiltersRequested_ThenFiltersReturned() {
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(bearerToken);

    ResponseEntity<FiltersDto> filters =
        testRestTemplate.exchange(
            "/indicator/filters", HttpMethod.GET, new HttpEntity<>(headers), FiltersDto.class);
    FiltersDto filtersDto = Objects.requireNonNull(filters.getBody());

    collector.checkThat(filters.getStatusCode(), equalTo(HttpStatus.OK));
    collector.checkThat(filtersDto.getThemes().size(), equalTo(DATABASE_THEMES_SIZE));
    collector.checkThat(filtersDto.getCrsCode().size(), equalTo(DATABASE_CRS_CODE_SIZE));
    collector.checkThat(filtersDto.getSource().size(), equalTo(DATABASE_SOURCE_SIZE));
    collector.checkThat(filtersDto.getSdgCode().size(), equalTo(DATABASE_SDG_CODE_SIZE));
    collector.checkThat(filtersDto.getLevel().size(), equalTo(DATABASE_LEVEL_SIZE));
  }
}
