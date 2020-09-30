package com.arqaam.logframelab.controller;

import com.arqaam.logframelab.controller.dto.FiltersDto;
import com.arqaam.logframelab.model.IndicatorResponse;
import com.arqaam.logframelab.model.persistence.CRSCode;
import com.arqaam.logframelab.model.persistence.Level;
import com.arqaam.logframelab.model.persistence.SDGCode;
import com.arqaam.logframelab.model.persistence.Source;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles(profiles = "integration")
public class IndicatorControllerIntegrationTest extends BaseControllerTest {

  private static final int DATABASE_SECTOR_SIZE = 42;
  private static final int DATABASE_CRS_CODE_SIZE = 26;
  private static final int DATABASE_SOURCE_SIZE = 8;
  private static final int DATABASE_SDG_CODE_SIZE = 17;
  private static final int DATABASE_LEVEL_SIZE = 3;

  @BeforeEach
  void setup() {
    generateAuthToken();
  }

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
  void getIndicators() {
    UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl("/indicator")
        .queryParam("name", "NUMBER")
        .queryParam("sectors", "Poverty");
    ResponseEntity<List<IndicatorResponse>> response = testRestTemplate
            .exchange(builder.build().encode().toUri(), HttpMethod.GET,
                    new HttpEntity<>(new HttpHeaders()), new ParameterizedTypeReference<>() {});
   assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertTrue(response.getBody().stream().anyMatch(indicatorResponse -> indicatorResponse.getName().contains("Number")));
    assertTrue(response.getBody().stream().allMatch(indicatorResponse -> indicatorResponse.getName().toLowerCase().contains("number")));
    assertTrue(response.getBody().stream().allMatch(indicatorResponse -> indicatorResponse.getSector().toLowerCase().contains("poverty")));
  }

  @Test
  void whenFiltersRequested_ThenFiltersReturned() {
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(bearerToken);

    ResponseEntity<FiltersDto> filters =
        testRestTemplate.exchange(
            "/indicator/filters", HttpMethod.GET, new HttpEntity<>(headers), FiltersDto.class);
    FiltersDto filtersDto = Objects.requireNonNull(filters.getBody());

    assertAll(
        () -> assertThat(filters.getStatusCode(), is(HttpStatus.OK)),
        () -> assertThat(filtersDto.getSector().size(), is(DATABASE_SECTOR_SIZE)),
        () -> assertThat(filtersDto.getCrsCode().size(), is(DATABASE_CRS_CODE_SIZE)),
        () -> assertThat(filtersDto.getSource().size(), is(DATABASE_SOURCE_SIZE)),
        () -> assertThat(filtersDto.getSdgCode().size(), is(DATABASE_SDG_CODE_SIZE)),
        () -> assertThat(filtersDto.getLevel().size(), is(DATABASE_LEVEL_SIZE)),
        () -> assertTrue(testSortAscending(new ArrayList<>(filtersDto.getSector()))),
        () -> assertTrue(testSortAscending(filtersDto.getCrsCode().stream().map(CRSCode::getName).collect(Collectors.toList()))),
        () -> assertTrue(testSortAscending(filtersDto.getSource().stream().map(Source::getName).collect(Collectors.toList()))),
        () -> assertTrue(testSortAscending(filtersDto.getSdgCode().stream().map(SDGCode::getName).collect(Collectors.toList()))),
        () -> assertTrue(testSortAscending(filtersDto.getLevel().stream().map(Level::getName).collect(Collectors.toList())))
    );
  }

  List<IndicatorResponse> sampleIndicatorResponse() {
    List<IndicatorResponse> list = new ArrayList<>();
    list.add(IndicatorResponse.builder().id(1L).build());
    list.add(IndicatorResponse.builder().id(42L).date("1980").value("100").build());
    return list;
  }
  private boolean testSortAscending(List<String> list) {
    for (int i = 0; i < list.size() - 1; i++) {
      if(list.get(i).compareTo(list.get(i+1))>0) return false;
    }
    return true;
  }
}
