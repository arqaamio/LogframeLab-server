package com.arqaam.logframelab.controller.dto;

import lombok.Value;

@Value
public class IndicatorRequestDto {
  long id;
  String crsCode;
  String dataSource;
  String description;
  Boolean disaggregation;
  String keywords;
  String name;
  String sdgCode;
  String source;
  String sourceVerification;
  String themes;
  Long levelId;
}
