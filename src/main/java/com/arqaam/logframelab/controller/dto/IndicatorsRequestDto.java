package com.arqaam.logframelab.controller.dto;

import java.util.Collection;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import com.arqaam.logframelab.model.persistence.CRSCode;
import com.arqaam.logframelab.model.persistence.SDGCode;
import com.arqaam.logframelab.model.persistence.Source;
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
    private Collection<String> sectors;

    @Getter
    private Collection<Long> sourceIds;

    private Collection<Long> sdg_code;
    private Collection<Long> crs_code;

    public Collection<Long> getSdgCodeIds() {
      return sdg_code;
    }

    public Collection<Long> getCrsCodeIds() {
      return crs_code;
    }
  }
}

