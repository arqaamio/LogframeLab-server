package com.arqaam.logframelab.controller.dto;

import com.arqaam.logframelab.model.persistence.Level;
import lombok.Data;

import java.util.LinkedHashSet;
import java.util.Set;

@Data
public class FiltersDto {

  private final Set<String> themes = new LinkedHashSet<>();
  private final Set<String> source = new LinkedHashSet<>();
  private final Set<Level> level = new LinkedHashSet<>();
  private final Set<String> sdg_code = new LinkedHashSet<>();
  private final Set<String> crs_code = new LinkedHashSet<>();

  public Set<String> getSdgCode() {
    return sdg_code;
  }

  public Set<String> getCrsCode() {
    return crs_code;
  }
}
