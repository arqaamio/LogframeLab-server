package com.arqaam.logframelab.model.projection;

import com.arqaam.logframelab.model.persistence.Level;

public interface IndicatorFilters {

  String getThemes();

  String getDescription();

  String getSource();

  Level getLevel();

  String getSdgCode();

  String getCrsCode();
}
