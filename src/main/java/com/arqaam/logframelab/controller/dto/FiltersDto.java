package com.arqaam.logframelab.controller.dto;

import com.arqaam.logframelab.model.persistence.Level;
import lombok.Getter;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
public class FiltersDto {

  private final Set<String> themes = new LinkedHashSet<>();
  private final Set<String> descriptions = new LinkedHashSet<>();
  private final Set<String> source = new LinkedHashSet<>();
  private final Set<Level> level = new LinkedHashSet<>();
  private final Set<String> sdg_code = new LinkedHashSet<>();
  private final Set<String> crs_code = new LinkedHashSet<>();
}
