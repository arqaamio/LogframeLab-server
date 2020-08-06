package com.arqaam.logframelab.service;

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

import com.arqaam.logframelab.util.DocManipulationUtil;
import com.arqaam.logframelab.util.Utils;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.jpa.domain.Specification;

public abstract class BaseIndicatorServiceTest  {

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
  @Mock
  DocManipulationUtil docManipulationUtil;
  @Mock
  Utils utils;

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

  List<IndicatorResponse> getExpectedResult(Boolean sorted) {
    List<Indicator> indicators = mockIndicatorList();
    List<IndicatorResponse> indicatorResponses = new ArrayList<>();
    if(sorted) {
      return indicators.stream().map(indicatorService::convertIndicatorToIndicatorResponse).collect(Collectors.toList());
    }else {
      indicatorResponses.add(indicatorService.convertIndicatorToIndicatorResponse(indicators.get(2)));
      indicatorResponses.add(indicatorService.convertIndicatorToIndicatorResponse(indicators.get(1)));
      indicatorResponses.add(indicatorService.convertIndicatorToIndicatorResponse(indicators.get(3)));
      indicatorResponses.add(indicatorService.convertIndicatorToIndicatorResponse(indicators.get(0)));
    }
    return indicatorResponses;
  }
}
