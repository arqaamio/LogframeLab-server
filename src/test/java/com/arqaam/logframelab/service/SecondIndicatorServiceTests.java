package com.arqaam.logframelab.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.arqaam.logframelab.model.IndicatorResponse;
import com.arqaam.logframelab.model.persistence.Indicator;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.StringUtils;

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

    ByteArrayOutputStream outputStream = indicatorService.exportIndicatorsDFIDFormat(getExpectedResult(true));
    assertNotNull(outputStream);
    XSSFSheet sheet = new XSSFWorkbook(new ByteArrayInputStream(outputStream.toByteArray()))
        .getSheetAt(0);

    int rowIndex = 1;
    rowIndex = validateDFIDTemplateLevel(sheet, impactIndicators, rowIndex,
        IndicatorService.IMPACT_NUM_TEMP_INDIC);
    rowIndex = validateDFIDTemplateLevel(sheet, outcomeIndicators, rowIndex,
        IndicatorService.OUTCOME_NUM_TEMP_INDIC);
    validateDFIDTemplateLevel(sheet, outputIndicators, rowIndex,
        IndicatorService.OUTPUT_NUM_TEMP_INDIC);

    sheet.getWorkbook().close();
  }

  @Test
  void exportIndicatorsDFIDFormat_withoutValuesAndDates() throws IOException {
    List<Indicator> indicatorList = mockIndicatorList().stream().peek(x -> {x.setDate(null);x.setValue(null);}).collect(Collectors.toList());
    when(indicatorRepository.findAllById(any())).thenReturn(indicatorList);
    List<Indicator> impactIndicators = indicatorList.stream()
            .filter(x -> x.getLevel().equals(mockLevels[3])).collect(Collectors.toList());
    List<Indicator> outcomeIndicators = indicatorList.stream()
            .filter(x -> x.getLevel().equals(mockLevels[1])).collect(Collectors.toList());
    List<Indicator> outputIndicators = indicatorList.stream()
            .filter(x -> x.getLevel().equals(mockLevels[0])).collect(Collectors.toList());

    ByteArrayOutputStream outputStream = indicatorService.exportIndicatorsDFIDFormat(getExpectedResult(true));

    assertNotNull(outputStream);
    XSSFSheet sheet = new XSSFWorkbook(new ByteArrayInputStream(outputStream.toByteArray()))
            .getSheetAt(0);

    int rowIndex = 1;
    rowIndex = validateDFIDTemplateLevel(sheet, impactIndicators, rowIndex,
            IndicatorService.IMPACT_NUM_TEMP_INDIC);
    rowIndex = validateDFIDTemplateLevel(sheet, outcomeIndicators, rowIndex,
            IndicatorService.OUTCOME_NUM_TEMP_INDIC);
    validateDFIDTemplateLevel(sheet, outputIndicators, rowIndex,
            IndicatorService.OUTPUT_NUM_TEMP_INDIC);

    sheet.getWorkbook().close();
  }

  @Test
  void exportIndicatorsDFIDFormat_noImpactIndicators_newRowsOutcome() throws IOException {
    List<Indicator> mockIndicators = mockIndicatorList().stream()
        .filter(x -> !x.getLevel().equals(mockLevels[3])).collect(Collectors.toList());
    mockIndicators.add(new Indicator(100L, "Extra indicator 1", "", "", mockLevels[1], "",
        "", false, "", "", mockSourceVerification.get(0), "", null, null, null, 0));
    mockIndicators.add(new Indicator(100L, "Extra indicator 2", "", "", mockLevels[1], "",
        "", false, "", "", mockSourceVerification.get(1), "", null, null, null, 0));
    mockIndicators.add(new Indicator(100L, "Extra indicator 3", "", "", mockLevels[1], "",
        "", false, "", "", mockSourceVerification.get(2), "", null, null, null, 0));
    when(indicatorRepository.findAllById(any())).thenReturn(mockIndicators);

    ByteArrayOutputStream outputStream = indicatorService
        .exportIndicatorsDFIDFormat(mockIndicators.stream()
            .map(indicatorService::convertIndicatorToIndicatorResponse)
            .collect(Collectors.toList()));

    assertNotNull(outputStream);
    XSSFSheet sheet = new XSSFWorkbook(new ByteArrayInputStream(outputStream.toByteArray()))
        .getSheetAt(0);

    int rowIndex = 1;
    rowIndex = validateDFIDTemplateLevel(sheet, Collections.emptyList(), rowIndex,
        IndicatorService.IMPACT_NUM_TEMP_INDIC);
    rowIndex = validateDFIDTemplateLevel(sheet,
        mockIndicators.stream().filter(x -> x.getLevel().equals(mockLevels[1]))
            .collect(Collectors.toList()),
        rowIndex, IndicatorService.OUTCOME_NUM_TEMP_INDIC);
    validateDFIDTemplateLevel(sheet,
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
        "", false, "", "", mockSourceVerification.get(0), "", null, null, null, 0));
    mockIndicators.add(new Indicator(100L, "Extra indicator 2", "", "", mockLevels[0], "",
        "", false, "", "", mockSourceVerification.get(1), "", null, null, null, 0));
    mockIndicators.add(new Indicator(100L, "Extra indicator 3", "", "", mockLevels[0], "",
        "", false, "", "", mockSourceVerification.get(2), "", null, null, null, 0));
    when(indicatorRepository.findAllById(any())).thenReturn(mockIndicators);

    ByteArrayOutputStream outputStream = indicatorService
        .exportIndicatorsDFIDFormat(mockIndicators.stream()
            .map(indicatorService::convertIndicatorToIndicatorResponse)
            .collect(Collectors.toList()));

    assertNotNull(outputStream);
    XSSFSheet sheet = new XSSFWorkbook(new ByteArrayInputStream(outputStream.toByteArray()))
        .getSheetAt(0);

    int rowIndex = 1;
    rowIndex = validateDFIDTemplateLevel(sheet,
        mockIndicators.stream().filter(x -> x.getLevel().equals(mockLevels[3]))
            .collect(Collectors.toList()),
        rowIndex, IndicatorService.IMPACT_NUM_TEMP_INDIC);
    rowIndex = validateDFIDTemplateLevel(sheet, Collections.emptyList(), rowIndex,
        IndicatorService.OUTCOME_NUM_TEMP_INDIC);
    validateDFIDTemplateLevel(sheet,
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
        "", false, "", "", mockSourceVerification.get(0), "", null, null, null, 0));
    mockIndicators.add(new Indicator(100L, "Extra indicator 2", "", "", mockLevels[3], "",
        "", false, "", "", mockSourceVerification.get(1), "", null, null, null, 0));
    mockIndicators.add(new Indicator(100L, "Extra indicator 3", "", "", mockLevels[3], "",
        "", false, "", "", mockSourceVerification.get(2), "", null, null, null, 0));
    when(indicatorRepository.findAllById(any())).thenReturn(mockIndicators);

    ByteArrayOutputStream outputStream = indicatorService
        .exportIndicatorsDFIDFormat(mockIndicators.stream()
            .map(indicatorService::convertIndicatorToIndicatorResponse)
            .collect(Collectors.toList()));

    assertNotNull(outputStream);
    XSSFSheet sheet = new XSSFWorkbook(new ByteArrayInputStream(outputStream.toByteArray()))
        .getSheetAt(0);

    int rowIndex = 1;
    rowIndex = validateDFIDTemplateLevel(sheet,
        mockIndicators.stream().filter(x -> x.getLevel().equals(mockLevels[3]))
            .collect(Collectors.toList()),
        rowIndex, IndicatorService.IMPACT_NUM_TEMP_INDIC);
    rowIndex = validateDFIDTemplateLevel(sheet,
        mockIndicators.stream().filter(x -> x.getLevel().equals(mockLevels[1]))
            .collect(Collectors.toList()),
        rowIndex, IndicatorService.OUTCOME_NUM_TEMP_INDIC);
    validateDFIDTemplateLevel(sheet, Collections.emptyList(), rowIndex,
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
    rowIndex = validateDFIDTemplateLevel(sheet,
        mockIndicators.stream().filter(x -> x.getLevel().equals(mockLevels[3]))
            .collect(Collectors.toList()),
        rowIndex, IndicatorService.IMPACT_NUM_TEMP_INDIC);
    rowIndex = validateDFIDTemplateLevel(sheet,
        mockIndicators.stream().filter(x -> x.getLevel().equals(mockLevels[1]))
            .collect(Collectors.toList()),
        rowIndex, IndicatorService.OUTCOME_NUM_TEMP_INDIC);
    validateDFIDTemplateLevel(sheet,
        mockIndicators.stream().filter(x -> x.getLevel().equals(mockLevels[0]))
            .collect(Collectors.toList()),
        rowIndex, IndicatorService.OUTPUT_NUM_TEMP_INDIC);
    sheet.getWorkbook().close();
  }

  Integer validateDFIDTemplateLevel(XSSFSheet sheet, List<Indicator> indicators, Integer rowIndex,
                                    Integer numberTemplateIndicators) {
    List<CellRangeAddress> mergedRegions = sheet.getMergedRegions();
    Integer initialRow = rowIndex;
    String baselineValue;
    for (Indicator indicator : indicators) {
      assertEquals(indicator.getName(), sheet.getRow(rowIndex + 1).getCell(2).getStringCellValue());
      assertEquals(indicator.getSourceVerification(),
              sheet.getRow(rowIndex + 3).getCell(3).getStringCellValue());
      baselineValue = !indicator.getLevel().equals(mockLevels[3]) ||
              StringUtils.isEmpty(indicator.getValue()) || StringUtils.isEmpty(indicator.getDate())
              ? "" : indicator.getValue() + " (" + indicator.getDate() + ")";
      assertEquals(baselineValue, sheet.getRow(rowIndex + 1).getCell(3).getStringCellValue());
      rowIndex += 4;
    }

    int count = indicators.size();
    while (count < numberTemplateIndicators) {
      assertEquals("", sheet.getRow(rowIndex + 1).getCell(2).getStringCellValue());
      assertEquals("", sheet.getRow(rowIndex + 3).getCell(3).getStringCellValue());
      rowIndex += 4;
      count++;
    }

    // check merged cells in first column
    int finalRowIndex = rowIndex;
    if (indicators.size() > numberTemplateIndicators) {
      if (numberTemplateIndicators.equals(IndicatorService.OUTPUT_NUM_TEMP_INDIC)) {
        assertTrue(mergedRegions.stream().anyMatch(x -> x.getLastColumn() == 0
                && x.getFirstRow() == initialRow + numberTemplateIndicators * 4 - 1
                && x.getLastRow() == finalRowIndex - 1));
      } else {
        assertTrue(mergedRegions.stream().anyMatch(x -> x.getLastColumn() == 0
                && x.getFirstRow() == initialRow + numberTemplateIndicators * 3
                && x.getLastRow() == finalRowIndex - 1));
      }
    }

    return rowIndex;
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
            .date("2000")
            .value("100")
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
        .date("1979")
        .value("50")
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
        .date("2001")
        .value("100")
        .keywordsList(keywordsGovList).build());
    list.add(Indicator.builder().id(73L).name(
        "Number of government policies developed or revised with civil society organisation participation through EU support")
        .description("Public Sector").keywords("government policies, policy").level(mockLevels[1])
        .keywordsList(keywordsGovPolicyList)
        .date("1999")
        .value("80")
        .sourceVerification(mockSourceVerification.get(3)).build());

    return list;
  }
}
