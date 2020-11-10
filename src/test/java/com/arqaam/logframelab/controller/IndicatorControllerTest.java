package com.arqaam.logframelab.controller;

import com.arqaam.logframelab.controller.dto.FiltersDto;
import com.arqaam.logframelab.exception.WrongFileExtensionException;
import com.arqaam.logframelab.model.Error;
import com.arqaam.logframelab.model.IndicatorResponse;
import com.arqaam.logframelab.model.MLScanIndicatorResponse;
import com.arqaam.logframelab.model.MLScanIndicatorResponse.MLScanIndicator;
import com.arqaam.logframelab.model.NumIndicatorsSectorLevel;
import com.arqaam.logframelab.model.persistence.*;
import com.arqaam.logframelab.model.projection.CounterSectorLevel;
import com.arqaam.logframelab.repository.IndicatorRepository;
import com.arqaam.logframelab.repository.LevelRepository;
import com.arqaam.logframelab.service.IndicatorService;
import com.arqaam.logframelab.service.MachineLearningService;

import com.arqaam.logframelab.service.StatisticService;
import com.arqaam.logframelab.util.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class IndicatorControllerTest extends BaseControllerTest {

  private static final Level[] mockLevels =
      new Level[]{
          new Level(1L, "OUTPUT", "OUTPUT", 3),
          new Level(2L, "OUTCOME", "OUTCOME", 2),
          new Level(3L, "OTHER_OUTCOMES", "OTHER OUTCOMES", 4),
          new Level(4L, "IMPACT", "IMPACT", 1)
      };

  private static final FiltersDto EMPTY_FILTER = new FiltersDto();

  private final static List<String> mockSectors = Arrays
      .asList("Digitalisation", "Education", "Poverty",
          "Nutrition", "Agriculture", "Health", "WASH", "Electricity", "Private Sector",
          "Infrastructure", "Migration", "Climate Change", "Environment", "Public Sector",
          "Human Rights", "Conflict", "Food Security", "Equality", "Water and Sanitation");
  private final static List<Source> mockSources = Arrays.asList(
          new Source(1L, "Capacity4Dev"),new Source(1L, "EU"),new Source(2L, "WFP"),new Source(3L, "ECHO"),
          new Source(4L,"FAO"), new Source(5L,"WHO"), new Source(6L,"FANTA"), new Source(7L,"IPA"), new Source(8L, "ACF"),
          new Source(9L,"Nutrition Cluster"), new Source(10L,"Freedom House"), new Source(11L,"CyberGreen"), new Source(12L,"ITU"),
          new Source(13L,"UN Sustainable Development Goals"), new Source(14L,"World Bank"), new Source(15L,"UNDP"), new Source(16L,"ILO"),
          new Source(7L,"IMF"));
  private final static List<SDGCode> mockSdgCodes =  Arrays.asList(
          new SDGCode(1L,"End poverty in all its forms everywhere"),
          new SDGCode(2L,"End hunger, achieve food security and improved nutrition and promote sustainable agriculture"),
          new SDGCode(3L,"Ensure healthy lives and promote well-being for all at all ages"),
          new SDGCode(4L,"Ensure inclusive and equitable quality education and promote lifelong learning opportunities for all"),
          new SDGCode(5L,"Achieve gender equality and empower all women and girls"));
  private final static List<CRSCode> mockCrsCodes = Arrays
        .asList(new CRSCode(998L,"Unallocated / Unspecified"),new CRSCode(151L, "Government & Civil Society-general"),
                new CRSCode(240L, "Banking & Financial Services"), new CRSCode(112L, "Basic Education"));
  private final static List<Long> mockLevelsId = Arrays.stream(mockLevels).map(Level::getId)
      .collect(Collectors.toList());
  private final static List<String> mockSourceVerification = Arrays
      .asList("World Bank Data", "EU", "SDG Country Data",
          "Project's M&E system", "UNDP Global Human Development Indicators");

  @MockBean
  private LevelRepository levelRepositoryMock;

  @MockBean
  private IndicatorRepository indicatorRepositoryMock;

  @MockBean
  private MachineLearningService machineLearningService;

  @MockBean
  private StatisticService statisticService;

  @Autowired
  private IndicatorService indicatorService;

  @BeforeEach
  void setup() {
    when(levelRepositoryMock.findAllByOrderByPriority())
        .thenReturn(Arrays.stream(mockLevels).sorted().collect(Collectors.toList()));
    when(indicatorRepositoryMock.findAll(any(Specification.class))).
        thenReturn(mockIndicatorList().stream()
            .filter(x -> mockSectors.contains(x.getSector()) && mockLevelsId
                .contains(x.getLevel().getId()) && mockSources.containsAll(x.getSource()) &&
                    mockSdgCodes.containsAll(x.getSdgCode()) && mockCrsCodes.containsAll(x.getCrsCode()))
            .collect(Collectors.toList()));

    when(indicatorRepositoryMock.findAll()).thenReturn(mockIndicatorList());

    // generateAuthToken();
  }

  @Test
  void handleFileUpload() {
    String indicatorName = mockIndicatorList().get(0).getName();
    String indicatorName2 = mockIndicatorList().get(1).getName();
    List<Indicator> indicators = new ArrayList<>();
    indicators.add(mockIndicatorList().get(0));
    indicators.add(mockIndicatorList().get(1));
    
    MLScanIndicator indicator1 = new MLScanIndicator(indicatorName, indicators.get(0).getId(), new MLScanIndicatorResponse.MLSearchResult(38.123456));
    MLScanIndicator indicator2 = new MLScanIndicator(indicatorName2, indicators.get(1).getId(), new MLScanIndicatorResponse.MLSearchResult(40.123456));
    
    List<MLScanIndicator> mlIndicators = new ArrayList<>();
    mlIndicators.add(indicator1);
    mlIndicators.add(indicator2);
    
    when(machineLearningService.scanForIndicators(any(), any())).thenReturn(mlIndicators);
    when(indicatorRepositoryMock.findAllByIdIn(any())).thenReturn(indicators);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);
    FiltersDto filters = getSampleFilter();

    MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
    body.add("file", new ClassPathResource("test_doc.docx"));
    body.add("filter", filters);

    List<IndicatorResponse> expected = Arrays.asList(
      indicatorService.convertIndicatorToIndicatorResponse(mockIndicatorList().get(1)),
      indicatorService.convertIndicatorToIndicatorResponse(mockIndicatorList().get(0))
    );
    expected.get(1).setScore(38);
    expected.get(0).setScore(40);
    
    ResponseEntity<List<IndicatorResponse>> response =
        testRestTemplate.exchange(
            "/indicator/upload",
            HttpMethod.POST,
            new HttpEntity<>(body, headers),
            new ParameterizedTypeReference<List<IndicatorResponse>>() {
            });

    assertEquals(HttpStatus.OK, response.getStatusCode());

    Objects.requireNonNull(response.getBody())
        .forEach(resp -> assertAll(
            () -> assertEqualsIndicator(expected, response.getBody()),
            () -> assertThat(filters.getSector(), hasItem(resp.getSector())),
            () -> assertThat(
                filters.getLevel().stream().map(Level::getName).collect(Collectors.toSet()),
                hasItem(resp.getLevel())),
            () -> assertTrue(filters.getCrsCode().containsAll(resp.getCrsCode())),
            () -> assertTrue(filters.getSource().containsAll(resp.getSource())),
            () -> assertTrue(filters.getSdgCode().containsAll(resp.getSdgCode()))));
  }

  /*@Test
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
  }*/

  @Test
  void handleFileUpload_wrongFileFormat() {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);
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
    verify(statisticService).addDownloadStatistic(Constants.WORD_FORMAT);
  }

  @Test
  void downloadIndicators_DFIDFormat() {
    List<IndicatorResponse> indicators = getExpectedResult();
    when(indicatorRepositoryMock.findAllById(any())).thenReturn(mockIndicatorList());
    ResponseEntity<Resource> response = testRestTemplate
            .exchange("/indicator/download?format=dfid", HttpMethod.POST,
                    new HttpEntity<>(indicators), Resource.class);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    verify(statisticService).addDownloadStatistic(Constants.DFID_FORMAT);

  }

  @Test
  void downloadIndicators_PRMFormat() {
    List<IndicatorResponse> indicators = getExpectedResult();
    when(indicatorRepositoryMock.findAllById(any())).thenReturn(mockIndicatorList());
    ResponseEntity<Resource> response = testRestTemplate
            .exchange("/indicator/download?format=prm", HttpMethod.POST,
                    new HttpEntity<>(indicators), Resource.class);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    verify(statisticService).addDownloadStatistic(Constants.PRM_FORMAT);
  }

  @Test
  void downloadIndicators_emptyIndicators() {
    List<IndicatorResponse> indicators = new ArrayList<>();
    ResponseEntity<Error> response =
            testRestTemplate.exchange(
                    "/indicator/download", HttpMethod.POST, new HttpEntity<>(indicators), Error.class);
    assertEqualsException(response, HttpStatus.CONFLICT, 6, IllegalArgumentException.class);
  }

  @Test
  void handleFileUpload_doc() {
    String indicatorName = mockIndicatorList().get(0).getName();
    String indicatorName2 = mockIndicatorList().get(1).getName();
    List<Indicator> indicators = new ArrayList<>();
    indicators.add(mockIndicatorList().get(0));
    indicators.add(mockIndicatorList().get(1));
    
    MLScanIndicator indicator1 = new MLScanIndicator(indicatorName, 1L, new MLScanIndicatorResponse.MLSearchResult(38.123456));
    MLScanIndicator indicator2 = new MLScanIndicator(indicatorName2, 2L, new MLScanIndicatorResponse.MLSearchResult(40.123456));
    
    List<MLScanIndicator> mlIndicators = new ArrayList<>();
    mlIndicators.add(indicator1);
    mlIndicators.add(indicator2);
    
    when(machineLearningService.scanForIndicators(any(), any())).thenReturn(mlIndicators);
    when(indicatorRepositoryMock.findAllByIdIn(any())).thenReturn(indicators);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);

    List<IndicatorResponse> expected = Arrays.asList(
      indicatorService.convertIndicatorToIndicatorResponse(mockIndicatorList().get(1)),
      indicatorService.convertIndicatorToIndicatorResponse(mockIndicatorList().get(0))
    );
    expected.get(1).setScore(38);
    expected.get(0).setScore(40);
    MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
    body.add("filter", getSampleFilter());
    body.add("file", new ClassPathResource("test doc.doc"));

    ResponseEntity<List<IndicatorResponse>> response = testRestTemplate.exchange("/indicator/upload", HttpMethod.POST,
        new HttpEntity<>(body, headers), new ParameterizedTypeReference<List<IndicatorResponse>>() {});

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEqualsIndicator(expected, response.getBody());
  }

  @Test
  void getIndicators() {
    List<IndicatorResponse> expectedResult = getExpectedResult();
    ResponseEntity<List<IndicatorResponse>> response = testRestTemplate
        .exchange("/indicator?sector=" + String.join(",", mockSectors) +
                        "&levels=" + mockLevelsId.stream().map(String::valueOf).collect(Collectors.joining(","))
                        + "&sources=" + mockSources.stream().map(x -> String.valueOf(x.getId())).collect(Collectors.joining(",")) +
                        "&sdgCodes=" + mockSdgCodes.stream().map(x -> String.valueOf(x.getId())).collect(Collectors.joining(",")) +
                        "&crsCodes=" + mockCrsCodes.stream().map(x -> String.valueOf(x.getId())).collect(Collectors.joining(",")), HttpMethod.GET,
                new HttpEntity<>(new HttpHeaders()), new ParameterizedTypeReference<List<IndicatorResponse>>() {
                });

    assertEquals(HttpStatus.OK, response.getStatusCode());
    verify(indicatorRepositoryMock).findAll(any(Specification.class));
    verify(indicatorRepositoryMock, times(0)).findAll();
    assertEqualsIndicator(expectedResult, response.getBody());
  }

  @Test
  void getIndicators_someFilters() {
    List<IndicatorResponse> expectedResult = getExpectedResult();
    ResponseEntity<List<IndicatorResponse>> response = testRestTemplate
        .exchange("/indicator?sector=" + String.join(",", mockSectors) +
                        "&levels=" + mockLevelsId.stream().map(String::valueOf).collect(Collectors.joining(","))
                        + "&sources=" + mockSources.stream().map(x -> String.valueOf(x.getId())).collect(Collectors.joining(",")),
                HttpMethod.GET, new HttpEntity<>(new HttpHeaders()),
                new ParameterizedTypeReference<List<IndicatorResponse>>() {
                });

    assertEquals(HttpStatus.OK, response.getStatusCode());
    verify(indicatorRepositoryMock).findAll(any(Specification.class));
    verify(indicatorRepositoryMock, times(0)).findAll();
    assertEqualsIndicator(expectedResult, response.getBody());
  }

  @Test
  void getIndicators_noFilters() {
    List<IndicatorResponse> expectedResult = mockIndicatorList().stream().sorted(Comparator.comparing(Indicator::getLevel))
        .map(indicatorService::convertIndicatorToIndicatorResponse).collect(Collectors.toList());
    ResponseEntity<List<IndicatorResponse>> response = testRestTemplate
        .exchange("/indicator", HttpMethod.GET,
                new HttpEntity<>(new HttpHeaders()), new ParameterizedTypeReference<List<IndicatorResponse>>() {
                });

    assertEquals(HttpStatus.OK, response.getStatusCode());
    verify(indicatorRepositoryMock, times(0)).findAll(any(Specification.class));
    verify(indicatorRepositoryMock).findAll();
    assertEqualsIndicator(expectedResult, response.getBody());
  }

  @Test
  void getTemplate() {
    List<IndicatorResponse> expectedResult = getExpectedResult();
    ResponseEntity<Resource> response = testRestTemplate
        .exchange("/indicator/template/dfid", HttpMethod.GET,new HttpEntity<>(new HttpHeaders()), Resource.class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
  }

  @Test
  void getTemplate_xlsxFormat() {
    List<IndicatorResponse> expectedResult = getExpectedResult();
    ResponseEntity<Resource> response = testRestTemplate
        .exchange("/indicator/template/xlsx", HttpMethod.GET,new HttpEntity<>(new HttpHeaders()), Resource.class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
  }

  @Test
  void getTotalNumIndicators() {
    Long expectedResult = 30L;
    when(indicatorService.getTotalNumIndicators()).thenReturn(expectedResult);
    ResponseEntity<Long> response = testRestTemplate
        .exchange("/indicator/total-number", HttpMethod.GET,new HttpEntity<>(new HttpHeaders()), Long.class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(expectedResult, response.getBody());
  }

  @Test
  void getIndicatorsByLevelAndSector() {
    List<CounterSectorLevel> counterSectorLevelList = new ArrayList<>();
    counterSectorLevelList.add(new CounterSectorLevel() {
      @Override
      public String getSector() { return "Sector"; }
      @Override
      public String getLevel() { return mockLevels[0].getName(); }
      @Override
      public Long getCount() { return 100L; }
    });

    counterSectorLevelList.add(new CounterSectorLevel() {
      @Override
      public String getSector() { return "Sector"; }
      @Override
      public String getLevel() { return mockLevels[1].getName(); }
      @Override
      public Long getCount() { return 4L; }
    });

    counterSectorLevelList.add(new CounterSectorLevel() {
      @Override
      public String getSector() { return "Sector 2"; }
      @Override
      public String getLevel() { return mockLevels[3].getName(); }
      @Override
      public Long getCount() { return 20L; }
    });
    List<NumIndicatorsSectorLevel.CountIndicatorsByLevel> counterLevel = new ArrayList<>();
    counterLevel.add(new NumIndicatorsSectorLevel.CountIndicatorsByLevel(mockLevels[0].getName(), 100L));
    counterLevel.add(new NumIndicatorsSectorLevel.CountIndicatorsByLevel(mockLevels[1].getName(), 4L));

    List<NumIndicatorsSectorLevel> expectedResult = new ArrayList<>();
    expectedResult.add(new NumIndicatorsSectorLevel("Sector", counterLevel));
    expectedResult.add(new NumIndicatorsSectorLevel("Sector 2", Collections.singletonList(new NumIndicatorsSectorLevel.CountIndicatorsByLevel(mockLevels[3].getName(), 20L))));
    
    when(indicatorRepositoryMock.countIndicatorsGroupedBySectorAndLevel()).thenReturn(counterSectorLevelList);
    ResponseEntity<List<NumIndicatorsSectorLevel>> response = testRestTemplate
        .exchange("/indicator/sector-level-count", HttpMethod.GET,
        new HttpEntity<>(new HttpHeaders()), new ParameterizedTypeReference<List<NumIndicatorsSectorLevel>>(){
        });

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(expectedResult, response.getBody());
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
        .source(Collections.singleton(mockSources.get(0))).sector(mockSectors.get(0)).sdgCode(Collections.singleton(mockSdgCodes.get(0)))
        .crsCode(Collections.singleton(mockCrsCodes.get(0))).build());
    list.add(Indicator.builder().id(73L).name(
        "Number of government policies developed or revised with civil society organisation participation through EU support")
        .description("Public Sector").level(mockLevels[1]).keywords("government policies, policy")
        .keywordsList(keywordsGovPolicyList)
        .source(Collections.singleton(mockSources.get(1))).sector(mockSectors.get(1)).sdgCode(Collections.singleton(mockSdgCodes.get(1)))
        .crsCode(Collections.singleton(mockCrsCodes.get(1))).build());
    list.add(Indicator.builder().id(5L).name("Revenue, excluding grants (% of GDP)")
        .description("Public Sector").level(mockLevels[3]).keywords("government")
        .keywordsList(keywordsGovList)
        .source(Collections.singleton(mockSources.get(2))).sector(mockSectors.get(2)).sdgCode(Collections.singleton(mockSdgCodes.get(2)))
        .crsCode(Collections.singleton(mockCrsCodes.get(2))).build());
    list.add(
        Indicator.builder().id(1L).name("Number of food insecure people receiving EU assistance")
            .description("Food & Agriculture").level(mockLevels[1]).keywords(keyword)
            .keywordsList(keywordsFoodList)
            .source(Collections.singleton(mockSources.get(3))).sector(mockSectors.get(3)).sdgCode(Collections.singleton(mockSdgCodes.get(3)))
            .crsCode(Collections.singleton(mockCrsCodes.get(3))).build());

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
        .getSector()
        .addAll(Arrays
            .asList("Digitalisation", "Education", "Poverty", "Nutrition", "Agriculture", "Health",
                "WASH", "Electricity", "Private Sector", "Infrastructure", "Migration",
                "Climate Change", "Environment", "Public Sector", "Human Rights", "Conflict",
                "Food Security", "Equality", "Water and Sanitation"));
    filters
        .getCrsCode()
        .addAll(mockCrsCodes);
    filters.getLevel().addAll(Arrays.asList(mockLevels));
    filters
        .getSource()
        .addAll(mockSources);
    filters.getSdgCode().addAll(mockSdgCodes);
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
