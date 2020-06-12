package com.arqaam.logframelab.controller.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class IndicatorRequestDto {

  private Long id;
  private String crsCode;
  private String dataSource;
  private String description;
  private Boolean disaggregation;
  private String keywords;
  private String name;
  private String sdgCode;
  private String source;
  private String sourceVerification;
  private String themes;
  private Long levelId;
}
