package com.arqaam.logframelab.controller.dto;

import com.arqaam.logframelab.model.persistence.CRSCode;
import com.arqaam.logframelab.model.persistence.SDGCode;
import com.arqaam.logframelab.model.persistence.Source;
import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class IndicatorRequestDto {

  private Long id;
  private Set<CRSCode> crsCode;
  private String dataSource;
  private String description;
  private Boolean disaggregation;
  private String keywords;
  private String name;
  private Set<SDGCode> sdgCode;
  private Set<Source> source;
  private String sourceVerification;
  private String sector;
  private Long levelId;
}
