package com.arqaam.logframelab.controller;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.arqaam.logframelab.controller.dto.FiltersDto;
import com.arqaam.logframelab.exception.WrongFileExtensionException;
import com.arqaam.logframelab.model.Error;
import com.arqaam.logframelab.model.IndicatorResponse;
import com.arqaam.logframelab.model.persistence.Indicator;
import com.arqaam.logframelab.model.persistence.Level;
import com.arqaam.logframelab.repository.IndicatorRepository;
import com.arqaam.logframelab.repository.LevelRepository;
import com.arqaam.logframelab.repository.initializer.BaseDatabaseTest;
import com.arqaam.logframelab.service.IndicatorService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

class IndicatorControllerTest extends BaseControllerTest implements BaseDatabaseTest {

  private static final Level[] mockLevels =
      new Level[]{
          new Level(1L, "OUTPUT", "OUTPUT", "{output}", "green", 3),
          new Level(2L, "OUTCOME", "OUTCOME", "{outcomes}", "red", 2),
          new Level(3L, "OTHER_OUTCOMES", "OTHER OUTCOMES", "{otheroutcomes}", "orange", 4),
          new Level(4L, "IMPACT", "IMPACT", "{impact}", "purple", 1)
      };

  private static final FiltersDto EMPTY_FILTER = new FiltersDto();

  private final static List<String> mockThemes = Arrays
      .asList("Digitalisation", "Education", "Poverty",
          "Nutrition", "Agriculture", "Health", "WASH", "Electricity", "Private Sector",
          "Infrastructure", "Migration", "Climate Change", "Environment", "Public Sector",
          "Human Rights", "Conflict", "Food Security", "Equality", "Water and Sanitation");
  private final static List<String> mockSources = Arrays
      .asList("Capacity4Dev", "EU", "WFP", "ECHO", "ECHO,WFP",
          "ECHO,WHO", "FAO", "FAO,WHO", "WHO", "FANTA", "IPA", "WHO,FAO", "ACF",
          "Nutrition Cluster", "Freendom House", "CyberGreen", "ITU",
          "UN Sustainable Development Goals", "World Bank", "UNDP", "ILO", "IMF");
  private final static List<String> mockSdgCodes = Arrays.asList("8.2", "7.1", "4.1", "1.a", "1.b");
  private final static List<String> mockCrsCodes = Arrays
      .asList("99810.0", "15160.0", "24010.0", "15190.0", "43010.0", "24050.0", "43030.0");
  private final static List<Long> mockLevelsId = Arrays.stream(mockLevels).map(Level::getId)
      .collect(Collectors.toList());
  private final static List<String> mockSourceVerification = Arrays
      .asList("World Bank Data", "EU", "SDG Country Data",
          "Project's M&E system", "UNDP Global Human Development Indicators");

  @MockBean
  private LevelRepository levelRepositoryMock;

  @MockBean
  private IndicatorRepository indicatorRepositoryMock;

  @Autowired
  private IndicatorService indicatorService;

  @BeforeEach
  void setup() {
    when(levelRepositoryMock.findAllByOrderByPriority())
        .thenReturn(Arrays.stream(mockLevels).sorted().collect(Collectors.toList()));
    when(indicatorRepositoryMock.findAll(any(Specification.class))).
        thenReturn(mockIndicatorList().stream()
            .filter(x -> mockThemes.contains(x.getThemes()) && mockLevelsId
                .contains(x.getLevel().getId()) && mockSources.contains(x.getSource())
                && mockSdgCodes.contains(x.getSdgCode()) && mockCrsCodes.contains(x.getCrsCode()))
            .collect(Collectors.toList()));

    when(indicatorRepositoryMock.findAll()).thenReturn(mockIndicatorList());

    generateAuthToken();
  }

  @Test
  void handleFileUpload() {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);
    headers.setBearerAuth(bearerToken);
    FiltersDto filters = getSampleFilter();

    MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
    body.add("file", new ClassPathResource("test_doc.docx"));
    body.add("filter", filters);

