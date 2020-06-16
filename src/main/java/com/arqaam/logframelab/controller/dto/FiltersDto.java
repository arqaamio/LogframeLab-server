package com.arqaam.logframelab.controller.dto;

import com.arqaam.logframelab.model.persistence.Level;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.LinkedHashSet;

@NoArgsConstructor
public class FiltersDto {

  @Getter
  private final Collection<String> themes = new LinkedHashSet<>();

  @Getter
  private final Collection<String> source = new LinkedHashSet<>();

  @Getter
  private final Collection<Level> level = new LinkedHashSet<>();

  private final Collection<String> sdg_code = new LinkedHashSet<>();
  private final Collection<String> crs_code = new LinkedHashSet<>();

  public Collection<String> getSdgCode() {
    return sdg_code;
  }

  public Collection<String> getCrsCode() {
    return crs_code;
  }

  @JsonIgnore
  public boolean isEmpty() {
    return themes.isEmpty()
        && source.isEmpty()
        && level.isEmpty()
        && sdg_code.isEmpty()
        && crs_code.isEmpty();
  }
}
