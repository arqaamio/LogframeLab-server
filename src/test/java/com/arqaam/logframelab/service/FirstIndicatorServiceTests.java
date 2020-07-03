package com.arqaam.logframelab.service;

import static java.util.Objects.isNull;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.arqaam.logframelab.model.IndicatorResponse;
import com.arqaam.logframelab.model.persistence.Indicator;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
public class FirstIndicatorServiceTests extends BaseIndicatorServiceTest {

  @Test
  void extractIndicatorsFromWordFile() throws IOException {
    when(indicatorRepository.findAll()).thenReturn(mockIndicatorList());
    List<IndicatorResponse> expectedResult = getExpectedResult(false);
    MultipartFile file = new MockMultipartFile("test_doc.docx", "test_doc.docx",
        MediaType.APPLICATION_OCTET_STREAM.toString(),
        new ClassPathResource("test_doc.docx").getInputStream());
    List<IndicatorResponse> result = indicatorService.extractIndicatorsFromWordFile(file, null);
    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(expectedResult, result);
  }

  @Test
  void extractIndicatorsFromWordFile_doc() throws IOException {
    when(indicatorRepository.findAll()).thenReturn(mockIndicatorList());
    List<IndicatorResponse> expectedResult = getExpectedResult(false);
    MultipartFile file = new MockMultipartFile("test doc.doc", "test doc.doc",
        MediaType.APPLICATION_OCTET_STREAM.toString(),
        new ClassPathResource("test doc.doc").getInputStream());
    List<IndicatorResponse> result = indicatorService.extractIndicatorsFromWordFile(file, null);
    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(expectedResult, result);
  }

  @Test
  void checkIndicators() {
    List<String> wordsToScan = Arrays.asList("food", "government", "policy", "retirement");
    List<Indicator> indicators = mockIndicatorList();
    // Test also indicators without keyword
    indicators.add(Indicator.builder().id(0L).name("Name").description("Description").build());
    Map<Long, Indicator> mapResult = new HashMap<>();
    indicatorService.checkIndicators(wordsToScan, indicators, mapResult);
    indicators = indicators.stream().sorted(Comparator.comparing(Indicator::getId))
        .collect(Collectors.toList());
    assertEquals(indicators.size() - 1, mapResult.size());
    for (int i = 0; i < mapResult.values().size(); i++) {
      assertEquals(indicators.get(i + 1), mapResult.values().toArray()[i]);
    }
  }

  @Test
  void checkIndicators_withIndicatorsWithSameId() {
    List<String> keywordsPolicyList = new ArrayList<>();
    keywordsPolicyList.add("policy");
    List<String> wordsToScan = Arrays.asList("food", "government", "policy", "retirement");
    List<Indicator> indicators = mockIndicatorList();
    indicators.add(Indicator.builder().id(73L).name(
        "Number of policies/strategies/laws/regulation developed/revised for digitalization with EU support")
        .description("Digitalisation").keywords("policy").keywordsList(keywordsPolicyList).build());
    Map<Long, Indicator> mapResult = new HashMap<>();
    indicatorService.checkIndicators(wordsToScan, indicators, mapResult);
    indicators = indicators.stream().sorted(Comparator.comparing(Indicator::getId))
        .collect(Collectors.toList());
    assertEquals(indicators.size() - 1, mapResult.size());
    for (int i = 0; i < indicators.size() - 1; i++) {
      assertEquals(indicators.get(i), mapResult.values().toArray()[i]);
    }
  }

  @Test
  void checkIndicators_withoutIndicators() {
    List<String> wordsToScan = Arrays.asList("food", "government", "policy", "retirement");
    List<Indicator> indicators = new ArrayList<>();
    Map<Long, Indicator> mapResult = new HashMap<>();
    indicatorService.checkIndicators(wordsToScan, indicators, mapResult);

    assertEquals(0, mapResult.size());
  }

  @Test
  void checkIndicators_withoutWordsToScan() {
    List<String> wordsToScan = new ArrayList<>();
    List<Indicator> indicators = new ArrayList<>();
    Map<Long, Indicator> mapResult = new HashMap<>();
    indicatorService.checkIndicators(wordsToScan, indicators, mapResult);

    assertTrue(mapResult.isEmpty());
  }

