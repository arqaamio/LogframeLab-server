package com.arqaam.logframelab.service;

import com.arqaam.logframelab.controller.dto.IndicatorRequestDto;
import com.arqaam.logframelab.model.persistence.Indicator;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface IndicatorMapper {

  @Mapping(source = "level.id", target = "levelId")
  IndicatorRequestDto indicatorToIndicatorRequestDto(Indicator indicator);
}
