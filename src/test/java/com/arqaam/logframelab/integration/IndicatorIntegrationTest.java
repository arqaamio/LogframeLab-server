package com.arqaam.logframelab.integration;

import com.arqaam.logframelab.controller.dto.FiltersDto;
import com.arqaam.logframelab.model.IndicatorResponse;
import com.arqaam.logframelab.model.NumIndicatorsSectorLevel;
import com.arqaam.logframelab.model.persistence.CRSCode;
import com.arqaam.logframelab.model.persistence.Level;
import com.arqaam.logframelab.model.persistence.SDGCode;
import com.arqaam.logframelab.model.persistence.Source;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.*;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;

public class IndicatorIntegrationTest extends BaseIntegrationTest {

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
  void handleFileUpload() {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);

    MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
    body.add("file", new ClassPathResource("test_doc.docx"));
    body.add("filter", new FiltersDto());
    ResponseEntity<List<IndicatorResponse>> response = testRestTemplate
            .exchange("/indicator/upload", HttpMethod.POST,
                    new HttpEntity<>(body, headers), new ParameterizedTypeReference<List<IndicatorResponse>>(){});

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertFalse(response.getBody().isEmpty());
    for (IndicatorResponse indicator : response.getBody()) {
      assertNotNull(indicator);
      assertTrue(indicator.getScore()> -1 && indicator.getScore() <= 100);
    }
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
    ResponseEntity<List<IndicatorResponse>> response = testRestTemplate
            .exchange("/indicator?name=NUMBER&sectors=Poverty", HttpMethod.GET,
                    new HttpEntity<>(new HttpHeaders()), new ParameterizedTypeReference<List<IndicatorResponse>>() {});
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

  @Test
  void getTotalNumIndicators() {
    ResponseEntity<Long> response = testRestTemplate
            .exchange("/indicator/total-number", HttpMethod.GET,
                    new HttpEntity<>(new HttpHeaders()), Long.class);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertTrue(response.getBody() > 0);
  }

  @Test
  void getIndicatorsByLevelAndSector() {
    ResponseEntity<List<NumIndicatorsSectorLevel>> response = testRestTemplate
            .exchange("/indicator/sector-level-count", HttpMethod.GET,
                    new HttpEntity<>(new HttpHeaders()), new ParameterizedTypeReference<List<NumIndicatorsSectorLevel>>() {});

                    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertFalse(response.getBody().isEmpty());
    Map<String, Integer> sectorMap = new HashMap<>();
    for (NumIndicatorsSectorLevel element : response.getBody()) {
      if(sectorMap.containsKey(element.getSector())) {
        fail("Sector should not be repeated");
      }else {
        sectorMap.put(element.getSector(), 1);
      }
      assertFalse(element.getCounter().isEmpty());
      for (NumIndicatorsSectorLevel.CountIndicatorsByLevel count : element.getCounter()) {
        assertTrue(count.getCount() > 0);
        assertFalse(count.getLevel().isBlank());
      }
    }
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
