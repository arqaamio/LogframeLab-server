package com.arqaam.logframelab.controller.dto;

import lombok.Data;

import java.util.Set;

@Data
public class IndicatorUploadDto {
  private Set<String> themes;
  private Set<String> source;
  private Set<String> sdg_code;
  private Set<Integer> level;
  private Set<String> crs_code;
}
