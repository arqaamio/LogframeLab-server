package com.arqaam.logframelab.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;

import com.arqaam.logframelab.model.IndicatorResponse;
import com.arqaam.logframelab.model.persistence.Indicator;
import com.arqaam.logframelab.model.persistence.Level;
import com.arqaam.logframelab.repository.IndicatorRepository;
import com.arqaam.logframelab.repository.LevelRepository;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.jpa.domain.Specification;

public abstract class BaseIndicatorServiceTest {

  final static Level[] mockLevels = new Level[]{
      new Level(1L, "OUTPUT", "OUTPUT", "{output}", "green", 3),
      new Level(2L, "OUTCOME", "OUTCOME", "{outcomes}", "red", 2),
      new Level(3L, "OTHER_OUTCOMES", "OTHER OUTCOMES", "{otheroutcomes}", "orange", 4),
      new Level(4L, "IMPACT", "IMPACT", "{impact}", "purple", 1)
  };
  final static List<String> mockThemes = Arrays.asList("Digitalisation", "Education", "Poverty",
      "Nutrition", "Agriculture", "Health", "WASH", "Electricity", "Private Sector",
      "Infrastructure", "Migration", "Climate Change", "Environment", "Public Sector",
      "Human Rights", "Conflict", "Food Security", "Equality", "Water and Sanitation");
  final static List<String> mockSources = Arrays
      .asList("Capacity4Dev", "EU", "WFP", "ECHO", "ECHO,WFP",
          "ECHO,WHO", "FAO", "FAO,WHO", "WHO", "FANTA", "IPA", "WHO,FAO", "ACF",
          "Nutrition Cluster", "Freendom House", "CyberGreen", "ITU",
          "UN Sustainable Development Goals", "World Bank", "UNDP", "ILO", "IMF");
  final static List<String> mockSdgCodes = Arrays.asList("8.2", "7.1", "4.1", "1.a", "1.b");
  final static List<String> mockCrsCodes = Arrays
      .asList("99810.0", "15160.0", "24010.0", "15190.0", "43010.0", "24050.0", "43030.0");
  final static List<Long> mockLevelsId = Arrays.stream(mockLevels).map(Level::getId)
      .collect(Collectors.toList());
  final static List<String> mockSourceVerification = Arrays
      .asList("World Bank Data", "EU", "SDG Country Data",
          "Project's M&E system", "UNDP Global Human Development Indicators");
  @Mock
  IndicatorRepository indicatorRepository;
  @Mock
  LevelRepository levelRepository;
  @InjectMocks
  IndicatorService indicatorService;

  @BeforeEach
  void setup() {

    lenient().when(levelRepository.findAll()).thenReturn(Arrays.asList(mockLevels));
    lenient().when(levelRepository.findAllByOrderByPriority())
        .thenReturn(Arrays.stream(mockLevels).sorted().collect(Collectors.toList()));
    lenient().when(indicatorRepository.save(any(Indicator.class)))
        .thenAnswer(i -> i.getArguments()[0]);
    lenient().when(indicatorRepository.findAll()).thenReturn(mockIndicatorList());
    lenient().when(indicatorRepository.findAllById(any())).thenReturn(mockIndicatorList());
    lenient().when(indicatorRepository.findAll(any(Specification.class))).
        thenReturn(mockIndicatorList().stream()
            .filter(x -> mockThemes.contains(x.getThemes()) && mockLevelsId
                .contains(x.getLevel().getId()) && mockSources.contains(x.getSource())
                && mockSdgCodes.contains(x.getSdgCode()) && mockCrsCodes.contains(x.getCrsCode()))
            .collect(Collectors.toList()));
  }

  abstract List<Indicator> mockIndicatorList();

  Integer validateTemplateLevel(XSSFSheet sheet, List<Indicator> indicators, Integer rowIndex,
      Integer numberTemplateIndicators) {
    List<CellRangeAddress> mergedRegions = sheet.getMergedRegions();
    Integer initialRow = rowIndex;

    for (Indicator indicator : indicators) {
      assertEquals("", sheet.getRow(rowIndex + 1).getCell(3).getStringCellValue());
      assertEquals(indicator.getName(), sheet.getRow(rowIndex + 1).getCell(2).getStringCellValue());
      assertEquals(indicator.getSourceVerification(),
          sheet.getRow(rowIndex + 3).getCell(3).getStringCellValue());
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

  List<IndicatorResponse> getExpectedResult() {
    List<Indicator> indicators = mockIndicatorList();
    List<IndicatorResponse> indicatorResponses = new ArrayList<>();
    indicatorResponses.add(indicatorService.convertIndicatorToIndicatorResponse(indicators.get(2)));
    indicatorResponses.add(indicatorService.convertIndicatorToIndicatorResponse(indicators.get(1)));
    indicatorResponses.add(indicatorService.convertIndicatorToIndicatorResponse(indicators.get(3)));
    indicatorResponses.add(indicatorService.convertIndicatorToIndicatorResponse(indicators.get(0)));
    return indicatorResponses;
  }
}
