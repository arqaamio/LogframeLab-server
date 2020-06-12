package com.arqaam.logframelab.service;

import com.arqaam.logframelab.controller.dto.IndicatorRequestDto;
import com.arqaam.logframelab.model.persistence.Indicator;
import com.arqaam.logframelab.model.persistence.TempIndicator;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface IndicatorMapper {

  TempIndicator indicatorToTempIndicator(Indicator indicator);

  Indicator tempIndicatorToIndicator(TempIndicator tempIndicator);

  @Mapping(source = "level.id", target = "levelId")
  IndicatorRequestDto indicatorToIndicatorRequestDto(Indicator indicator);
}
