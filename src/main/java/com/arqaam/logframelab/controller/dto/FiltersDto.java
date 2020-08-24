package com.arqaam.logframelab.controller.dto;

import com.arqaam.logframelab.model.persistence.CRSCode;
import com.arqaam.logframelab.model.persistence.Level;
import com.arqaam.logframelab.model.persistence.SDGCode;
import com.arqaam.logframelab.model.persistence.Source;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.LinkedHashSet;

@NoArgsConstructor
public class FiltersDto {

  @Getter
  private final Collection<String> sector = new LinkedHashSet<>();

  @Getter
  private final Collection<Source> source = new LinkedHashSet<>();

  @Getter
  private final Collection<Level> level = new LinkedHashSet<>();

  @Getter
  private final Collection<Long> levelIds = new LinkedHashSet<>();

  private final Collection<SDGCode> sdg_code = new LinkedHashSet<>();
  private final Collection<CRSCode> crs_code = new LinkedHashSet<>();


  public Collection<SDGCode> getSdgCode() {
    return sdg_code;
  }

  public Collection<CRSCode> getCrsCode() {
    return crs_code;
  }

  @JsonIgnore
  public boolean isEmpty() {
    return sector.isEmpty()
        && source.isEmpty()
        && level.isEmpty()
        && sdg_code.isEmpty()
        && crs_code.isEmpty();
  }
}
