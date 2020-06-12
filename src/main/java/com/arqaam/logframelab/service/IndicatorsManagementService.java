package com.arqaam.logframelab.service;

import com.arqaam.logframelab.controller.dto.IndicatorRequestDto;
import com.arqaam.logframelab.controller.dto.IndicatorsRequestDto;
import com.arqaam.logframelab.controller.dto.TempIndicatorApprovalRequestDto;
import com.arqaam.logframelab.model.persistence.Indicator;
import com.arqaam.logframelab.model.persistence.TempIndicator;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

public interface IndicatorsManagementService {

  Page<Indicator> getIndicators(IndicatorsRequestDto indicatorsRequest);

  Indicator saveIndicator(IndicatorRequestDto createIndicatorRequest);

  void deleteIndicator(Long id);

  void processFileWithTempIndicators(MultipartFile file);

  Page<TempIndicator> getIndicatorsForApproval(IndicatorsRequestDto indicatorsRequest);

  void processTempIndicatorsApproval(TempIndicatorApprovalRequestDto approvalRequest);

  boolean indicatorExists(Long id);

  Optional<Indicator> getIndicator(Long id);
}
