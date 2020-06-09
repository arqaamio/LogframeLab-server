package com.arqaam.logframelab.service;

import com.arqaam.logframelab.model.persistence.Indicator;
import com.arqaam.logframelab.model.persistence.TempIndicator;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface IndicatorMapper {

  TempIndicator indicatorToTempIndicator(Indicator indicator);

  Indicator tempIndicatorToIndicator(TempIndicator tempIndicator);
}