  @Test
  void exportIndicatorsInWordFile() throws IOException {
    List<Indicator> indicatorList = mockIndicatorList();
    List<Indicator> impactIndicators = indicatorList.stream()
            .filter(x -> x.getLevel().equals(mockLevels[3])).collect(Collectors.toList());
    List<Indicator> outcomeIndicators = indicatorList.stream()
            .filter(x -> x.getLevel().equals(mockLevels[1])).collect(Collectors.toList());
    List<Indicator> outputIndicators = indicatorList.stream()
            .filter(x -> x.getLevel().equals(mockLevels[0])).collect(Collectors.toList());

    List<IndicatorResponse> indicatorResponse = createListIndicatorResponse(indicatorList);
    ByteArrayOutputStream result = indicatorService.exportIndicatorsInWordFile(indicatorResponse);

    assertNotNull(result);
    XWPFDocument resultDoc = new XWPFDocument(new ByteArrayInputStream(result.toByteArray()));
    assertEquals(2, resultDoc.getTables().size());
    XWPFTable table = resultDoc.getTableArray(0);
    Integer rowIndex = 1;
    rowIndex = validateWordTemplateLevel(table, impactIndicators, indicatorResponse, rowIndex);
    rowIndex = validateWordTemplateLevel(table, outcomeIndicators, indicatorResponse, rowIndex);
    rowIndex = validateWordTemplateLevel(table, Collections.emptyList(), indicatorResponse, rowIndex);
    validateWordTemplateLevel(table, outputIndicators, indicatorResponse, rowIndex);
    resultDoc.close();
  }

  @Test
  void exportIndicatorsInWordFile_withoutValuesAndDate() throws IOException {
    List<Indicator> indicatorList = mockIndicatorList().stream().peek(x -> {x.setValue(null); x.setDate(null);}).collect(Collectors.toList());
    lenient().when(indicatorRepository.findAllById(any())).thenReturn(indicatorList);

    List<Indicator> impactIndicators = indicatorList.stream()
            .filter(x -> x.getLevel().equals(mockLevels[3])).collect(Collectors.toList());
    List<Indicator> outcomeIndicators = indicatorList.stream()
            .filter(x -> x.getLevel().equals(mockLevels[1])).collect(Collectors.toList());
    List<Indicator> outputIndicators = indicatorList.stream()
            .filter(x -> x.getLevel().equals(mockLevels[0])).collect(Collectors.toList());

    List<IndicatorResponse> indicatorResponse = createListIndicatorResponse(indicatorList);
    ByteArrayOutputStream result = indicatorService.exportIndicatorsInWordFile(indicatorResponse);

    assertNotNull(result);
    XWPFDocument resultDoc = new XWPFDocument(new ByteArrayInputStream(result.toByteArray()));
    assertEquals(2, resultDoc.getTables().size());
    XWPFTable table = resultDoc.getTableArray(0);
    Integer rowIndex = 1;
    rowIndex = validateWordTemplateLevel(table, impactIndicators, indicatorResponse, rowIndex);
    rowIndex = validateWordTemplateLevel(table, outcomeIndicators, indicatorResponse, rowIndex);
    rowIndex = validateWordTemplateLevel(table, Collections.emptyList(), indicatorResponse, rowIndex);
    validateWordTemplateLevel(table, outputIndicators, indicatorResponse, rowIndex);
    resultDoc.close();
  }

  @Test
  void importIndicators() {
    //TODO this test
//        indicatorService.importIndicators(new ClassPathResource("Indicator.xlsx").getPath());

//        indicatorService.importIndicators("/home/ari/Downloads/Indicator.xlsx");
//        indicatorService.importIndicators("/home/ari/Downloads/SDGs_changed.xlsx");

  }

