package com.arqaam.logframelab.service;

import com.arqaam.logframelab.controller.dto.IndicatorRequestDto;
import com.arqaam.logframelab.controller.dto.IndicatorsRequestDto;
import com.arqaam.logframelab.model.persistence.Indicator;
import com.arqaam.logframelab.repository.IndicatorRepository;
import com.arqaam.logframelab.repository.LevelRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

@Service
public class IndicatorsManagementServiceImpl implements IndicatorsManagementService {

  private final IndicatorRepository indicatorRepository;
  private final LevelRepository levelRepository;

  public IndicatorsManagementServiceImpl(IndicatorRepository indicatorRepository,
      LevelRepository levelRepository) {
    this.indicatorRepository = indicatorRepository;
    this.levelRepository = levelRepository;
  }

  @Override
  public Page<Indicator> getIndicators(IndicatorsRequestDto indicatorsRequest) {
    PageRequest page = PageRequest
        .of(indicatorsRequest.getPage() - 1, indicatorsRequest.getPageSize());

    if (indicatorsRequest.getSortBy() != null) {
      page = PageRequest.of(indicatorsRequest.getPage() - 1, indicatorsRequest.getPageSize(),
          Direction.fromString(indicatorsRequest.getSortBy().getDirection()),
          indicatorsRequest.getSortBy().getProperty());
    }

    return indicatorRepository.findAll(page);
  }

  @Override
  public Indicator saveIndicator(IndicatorRequestDto createIndicatorRequest) {
    return indicatorRepository.save(
        Indicator.builder().id(createIndicatorRequest.getId())
            .description(createIndicatorRequest.getDescription())
            .name(createIndicatorRequest.getName())
            .level(levelRepository.findById(createIndicatorRequest.getLevelId()).orElse(null))
            .keywords(createIndicatorRequest.getKeywords())
            .crsCode(createIndicatorRequest.getCrsCode())
            .sdgCode(createIndicatorRequest.getSdgCode())
            .source(createIndicatorRequest.getSource())
            .themes(createIndicatorRequest.getThemes())
            .sourceVerification(createIndicatorRequest.getSourceVerification())
            .dataSource(createIndicatorRequest.getDataSource())
            .disaggregation(createIndicatorRequest.getDisaggregation())
            .build());
  }

  @Override
  public void deleteIndicator(Long id) {
    indicatorRepository.deleteById(id);
  }
}
