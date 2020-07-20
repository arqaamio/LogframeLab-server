package com.arqaam.logframelab.controller.dto;

import java.util.Collection;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;

@Data
public class IndicatorsRequestDto {

  @Min(1)
  private int page;

  @Min(1)
  private int pageSize;

  @NotNull
  @Valid
  private SortDto sortBy;

  private FilterRequestDto filters;

  @Data
  public static class FilterRequestDto {
    @Getter
    private Collection<Long> levelIds;

    @Getter
    private Collection<String> themes;

    @Getter
    private Collection<String> source;

    private Collection<String> sdg_code;
    private Collection<String> crs_code;

    public Collection<String> getSdgCode() {
      return sdg_code;
    }

    public Collection<String> getCrsCode() {
      return crs_code;
    }
  }
}