  @Test
  void exportIndicatorsInWorksheet() {
    List<Indicator> expectedResult = mockIndicatorList();

    when(indicatorRepository.findAllById(any())).thenReturn(expectedResult);
    ByteArrayOutputStream outputStream = indicatorService
        .exportIndicatorsInWorksheet(createListIndicatorResponse(null));
//        MultipartFile multipartFile = new MockMultipartFile("indicators_export.xlsx", outputStream.toByteArray());
//        List<Indicator> result = indicatorService.importIndicators(multipartFile);
//
//        // because Id in the result is null, and in the expected result it isn't.
//        for (int i = 0; i < expectedResult.size(); i++) {
//            assertEquals(expectedResult.get(i).getLevel(), result.get(i).getLevel());
//            assertEquals(expectedResult.get(i).getKeywordsList(), result.get(i).getKeywordsList());
//            assertEquals(expectedResult.get(i).getDisaggregation(), result.get(i).getDisaggregation());
//            assertEquals(expectedResult.get(i).getCrsCode(), result.get(i).getCrsCode());
//            assertEquals(expectedResult.get(i).getDescription(), result.get(i).getDescription());
//            assertEquals(expectedResult.get(i).getName(), result.get(i).getName());
//            assertEquals(expectedResult.get(i).getSdgCode(), result.get(i).getSdgCode());
//            assertEquals(expectedResult.get(i).getSource(), result.get(i).getSource());
//            assertEquals(expectedResult.get(i).getThemes(), result.get(i).getThemes());
//            assertEquals(expectedResult.get(i).getDataSource(), result.get(i).getDataSource());
//            assertEquals(expectedResult.get(i).getSourceVerification(), result.get(i).getSourceVerification());
//        }

//        try(OutputStream fileOutputStream = new FileOutputStream("thefilename.xlsx")) {
//            outputStream.writeTo(fileOutputStream);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
  }


  List<Indicator> mockIndicatorList() {
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
        .dataSource("https://data.worldbank.org/indicator/NY.ADJ.DKAP.GN.ZS?view=chart")
        .date("2000")
        .value("100")
        .crsCode(mockCrsCodes.get(0)).build());
    list.add(Indicator.builder().id(73L).name(
        "Number of government policies developed or revised with civil society organisation participation through EU support")
        .description("Public Sector").level(mockLevels[1]).keywords("government policies, policy")
        .keywordsList(keywordsGovPolicyList)
        .source(mockSources.get(1)).themes(mockThemes.get(1)).sdgCode(mockSdgCodes.get(1))
        .dataSource("https://data.worldbank.org/indicator/SE.PRM.TENR.FE?view=chart")
        .date("2001")
        .value("100")
        .crsCode(mockCrsCodes.get(1)).build());
    list.add(Indicator.builder().id(5L).name("Revenue, excluding grants (% of GDP)")
        .description("Public Sector").level(mockLevels[3]).keywords("government")
        .keywordsList(keywordsGovList)
        .source(mockSources.get(2)).themes(mockThemes.get(2)).sdgCode(mockSdgCodes.get(2))
        .dataSource("https://data.worldbank.org/indicator/EG.ELC.ACCS.UR.ZS?view=chart")
        .date("1980")
        .value("50")
        .crsCode(mockCrsCodes.get(2)).build());
    list.add(
        Indicator.builder().id(1L).name("Number of food insecure people receiving EU assistance")
            .description("Food & Agriculture").level(mockLevels[1]).keywords(keyword)
            .keywordsList(keywordsFoodList)
            .source(mockSources.get(3)).themes(mockThemes.get(3)).sdgCode(mockSdgCodes.get(3))
            .dataSource("https://data.worldbank.org/indicator/EG.CFT.ACCS.ZS?view=chart")
            .date("2001")
            .value("100")
            .crsCode(mockCrsCodes.get(3)).build());

