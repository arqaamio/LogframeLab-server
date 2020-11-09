package com.arqaam.logframelab.service;

import com.arqaam.logframelab.model.persistence.Indicator;
import org.apache.logging.log4j.util.Strings;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.StringUtils;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

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

    ByteArrayOutputStream outputStream = indicatorService.exportIndicatorsDFIDFormat(getExpectedResult(true, null));
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

    ByteArrayOutputStream outputStream = indicatorService.exportIndicatorsDFIDFormat(getExpectedResult(true, indicatorList));

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
        null, false, null, null, mockSourceVerification.get(0), "", false, false, 0L, null, null, null, null, 0));
    mockIndicators.add(new Indicator(100L, "Extra indicator 2", "", "", mockLevels[1], "",
        null, false, null, null, mockSourceVerification.get(1), "", false, false, 0L, null, null, null, null, 0));
    mockIndicators.add(new Indicator(100L, "Extra indicator 3", "", "", mockLevels[1], "",
        null, false, null, null, mockSourceVerification.get(2), "", false, false, 0L, null,null, null, null, 0));
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
        null, false, null, null, mockSourceVerification.get(0), "", false, false, 0L, null, null, null, null, 0));
    mockIndicators.add(new Indicator(100L, "Extra indicator 2", "", "", mockLevels[0], "",
        null, false, null, null, mockSourceVerification.get(1), "", false, false, 0L, null, null, null, null, 0));
    mockIndicators.add(new Indicator(100L, "Extra indicator 3", "", "", mockLevels[0], "",
        null, false, null, null, mockSourceVerification.get(2), "", false, false, 0L, null, null, null, null, 0));
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
        null, false, null, null, mockSourceVerification.get(0), "", false, false, 0L, null, null, null, null, 0));
    mockIndicators.add(new Indicator(100L, "Extra indicator 2", "", "", mockLevels[3], "",
        null, false, null, null, mockSourceVerification.get(1), "", false, false, 0L, null, null, null, null, 0));
    mockIndicators.add(new Indicator(100L, "Extra indicator 3", "", "", mockLevels[3], "",
        null, false, null, null, mockSourceVerification.get(2), "", false, false, 0L, null, null, null, null, 0));
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

  @Test
  void exportIndicatorsDFIDFormat_withMultipleStatements() throws IOException {
    List<Indicator> indicators = mockIndicatorList();
    indicators.add(Indicator.builder().id(2L)
            .level(mockLevels[1])
            .name("Fake indicator")
            .description("")
            .sector("")
            .disaggregation(false)
            .crsCode(null)
            .sdgCode(null)
            .source(null)
            .score(null)
            .sourceVerification("Source verification")
            .value("")
            .date("").statement("Statement 2").build());
    when(indicatorRepository.findAllById(any())).thenReturn(indicators);
    List<Indicator> impactIndicators = indicators.stream()
            .filter(x -> x.getLevel().equals(mockLevels[3])).collect(Collectors.toList());
    List<Indicator> outcomeIndicators = indicators.stream()
            .filter(x -> x.getLevel().equals(mockLevels[1])).collect(Collectors.toList());
    List<Indicator> outputIndicators = indicators.stream()
            .filter(x -> x.getLevel().equals(mockLevels[0])).collect(Collectors.toList());

    ByteArrayOutputStream outputStream = indicatorService.exportIndicatorsDFIDFormat(getExpectedResult(true, indicators));
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

  Integer validateDFIDTemplateLevel(XSSFSheet sheet, List<Indicator> indicators, Integer rowIndex,
                                    Integer numberTemplateIndicators) {
    List<CellRangeAddress> mergedRegions = sheet.getMergedRegions();
    Integer initialRow = rowIndex;
    String baselineValue;
    Map<String, List<Indicator>> map = new HashMap<>();
    for (Indicator ind : indicators) {
      if (map.containsKey(ind.getStatement())) {
        map.get(ind.getStatement()).add(ind);
      } else {
        List<Indicator> indicators1 = new ArrayList<>();
        indicators1.add(ind);
        map.put(ind.getStatement(), indicators1);
      }
    }
    Iterator<Map.Entry<String, List<Indicator>>> iterator = map.entrySet().iterator();
    while(iterator.hasNext()) {
      List<Indicator> indicatorList = iterator.next().getValue();
      initialRow = rowIndex;
      Integer lastStatementRow = rowIndex;
      for (int i = 0; i < indicatorList.size(); i++) {
        if(Strings.isEmpty(indicatorList.get(i).getStatement())) {
//          if(i<){
            assertEquals(indicatorList.get(i).getLevel().getName(), sheet.getRow(rowIndex).getCell(0).getStringCellValue());
//          }
//          assertEquals(sheet.getRow(rowIndex).getCell(1).getStringCellValue().);
          assertEquals("", sheet.getRow(rowIndex + 1).getCell(0).getStringCellValue());
        } else {
          // Last statement
//          if(!iterator.hasNext() && indicatorList.size()-i+1<numberTemplateIndicators){
//            assertEquals("", sheet.getRow(rowIndex+1).getCell(0).getStringCellValue());
//          } else {
//            assertEquals(indicatorList.get(i).getStatement(), sheet.getRow(rowIndex+1).getCell(0).getStringCellValue());
//            lastStatementRow+=4;
//          }
        }

        assertEquals(indicatorList.get(i).getName(), sheet.getRow(rowIndex + 1).getCell(1).getStringCellValue());
        assertEquals(indicatorList.get(i).getSourceVerification(),
                sheet.getRow(rowIndex + 3).getCell(2).getStringCellValue());
        baselineValue = !indicatorList.get(i).getLevel().equals(mockLevels[3]) ||
                StringUtils.isEmpty(indicatorList.get(i).getValue()) || StringUtils.isEmpty(indicatorList.get(i).getDate())
                ? "" : indicatorList.get(i).getValue() + " (" + indicatorList.get(i).getDate() + ")";
        assertEquals(baselineValue, sheet.getRow(rowIndex + 1).getCell(2).getStringCellValue());

        rowIndex+=4;
      }
      if(!iterator.hasNext() && indicatorList.size()<numberTemplateIndicators) {
          rowIndex=rowIndex + 4*(numberTemplateIndicators - indicatorList.size());
      }

      // assert merge
//      if(!indicatorList.get(0).getLevel().getName().equals("IMPACT")){
//        Integer finalInitialRow = initialRow;
//        Integer finalLastStatementRow = lastStatementRow;
//        assertTrue(mergedRegions.stream().anyMatch(x -> x.getLastColumn() == 0
//                && x.getFirstRow() == finalInitialRow + 1
//                && x.getLastRow() == finalLastStatementRow -1));
//      }
    }
    if(indicators.isEmpty()) {
      int count = 0;
      while (count < numberTemplateIndicators) {
        assertEquals("", sheet.getRow(rowIndex + 1).getCell(1).getStringCellValue());
        assertEquals("", sheet.getRow(rowIndex + 3).getCell(2).getStringCellValue());
        rowIndex += 4;
        count++;
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
            .sector("Global Partnership for Sustainable Development")
            .source(Collections.singleton(mockSources.get(1)))
            .disaggregation(true)
            .crsCode(Collections.singleton(mockCrsCodes.get(1)))
            .sdgCode(Collections.singleton(mockSdgCodes.get(1)))
            .sourceVerification("Capacity4Dev")
            .dataSource("https://data.worldbank.org/indicator/SN.ITK.VITA.ZS?view=chart")
            .description("Food & Agriculture").keywords(keyword).level(mockLevels[1])
            .date("2000")
            .value("100")
            .statement("Statement Outcome")
            .keywordsList(keywordsList).build());
    list.add(Indicator.builder().id(4L).name(
        "Number of policies/strategies/laws/regulation developed/revised for digitalization with EU support")
        .sector("Global Partnership for Sustainable Development")
        .source(Collections.singleton(mockSources.get(2)))
        .disaggregation(true)
        .crsCode(Collections.singleton(mockCrsCodes.get(2)))
        .sdgCode(Collections.singleton(mockSdgCodes.get(2)))
        .sourceVerification("Capacity4Dev")
        .dataSource("https://data.worldbank.org/indicator/SI.POV.URGP?view=chart")
        .description("Digitalisation").keywords("policy").level(mockLevels[0])
        .date("1979")
        .value("50")
        .statement("Statement Output")
        .keywordsList(keywordsPolicyList).build());
    list.add(Indicator.builder().id(5L).name("Revenue, excluding grants (% of GDP)")
        .sector("Global Partnership for Sustainable Development")
        .source(Collections.singleton(mockSources.get(3)))
        .disaggregation(false)
        .crsCode(Collections.singleton(mockCrsCodes.get(3)))
        .sdgCode(Collections.singleton(mockSdgCodes.get(3)))
        .sourceVerification("HIPSO")
        .dataSource("https://data.worldbank.org/indicator/EG.CFT.ACCS.ZS?view=chart")
        .description("Technical Note, EURF 2.01").keywords("government").level(mockLevels[3])
        .date("2001")
        .value("100")
        .statement("Statement Impact")
        .keywordsList(keywordsGovList).build());
    list.add(Indicator.builder().id(73L).name(
        "Number of government policies developed or revised with civil society organisation participation through EU support")
        .description("Public Sector").keywords("government policies, policy").level(mockLevels[1])
        .keywordsList(keywordsGovPolicyList)
        .date("1999")
        .value("80")
        .statement("Statement Outcome")
        .sourceVerification(mockSourceVerification.get(3)).build());

    return list;
  }
}
