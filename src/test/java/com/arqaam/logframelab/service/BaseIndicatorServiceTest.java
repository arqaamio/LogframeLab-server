package com.arqaam.logframelab.service;

import com.arqaam.logframelab.model.IndicatorResponse;
import com.arqaam.logframelab.model.persistence.*;
import com.arqaam.logframelab.repository.*;
import com.arqaam.logframelab.util.DocManipulationUtil;
import com.arqaam.logframelab.util.Utils;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;

public abstract class BaseIndicatorServiceTest {

  final static Level[] mockLevels = new Level[]{
          new Level(1L, "OUTPUT", "OUTPUT", 3),
          new Level(2L, "OUTCOME", "OUTCOME", 2),
          new Level(3L, "OTHER_OUTCOMES", "OTHER OUTCOMES", 4),
          new Level(4L, "IMPACT", "IMPACT", 1)
  };
  final static List<String> mockSectors = Arrays.asList("Digitalisation", "Education", "Poverty",
      "Nutrition", "Agriculture", "Health", "WASH", "Electricity", "Private Sector",
      "Infrastructure", "Migration", "Climate Change", "Environment", "Public Sector",
      "Human Rights", "Conflict", "Food Security", "Equality", "Water and Sanitation");
  final static List<Source> mockSources = Arrays.asList(
          new Source(1L, "Capacity4Dev"),new Source(1L, "EU"),new Source(2L, "WFP"),new Source(3L, "ECHO"),
          new Source(4L,"FAO"), new Source(5L,"WHO"), new Source(6L,"FANTA"), new Source(7L,"IPA"), new Source(8L, "ACF"),
          new Source(9L,"Nutrition Cluster"), new Source(10L,"Freedom House"), new Source(11L,"CyberGreen"), new Source(12L,"ITU"),
          new Source(13L,"UN Sustainable Development Goals"), new Source(14L,"World Bank"), new Source(15L,"UNDP"), new Source(16L,"ILO"),
          new Source(7L,"IMF"), new Source(19L, "OCHA Indicator Registry"));
  final static List<SDGCode> mockSdgCodes =  Arrays.asList(
          new SDGCode(1L,"End poverty in all its forms everywhere"),
          new SDGCode(2L,"End hunger, achieve food security and improved nutrition and promote sustainable agriculture"),
          new SDGCode(3L,"Ensure healthy lives and promote well-being for all at all ages"),
          new SDGCode(4L,"Ensure inclusive and equitable quality education and promote lifelong learning opportunities for all"),
          new SDGCode(5L,"Achieve gender equality and empower all women and girls"));
  final static List<CRSCode> mockCrsCodes = Arrays
          .asList(new CRSCode(998L,"Unallocated / Unspecified"),new CRSCode(151L, "Government & Civil Society-general"),
                  new CRSCode(240L, "Banking & Financial Services"), new CRSCode(112L, "Basic Education"), new CRSCode(720L, "Emergency Response"));
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
  SourceRepository sourceRepository;
  @Mock
  SDGCodeRepository sdgCodeRepository;
  @Mock
  CRSCodeRepository crsCodeRepository;
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
            .filter(x -> mockSectors.contains(x.getSector()) && mockLevelsId
                .contains(x.getLevel().getId()) && mockSources.containsAll(x.getSource())
                && mockSdgCodes.containsAll(x.getSdgCode()) && mockCrsCodes.containsAll(x.getCrsCode()))
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
