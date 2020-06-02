package com.arqaam.logframelab.controller;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;

import com.arqaam.logframelab.controller.dto.FiltersDto;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.arqaam.logframelab.model.IndicatorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class IndicatorControllerIntegrationTest extends BaseControllerTest {

  private static final int DATABASE_THEMES_SIZE = 42;
  private static final int DATABASE_CRS_CODE_SIZE = 76;
  private static final int DATABASE_SOURCE_SIZE = 26;
  private static final int DATABASE_SDG_CODE_SIZE = 168;
  private static final int DATABASE_LEVEL_SIZE = 3;

  @Test
  void downloadIndicators() {
    List<IndicatorResponse> indicators = sampleIndicatorResponse();
    ResponseEntity<Resource> response = testRestTemplate
            .exchange("/indicator/download", HttpMethod.POST,
                    new HttpEntity<>(indicators), Resource.class);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
  }

  @Test
  public void whenFiltersRequested_ThenFiltersReturned() {
    ResponseEntity<FiltersDto> filters =
        testRestTemplate.getForEntity("/indicator/filters", FiltersDto.class);
    FiltersDto filtersDto = Objects.requireNonNull(filters.getBody());

    assertAll(
        () -> assertThat(filters.getStatusCode(), is(HttpStatus.OK)),
        () -> assertThat(filtersDto.getThemes().size(), is(DATABASE_THEMES_SIZE)),
        () -> assertThat(filtersDto.getCrsCode().size(), is(DATABASE_CRS_CODE_SIZE)),
        () -> assertThat(filtersDto.getSource().size(), is(DATABASE_SOURCE_SIZE)),
        () -> assertThat(filtersDto.getSdgCode().size(), is(DATABASE_SDG_CODE_SIZE)),
        () -> assertThat(filtersDto.getLevel().size(), is(DATABASE_LEVEL_SIZE)));
  }

  List<IndicatorResponse> sampleIndicatorResponse() {
    List<IndicatorResponse> list = new ArrayList<>();
    list.add(IndicatorResponse.builder().id(1L).build());
    list.add(IndicatorResponse.builder().id(42L).date("1980").value("100").build());
    return list;
  }
}
