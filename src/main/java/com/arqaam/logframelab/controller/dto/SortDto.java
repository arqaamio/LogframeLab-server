package com.arqaam.logframelab.controller.dto;

import javax.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SortDto {

  @NotBlank
  String property;

  @NotBlank
  String direction;
}
