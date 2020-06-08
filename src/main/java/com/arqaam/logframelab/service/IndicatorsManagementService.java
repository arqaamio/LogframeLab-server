package com.arqaam.logframelab.service;

import com.arqaam.logframelab.controller.dto.IndicatorRequestDto;
import com.arqaam.logframelab.controller.dto.IndicatorsRequestDto;
import com.arqaam.logframelab.model.persistence.Indicator;
import org.springframework.data.domain.Page;

public interface IndicatorsManagementService {

  Page<Indicator> getIndicators(IndicatorsRequestDto indicatorsRequest);

  Indicator saveIndicator(IndicatorRequestDto createIndicatorRequest);

  void deleteIndicator(Long id);
}