    return list;
  }


  private List<IndicatorResponse> createListIndicatorResponse(List<Indicator> indicators) {
    List<IndicatorResponse> list = new ArrayList<>();
    if(Optional.ofNullable(indicators).isPresent() && !indicators.isEmpty()) {
      for (Indicator indicator : indicators) {
        list.add(IndicatorResponse.builder()
                .name(indicator.getName())
                .id(indicator.getId())
                .color(indicator.getLevel().getColor())
                .description(indicator.getDescription())
                .level(indicator.getLevel().getName())
                .sdgCode(indicator.getSdgCode())
                .crsCode(indicator.getCrsCode())
                .source(indicator.getSource())
                .themes(indicator.getThemes())
                .disaggregation(indicator.getDisaggregation())
                .date(indicator.getDate())
                .value(indicator.getValue())
                .build());
      }
    } else {
      for (int i = 1; i < 6; i++) {
        list.add(IndicatorResponse.builder().id(i).level("IMPACT").color("color").name("Label " + i)
                .description("Description").build());
      }
    }
    return list;
  }

  @Test
  void getIndicators() {
    List<Indicator> expectedResult = mockIndicatorList().stream()
        .filter(
            x -> mockThemes.contains(x.getThemes()) && mockLevelsId.contains(x.getLevel().getId())
                && mockSources.contains(x.getSource())
                && mockSdgCodes.contains(x.getSdgCode()) && mockCrsCodes.contains(x.getCrsCode()))
        .collect(Collectors.toList());

    List<Indicator> result = indicatorService.getIndicators(Optional.of(mockThemes),
        Optional.of(mockSources), Optional.of(mockLevelsId), Optional.of(mockSdgCodes),
        Optional.of(mockCrsCodes));
    verify(indicatorRepository).findAll(any(Specification.class));
    verify(indicatorRepository, times(0)).findAll();
    assertEquals(expectedResult, result);
  }

  @Test
  void getIndicators_someFilters() {
    when(indicatorRepository.findAll(any(Specification.class))).
        thenReturn(mockIndicatorList().stream()
            .filter(x -> mockThemes.contains(x.getThemes()) && mockLevelsId
                .contains(x.getLevel().getId()) && mockSources.contains(x.getSource())
            ).collect(Collectors.toList()));

    List<Indicator> expectedResult = mockIndicatorList().stream()
        .filter(
            x -> mockThemes.contains(x.getThemes()) && mockLevelsId.contains(x.getLevel().getId())
                && mockSources.contains(x.getSource())
        ).collect(Collectors.toList());

    List<Indicator> result = indicatorService.getIndicators(Optional.of(mockThemes),
        Optional.of(mockSources), Optional.of(mockLevelsId), Optional.empty(), Optional.empty());
    verify(indicatorRepository).findAll(any(Specification.class));
    verify(indicatorRepository, times(0)).findAll();
    assertEquals(expectedResult, result);
  }

  @Test
  void getIndicators_noFilter() {
    List<Indicator> expectedResult = mockIndicatorList();
    List<Indicator> result = indicatorService
        .getIndicators(Optional.empty(), Optional.empty(), Optional.empty(),
            Optional.empty(), Optional.empty());
    verify(indicatorRepository, times(0)).findAll(any(Specification.class));
    verify(indicatorRepository).findAll();
    assertEquals(expectedResult, result);
  }

  Integer validateWordTemplateLevel(XWPFTable table, List<Indicator> indicators, List<IndicatorResponse> indicatorResponses, Integer rowIndex){
    if(indicators.size() > 0){
      // if its impact
      String assumptionsValue = indicators.get(0).getLevel().equals(mockLevels[3]) ? "\tNot applicable" : "";
      for (int i = 0; i < indicators.size(); i++) {
        XWPFTableRow row = table.getRow(i+rowIndex);
        assertEquals(8, row.getTableCells().size());
        assertEquals("", row.getCell(1).getTextRecursively());
        assertEquals(indicators.get(i).getName(), row.getCell(2).getTextRecursively());
        int finalI = i;
        indicatorResponses.stream().filter(x->x.getLevel().equals(mockLevels[3].getName()) && x.getId() == indicators.get(finalI).getId()).findFirst().ifPresent(response-> {
          String baselineValue = (isNull(response.getDate()) || isNull(response.getValue())) ? "" : response.getValue() + " (" +response.getDate() + ")";
          assertEquals(baselineValue, row.getCell(3).getTextRecursively());
        });
        assertEquals("", row.getCell(4).getTextRecursively());
        assertEquals("", row.getCell(5).getTextRecursively());
        assertEquals(Optional.ofNullable(indicators.get(i).getSourceVerification()).orElse(""), row.getCell(6).getTextRecursively());
        assertEquals(assumptionsValue,row.getCell(7).getTextRecursively());

        // validate merge cells
        assertTrue(row.getCell(0).getCTTc().getTcPr().isSetVMerge());
        assertTrue(row.getCell(1).getCTTc().getTcPr().isSetVMerge());
        assertTrue(row.getCell(7).getCTTc().getTcPr().isSetVMerge());
      }

      return rowIndex + indicators.size();
    }else {
      assertEquals(8, table.getRow(rowIndex).getTableCells().size());
      // First column has the level and last column is not empty for impact indicators
      for (int i = 1; i < table.getRow(rowIndex).getTableCells().size() - 1; i++) {
        assertEquals("", table.getRow(rowIndex).getCell(i).getTextRecursively());
      }
      return ++rowIndex;
    }
  }
}
