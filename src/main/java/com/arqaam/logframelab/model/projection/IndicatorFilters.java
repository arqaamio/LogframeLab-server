package com.arqaam.logframelab.model.projection;

import com.arqaam.logframelab.model.persistence.CRSCode;
import com.arqaam.logframelab.model.persistence.Level;
import com.arqaam.logframelab.model.persistence.SDGCode;
import com.arqaam.logframelab.model.persistence.Source;

import java.util.Set;

public interface IndicatorFilters {

  String getSector();

  String getDescription();

  Set<Source> getSource();

  Level getLevel();

  Set<SDGCode> getSdgCode();

  Set<CRSCode> getCrsCode();
}