    ResponseEntity<List<IndicatorResponse>> response =
        testRestTemplate.exchange(
            "/indicator/upload",
            HttpMethod.POST,
            new HttpEntity<>(body, headers),
            new ParameterizedTypeReference<List<IndicatorResponse>>() {
            });

    assertThat(response.getStatusCode(), is(HttpStatus.OK));

    Objects.requireNonNull(response.getBody())
        .forEach(resp -> assertAll(
            () -> assertThat(filters.getThemes(), hasItem(resp.getThemes())),
            () -> assertThat(
                filters.getLevel().stream().map(Level::getName).collect(Collectors.toSet()),
                hasItem(resp.getLevel())),
            () -> assertThat(filters.getSource(), hasItem(resp.getSource())),
            () -> assertThat(filters.getCrsCode(), hasItem(resp.getCrsCode())),
            () -> assertThat(filters.getSdgCode(), hasItem(resp.getSdgCode()))));
  }

  @Test
  void handleFileUpload_indicatorsWithSameId() {
    List<String> keywordsFoodList = new ArrayList<>();
    keywordsFoodList.add("agriculture");
    keywordsFoodList.add("food");
    List<IndicatorResponse> expectedResult = getExpectedResult();
    List<Indicator> indicators = mockIndicatorList();
    indicators.add(
        Indicator.builder().id(1L).name("Name 1").description("Description").level(mockLevels[1])
            .keywords("agriculture").keywordsList(keywordsFoodList).build());
    indicators.add(
        Indicator.builder().id(4L).name("Name 4").description("Description").level(mockLevels[0])
            .build());

    when(indicatorRepositoryMock.findAll()).thenReturn(indicators);
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);
    headers.setBearerAuth(bearerToken);
    MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
    body.add("file", new ClassPathResource("test_doc.docx"));
    body.add("filter", EMPTY_FILTER);
    ResponseEntity<List<IndicatorResponse>> response = testRestTemplate
        .exchange("/indicator/upload", HttpMethod.POST,
            new HttpEntity<>(body, headers),
            new ParameterizedTypeReference<List<IndicatorResponse>>() {
            });

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEqualsIndicator(expectedResult, response.getBody());
  }

  @Test
  void handleFileUpload_withIndicatorsWithoutKeywords() {
    List<String> keywordsFoodList = new ArrayList<>();
    keywordsFoodList.add("food");
    List<String> keywordsList = new ArrayList<>();
    keywordsList.add(keywordsFoodList.get(0));
    List<IndicatorResponse> expectedResult = getExpectedResult();
    expectedResult.add(IndicatorResponse.builder().id(6L).level(mockLevels[2].getName())
        .color(mockLevels[2].getColor())
        .name("Name 6").description("Description").build());
    expectedResult.add(IndicatorResponse.builder().id(5L).level(mockLevels[2].getName())
        .color(mockLevels[2].getColor())
        .name("Name 3").description("Description").build());
    List<Indicator> indicators = mockIndicatorList();
    // This showcases how keywords property is irrelevant, only keywordList is taken into consideration
    indicators.add(
        Indicator.builder().id(6L).name("Name 6").description("Description").level(mockLevels[2])
            .keywordsList(keywordsList).build());
    indicators.add(
        Indicator.builder().id(2L).name("Name 2").description("Description").level(mockLevels[1])
            .build());
    indicators.add(
        Indicator.builder().id(3L).name("Name 3").description("Description").level(mockLevels[2])
            .keywords("agriculture").build());

    when(indicatorRepositoryMock.findAll()).thenReturn(indicators);
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);
    headers.setBearerAuth(bearerToken);
    MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
    body.add("file", new ClassPathResource("test_doc.docx"));
    body.add("filter", EMPTY_FILTER);
    ResponseEntity<List<IndicatorResponse>> response = testRestTemplate
        .exchange("/indicator/upload", HttpMethod.POST,
            new HttpEntity<>(body, headers),
            new ParameterizedTypeReference<List<IndicatorResponse>>() {
            });

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEqualsIndicator(expectedResult, response.getBody());
  }

  @Test
  void handleFileUpload_wrongFileFormat() {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);
    headers.setBearerAuth(bearerToken);
    MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
    body.add("file", new ClassPathResource("application.properties"));
    body.add("filter", getSampleFilter());
    ResponseEntity<Error> response = testRestTemplate.exchange("/indicator/upload", HttpMethod.POST,
        new HttpEntity<>(body, headers), Error.class);
    assertEqualsException(response, HttpStatus.CONFLICT, 1, WrongFileExtensionException.class);
  }

  @Test
  void downloadIndicators_wordFormat() {
    List<IndicatorResponse> indicators = getExpectedResult();
    ResponseEntity<Resource> response = testRestTemplate
        .exchange("/indicator/download", HttpMethod.POST,
            new HttpEntity<>(indicators), Resource.class);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
  }

  @Test
  void downloadIndicators_DFIDFormat() {
    List<IndicatorResponse> indicators = getExpectedResult();
    when(indicatorRepositoryMock.findAllById(any())).thenReturn(mockIndicatorList());
    ResponseEntity<Resource> response = testRestTemplate
        .exchange("/indicator/download?format=dfid", HttpMethod.POST,
            new HttpEntity<>(indicators, headersWithAuth()), Resource.class);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
  }

  @Test
  void downloadIndicators_emptyIndicators() {
    List<IndicatorResponse> indicators = new ArrayList<>();
    ResponseEntity<Error> response =
        testRestTemplate.exchange(
            "/indicator/download", HttpMethod.POST, new HttpEntity<>(indicators, headersWithAuth()), Error.class);
    assertEqualsException(response, HttpStatus.CONFLICT, 6, IllegalArgumentException.class);
  }

  @Test
  void handleFileUpload_doc() {
    List<IndicatorResponse> expectedResult = getExpectedResult();
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);
    headers.setBearerAuth(bearerToken);
    MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
    body.add("filter", getSampleFilter());

    body.add("file", new ClassPathResource("test doc.doc"));
    ResponseEntity<List<IndicatorResponse>> response = testRestTemplate.exchange("/indicator/upload", HttpMethod.POST,
        new HttpEntity<>(body, headers), new ParameterizedTypeReference<List<IndicatorResponse>>() {});

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEqualsIndicator(expectedResult, response.getBody());
  }

  @Test
  void getIndicators() {
    List<IndicatorResponse> expectedResult = getExpectedResult();
    ResponseEntity<List<IndicatorResponse>> response = testRestTemplate
        .exchange("/indicator?themes=" + String.join(",", mockThemes) +
                "&levels=" + mockLevelsId.stream().map(String::valueOf).collect(Collectors.joining(","))
                + "&sources=" + String.join(",", mockSources) +
                "&sdgCodes=" + String.join(",", mockSdgCodes) + "&crsCodes=" + String
                .join(",", mockCrsCodes), HttpMethod.GET,
            new HttpEntity<>(headersWithAuth()), new ParameterizedTypeReference<List<IndicatorResponse>>() {
            });

    assertEquals(HttpStatus.OK, response.getStatusCode());
    verify(indicatorRepositoryMock).findAll(any(Specification.class));
    verify(indicatorRepositoryMock, times(0)).findAll();
    assertEqualsIndicator(Arrays
        .asList(expectedResult.get(3), expectedResult.get(1), expectedResult.get(0),
            expectedResult.get(2)), response.getBody());
  }

  @Test
  void getIndicators_someFilters() {
    List<IndicatorResponse> expectedResult = getExpectedResult();
    ResponseEntity<List<IndicatorResponse>> response = testRestTemplate
        .exchange("/indicator?themes=" + String.join(",", mockThemes) +
                "&levels=" + mockLevelsId.stream().map(String::valueOf).collect(Collectors.joining(","))
                + "&sources=" + String.join(",", mockSources),
            HttpMethod.GET, new HttpEntity<>(headersWithAuth()),
            new ParameterizedTypeReference<List<IndicatorResponse>>() {
            });

    assertEquals(HttpStatus.OK, response.getStatusCode());
    verify(indicatorRepositoryMock).findAll(any(Specification.class));
    verify(indicatorRepositoryMock, times(0)).findAll();
    assertEqualsIndicator(Arrays
        .asList(expectedResult.get(3), expectedResult.get(1), expectedResult.get(0),
            expectedResult.get(2)), response.getBody());
  }

  @Test
  void getIndicators_noFilters() {
    List<IndicatorResponse> expectedResult = mockIndicatorList().stream()
        .map(indicatorService::convertIndicatorToIndicatorResponse).collect(Collectors.toList());
    ResponseEntity<List<IndicatorResponse>> response = testRestTemplate
        .exchange("/indicator", HttpMethod.GET,
            new HttpEntity<>(headersWithAuth()), new ParameterizedTypeReference<List<IndicatorResponse>>() {
            });

    assertEquals(HttpStatus.OK, response.getStatusCode());
    verify(indicatorRepositoryMock, times(0)).findAll(any(Specification.class));
    verify(indicatorRepositoryMock).findAll();
    assertEqualsIndicator(expectedResult, response.getBody());
  }

  private List<Indicator> mockIndicatorList() {
    String keyword = "food insecurity,agriculture";
    List<Indicator> list = new ArrayList<>();

    List<String> keywordsFoodList = new ArrayList<>();
    keywordsFoodList.add("agriculture");
    keywordsFoodList.add("food");

    List<String> keywordsPolicyList = new ArrayList<>();
    keywordsPolicyList.add("policy");

    List<String> keywordsGovList = new ArrayList<>();
    keywordsGovList.add("government");

    List<String> keywordsGovPolicyList = new ArrayList<>();
    keywordsGovPolicyList.add("government policies");
    keywordsGovPolicyList.add("policy");

    list.add(Indicator.builder().id(4L).name(
        "Number of policies/strategies/laws/regulation developed/revised for digitalisation with EU support")
        .description("Digitalisation").level(mockLevels[0]).keywords("policy")
        .keywordsList(keywordsPolicyList)
        .source(mockSources.get(0)).themes(mockThemes.get(0)).sdgCode(mockSdgCodes.get(0))
        .crsCode(mockCrsCodes.get(0)).build());
    list.add(Indicator.builder().id(73L).name(
        "Number of government policies developed or revised with civil society organisation participation through EU support")
        .description("Public Sector").level(mockLevels[1]).keywords("government policies, policy")
        .keywordsList(keywordsGovPolicyList)
        .source(mockSources.get(1)).themes(mockThemes.get(1)).sdgCode(mockSdgCodes.get(1))
        .crsCode(mockCrsCodes.get(1)).build());
    list.add(Indicator.builder().id(5L).name("Revenue, excluding grants (% of GDP)")
        .description("Public Sector").level(mockLevels[3]).keywords("government")
        .keywordsList(keywordsGovList)
        .source(mockSources.get(2)).themes(mockThemes.get(2)).sdgCode(mockSdgCodes.get(2))
        .crsCode(mockCrsCodes.get(2)).build());
    list.add(
        Indicator.builder().id(1L).name("Number of food insecure people receiving EU assistance")
            .description("Food & Agriculture").level(mockLevels[1]).keywords(keyword)
            .keywordsList(keywordsFoodList)
            .source(mockSources.get(3)).themes(mockThemes.get(3)).sdgCode(mockSdgCodes.get(3))
            .crsCode(mockCrsCodes.get(3)).build());

    return list;
  }

  private List<IndicatorResponse> getExpectedResult() {
    List<Indicator> indicators = mockIndicatorList();
    List<IndicatorResponse> indicatorResponses = new ArrayList<>();
    indicatorResponses.add(indicatorService.convertIndicatorToIndicatorResponse(indicators.get(2)));
    indicatorResponses.add(indicatorService.convertIndicatorToIndicatorResponse(indicators.get(1)));
    indicatorResponses.add(indicatorService.convertIndicatorToIndicatorResponse(indicators.get(3)));
    indicatorResponses.add(indicatorService.convertIndicatorToIndicatorResponse(indicators.get(0)));
    return indicatorResponses;
  }

  private void assertEqualsIndicator(List<IndicatorResponse> expectedResult,
      List<IndicatorResponse> result) {
    assertNotNull(result);
    assertEquals(expectedResult.size(), result.size());

    for (int i = 0; i < expectedResult.size(); i++) {
      assertEquals(expectedResult.get(i), result.get(i));
    }
  }

  private FiltersDto getSampleFilter() {
    FiltersDto filters = new FiltersDto();
    filters
        .getThemes()
        .addAll(Arrays
            .asList("Digitalisation", "Education", "Poverty", "Nutrition", "Agriculture", "Health",
                "WASH", "Electricity", "Private Sector", "Infrastructure", "Migration",
                "Climate Change", "Environment", "Public Sector", "Human Rights", "Conflict",
                "Food Security", "Equality", "Water and Sanitation"));
    filters
        .getCrsCode()
        .addAll(
            Arrays.asList("0.0", "16010.0", "24010.0", "15190.0", "99810.0", "15160.0", "15160.0"));
    filters.getLevel().addAll(Arrays.asList(mockLevels));
    filters
        .getSource()
        .addAll(Arrays
            .asList("Capacity4Dev", "EU", "WFP", "ECHO", "ECHO,WFP", "ECHO,WHO", "FAO", "FAO,WHO",
                "WHO", "FANTA", "IPA", "WHO,FAO", "ACF", "Nutrition Cluster", "Freendom House",
                "CyberGreen", "ITU", "UN Sustainable Development Goals", "World Bank", "UNDP",
                "ILO", "IMF"));
    filters.getSdgCode().addAll(Arrays.asList("4.1", "7.1", "1.a", "8.2"));
    return filters;
  }


}
//    private Integer validateTemplateLevel(XSSFSheet sheet, List<Indicator> indicators, Integer rowIndex, Integer numberTemplateIndicators){
//        List<CellRangeAddress> mergedRegions = sheet.getMergedRegions();
//        Integer initialRow = rowIndex;
//
//        for (Indicator indicator : indicators) {
//            assertEquals("", sheet.getRow(rowIndex + 1).getCell(3).getStringCellValue());
//            assertEquals(indicator.getName(), sheet.getRow(rowIndex + 1).getCell(2).getStringCellValue());
//            assertEquals(indicator.getSourceVerification(), sheet.getRow(rowIndex + 3).getCell(3).getStringCellValue());
//            rowIndex += 4;
//        }
//
//        int count = indicators.size();
//        while(count<numberTemplateIndicators){
//            assertEquals("", sheet.getRow(rowIndex+1).getCell(2).getStringCellValue());
//            assertEquals("", sheet.getRow(rowIndex+3).getCell(3).getStringCellValue());
//            rowIndex+=4;
//            count++;
//        }
//
//        // check merged cells in first column
//        int finalRowIndex = rowIndex;
//        if(indicators.size()>numberTemplateIndicators){
//            if(numberTemplateIndicators.equals(IndicatorService.OUTPUT_NUM_TEMP_INDIC)){
//                assertTrue(mergedRegions.stream().anyMatch(x -> x.getLastColumn() == 0 && x.getFirstRow() == initialRow + numberTemplateIndicators * 3 - 1
//                        && x.getLastRow() == finalRowIndex - 1));
//            }else {
//                assertTrue(mergedRegions.stream().anyMatch(x -> x.getLastColumn() == 0 && x.getFirstRow() == initialRow + numberTemplateIndicators * 3
//                        && x.getLastRow() == finalRowIndex - 1));
//            }
//        }
//
//        return rowIndex;
//    }
