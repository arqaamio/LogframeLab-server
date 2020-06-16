package com.arqaam.logframelab.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.arqaam.logframelab.model.persistence.Indicator;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SecondIndicatorServiceTests extends BaseIndicatorServiceTest {

  @Test
  void exportIndicatorsDFIDFormat() throws IOException {
    when(indicatorRepository.findAllById(any())).thenReturn(mockIndicatorList());
    List<Indicator> impactIndicators = mockIndicatorList().stream()
        .filter(x -> x.getLevel().equals(mockLevels[3])).collect(Collectors.toList());
    List<Indicator> outcomeIndicators = mockIndicatorList().stream()
        .filter(x -> x.getLevel().equals(mockLevels[1])).collect(Collectors.toList());
    List<Indicator> outputIndicators = mockIndicatorList().stream()
        .filter(x -> x.getLevel().equals(mockLevels[0])).collect(Collectors.toList());

    ByteArrayOutputStream outputStream = indicatorService
        .exportIndicatorsDFIDFormat(getExpectedResult());

    assertNotNull(outputStream);
    XSSFSheet sheet = new XSSFWorkbook(new ByteArrayInputStream(outputStream.toByteArray()))
        .getSheetAt(0);

    int rowIndex = 1;
    rowIndex = validateTemplateLevel(sheet, impactIndicators, rowIndex,
        IndicatorService.IMPACT_NUM_TEMP_INDIC);
    rowIndex = validateTemplateLevel(sheet, outcomeIndicators, rowIndex,
        IndicatorService.OUTCOME_NUM_TEMP_INDIC);
    validateTemplateLevel(sheet, outputIndicators, rowIndex,
        IndicatorService.OUTPUT_NUM_TEMP_INDIC);

    sheet.getWorkbook().close();
  }

  @Test
  void exportIndicatorsDFIDFormat_noImpactIndicators_newRowsOutcome() throws IOException {
    List<Indicator> mockIndicators = mockIndicatorList().stream()
        .filter(x -> !x.getLevel().equals(mockLevels[3])).collect(Collectors.toList());
    mockIndicators.add(new Indicator(100L, "Extra indicator 1", "", "", mockLevels[1], "",
        "", false, "", "", mockSourceVerification.get(0), "", null, 0));
    mockIndicators.add(new Indicator(100L, "Extra indicator 2", "", "", mockLevels[1], "",
        "", false, "", "", mockSourceVerification.get(1), "", null, 0));
    mockIndicators.add(new Indicator(100L, "Extra indicator 3", "", "", mockLevels[1], "",
        "", false, "", "", mockSourceVerification.get(2), "", null, 0));
    when(indicatorRepository.findAllById(any())).thenReturn(mockIndicators);

    ByteArrayOutputStream outputStream = indicatorService
        .exportIndicatorsDFIDFormat(mockIndicators.stream()
            .map(indicatorService::convertIndicatorToIndicatorResponse)
            .collect(Collectors.toList()));

    assertNotNull(outputStream);
    XSSFSheet sheet = new XSSFWorkbook(new ByteArrayInputStream(outputStream.toByteArray()))
        .getSheetAt(0);

    int rowIndex = 1;
    rowIndex = validateTemplateLevel(sheet, Collections.emptyList(), rowIndex,
        IndicatorService.IMPACT_NUM_TEMP_INDIC);
    rowIndex = validateTemplateLevel(sheet,
        mockIndicators.stream().filter(x -> x.getLevel().equals(mockLevels[1]))
            .collect(Collectors.toList()),
        rowIndex, IndicatorService.OUTCOME_NUM_TEMP_INDIC);
    validateTemplateLevel(sheet,
        mockIndicators.stream().filter(x -> x.getLevel().equals(mockLevels[0]))
            .collect(Collectors.toList()),
        rowIndex, IndicatorService.OUTPUT_NUM_TEMP_INDIC);
    sheet.getWorkbook().close();
  }

  @Test
  void exportIndicatorsDFIDFormat_noOutcomeIndicators_newRowsOutput() throws IOException {
    List<Indicator> mockIndicators = mockIndicatorList().stream()
        .filter(x -> !x.getLevel().equals(mockLevels[1])).collect(Collectors.toList());
    mockIndicators.add(new Indicator(100L, "Extra indicator 1", "", "", mockLevels[0], "",
        "", false, "", "", mockSourceVerification.get(0), "", null, 0));
    mockIndicators.add(new Indicator(100L, "Extra indicator 2", "", "", mockLevels[0], "",
        "", false, "", "", mockSourceVerification.get(1), "", null, 0));
    mockIndicators.add(new Indicator(100L, "Extra indicator 3", "", "", mockLevels[0], "",
        "", false, "", "", mockSourceVerification.get(2), "", null, 0));
    when(indicatorRepository.findAllById(any())).thenReturn(mockIndicators);

    ByteArrayOutputStream outputStream = indicatorService
        .exportIndicatorsDFIDFormat(mockIndicators.stream()
            .map(indicatorService::convertIndicatorToIndicatorResponse)
            .collect(Collectors.toList()));

    assertNotNull(outputStream);
    XSSFSheet sheet = new XSSFWorkbook(new ByteArrayInputStream(outputStream.toByteArray()))
        .getSheetAt(0);

    int rowIndex = 1;
    rowIndex = validateTemplateLevel(sheet,
        mockIndicators.stream().filter(x -> x.getLevel().equals(mockLevels[3]))
            .collect(Collectors.toList()),
        rowIndex, IndicatorService.IMPACT_NUM_TEMP_INDIC);
    rowIndex = validateTemplateLevel(sheet, Collections.emptyList(), rowIndex,
        IndicatorService.OUTCOME_NUM_TEMP_INDIC);
    validateTemplateLevel(sheet,
        mockIndicators.stream().filter(x -> x.getLevel().equals(mockLevels[0]))
            .collect(Collectors.toList()),
        rowIndex, IndicatorService.OUTPUT_NUM_TEMP_INDIC);
    sheet.getWorkbook().close();
  }

  @Test
  void exportIndicatorsDFIDFormat_noOutputIndicators_newRowsImpact() throws IOException {
    List<Indicator> mockIndicators = mockIndicatorList().stream()
        .filter(x -> !x.getLevel().equals(mockLevels[0])).collect(Collectors.toList());
    mockIndicators.add(new Indicator(100L, "Extra indicator 1", "", "", mockLevels[3], "",
        "", false, "", "", mockSourceVerification.get(0), "", null, 0));
    mockIndicators.add(new Indicator(100L, "Extra indicator 2", "", "", mockLevels[3], "",
        "", false, "", "", mockSourceVerification.get(1), "", null, 0));
    mockIndicators.add(new Indicator(100L, "Extra indicator 3", "", "", mockLevels[3], "",
        "", false, "", "", mockSourceVerification.get(2), "", null, 0));
    when(indicatorRepository.findAllById(any())).thenReturn(mockIndicators);

    ByteArrayOutputStream outputStream = indicatorService
        .exportIndicatorsDFIDFormat(mockIndicators.stream()
            .map(indicatorService::convertIndicatorToIndicatorResponse)
            .collect(Collectors.toList()));

    assertNotNull(outputStream);
    XSSFSheet sheet = new XSSFWorkbook(new ByteArrayInputStream(outputStream.toByteArray()))
        .getSheetAt(0);

    int rowIndex = 1;
    rowIndex = validateTemplateLevel(sheet,
        mockIndicators.stream().filter(x -> x.getLevel().equals(mockLevels[3]))
            .collect(Collectors.toList()),
        rowIndex, IndicatorService.IMPACT_NUM_TEMP_INDIC);
    rowIndex = validateTemplateLevel(sheet,
        mockIndicators.stream().filter(x -> x.getLevel().equals(mockLevels[1]))
            .collect(Collectors.toList()),
        rowIndex, IndicatorService.OUTCOME_NUM_TEMP_INDIC);
    validateTemplateLevel(sheet, Collections.emptyList(), rowIndex,
        IndicatorService.OUTPUT_NUM_TEMP_INDIC);
    sheet.getWorkbook().close();
  }

  @Test
  void exportIndicatorsDFIDFormat_newRowsForEveryLevel() throws IOException {
    List<Indicator> mockIndicators = mockIndicatorList();
    mockIndicators.addAll(mockIndicatorList());
    mockIndicators.addAll(mockIndicatorList());
    when(indicatorRepository.findAllById(any())).thenReturn(mockIndicators);

    ByteArrayOutputStream outputStream = indicatorService
        .exportIndicatorsDFIDFormat(mockIndicators.stream()
            .map(indicatorService::convertIndicatorToIndicatorResponse)
            .collect(Collectors.toList()));

    assertNotNull(outputStream);
    XSSFSheet sheet = new XSSFWorkbook(new ByteArrayInputStream(outputStream.toByteArray()))
        .getSheetAt(0);

    int rowIndex = 1;
    rowIndex = validateTemplateLevel(sheet,
        mockIndicators.stream().filter(x -> x.getLevel().equals(mockLevels[3]))
            .collect(Collectors.toList()),
        rowIndex, IndicatorService.IMPACT_NUM_TEMP_INDIC);
    rowIndex = validateTemplateLevel(sheet,
        mockIndicators.stream().filter(x -> x.getLevel().equals(mockLevels[1]))
            .collect(Collectors.toList()),
        rowIndex, IndicatorService.OUTCOME_NUM_TEMP_INDIC);
    validateTemplateLevel(sheet,
        mockIndicators.stream().filter(x -> x.getLevel().equals(mockLevels[0]))
            .collect(Collectors.toList()),
        rowIndex, IndicatorService.OUTPUT_NUM_TEMP_INDIC);
    sheet.getWorkbook().close();
  }

  List<Indicator> mockIndicatorList() {
    String keyword = "food insecurity,nutrition,farming,agriculture";
    List<Indicator> list = new ArrayList<>();

    List<String> keywordsList = new ArrayList<>();
    keywordsList.add("agriculture");
    keywordsList.add("nutrition");

    List<String> keywordsPolicyList = new ArrayList<>();
    keywordsPolicyList.add("policy");

    List<String> keywordsGovList = new ArrayList<>();
    keywordsGovList.add("government");

    List<String> keywordsGovPolicyList = new ArrayList<>();
    keywordsGovPolicyList.add("government policies");
    keywordsGovPolicyList.add("policy");

    list.add(
        Indicator.builder().id(1L).name("Number of food insecure people receiving EU assistance")
            .themes("Global Partnership for Sustainable Development")
            .source("UN Sustainable Development Goals")
            .disaggregation(true)
            .crsCode("51010.0")
            .sdgCode("19.4")
            .sourceVerification("Capacity4Dev")
            .dataSource("https://data.worldbank.org/indicator/SN.ITK.VITA.ZS?view=chart")
            .description("Food & Agriculture").keywords(keyword).level(mockLevels[1])
            .keywordsList(keywordsList).build());
    list.add(Indicator.builder().id(4L).name(
        "Number of policies/strategies/laws/regulation developed/revised for digitalization with EU support")
        .themes("Global Partnership for Sustainable Development")
        .source("UN Sustainable Development Goals")
        .disaggregation(true)
        .crsCode("43060.0")
        .sdgCode("1.a")
        .sourceVerification("Capacity4Dev")
        .dataSource("https://data.worldbank.org/indicator/SI.POV.URGP?view=chart")
        .description("Digitalisation").keywords("policy").level(mockLevels[0])
        .keywordsList(keywordsPolicyList).build());
    list.add(Indicator.builder().id(5L).name("Revenue, excluding grants (% of GDP)")
        .themes("Global Partnership for Sustainable Development")
        .source("UN Sustainable Development Goals")
        .disaggregation(false)
        .crsCode("99810.0")
        .sdgCode("17.4")
        .sourceVerification("HIPSO")
        .dataSource("https://data.worldbank.org/indicator/EG.CFT.ACCS.ZS?view=chart")
        .description("Technical Note, EURF 2.01").keywords("government").level(mockLevels[3])
        .keywordsList(keywordsGovList).build());
    list.add(Indicator.builder().id(73L).name(
        "Number of government policies developed or revised with civil society organisation participation through EU support")
        .description("Public Sector").keywords("government policies, policy").level(mockLevels[1])
        .keywordsList(keywordsGovPolicyList)
        .sourceVerification(mockSourceVerification.get(3)).build());

    return list;
  }
}
