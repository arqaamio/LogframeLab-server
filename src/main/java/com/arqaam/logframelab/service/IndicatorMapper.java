package com.arqaam.logframelab.service;

import com.arqaam.logframelab.controller.dto.IndicatorRequestDto;
import com.arqaam.logframelab.model.persistence.CRSCode;
import com.arqaam.logframelab.model.persistence.Indicator;
import com.arqaam.logframelab.model.persistence.SDGCode;
import com.arqaam.logframelab.model.persistence.Source;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface IndicatorMapper {

  IndicatorMapper INSTANCE = Mappers.getMapper(IndicatorMapper.class);

  @Mapping(source = "level.id", target = "levelId")
  @Mapping(target = "source", qualifiedByName = "mapSourceId")
  @Mapping(target = "sdgCode", qualifiedByName = "mapSDGCodeId")
  @Mapping(target = "crsCode", qualifiedByName = "mapCRSCodeId")
  IndicatorRequestDto indicatorToIndicatorRequestDto(Indicator indicator);

  @Named("mapSourceId")
  default Long mapId(Source source){
    return source.getId();
  }

  @Named("mapSDGCodeId")
  default Long mapId(SDGCode sdgCode){
    return sdgCode.getId();
  }

  @Named("mapCRSCodeId")
  default Long mapId(CRSCode crsCode){
    return crsCode.getId();
  }
}
