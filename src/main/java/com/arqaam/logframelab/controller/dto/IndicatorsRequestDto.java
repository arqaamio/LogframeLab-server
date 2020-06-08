package com.arqaam.logframelab.controller.dto;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
public class IndicatorsRequestDto {

  @Min(1)
  private int page;

  @Min(1)
  private int pageSize;

  @NotNull
  @Valid
  private SortDto sortBy;
}
