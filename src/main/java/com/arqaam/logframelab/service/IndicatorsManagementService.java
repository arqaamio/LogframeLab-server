package com.arqaam.logframelab.service;

import com.arqaam.logframelab.controller.dto.IndicatorRequestDto;
import com.arqaam.logframelab.controller.dto.IndicatorsRequestDto;
import com.arqaam.logframelab.controller.dto.IndicatorApprovalRequestDto;
import com.arqaam.logframelab.model.persistence.Indicator;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

public interface IndicatorsManagementService {

  Page<Indicator> getIndicators(IndicatorsRequestDto indicatorsRequest);

  Indicator saveIndicator(IndicatorRequestDto createIndicatorRequest);

  void deleteIndicator(Long id);

  void processFileWithTempIndicators(MultipartFile file);

  Page<Indicator> getIndicatorsForApproval(IndicatorsRequestDto indicatorsRequest);

  void processTempIndicatorsApproval(IndicatorApprovalRequestDto approvalRequest);

  boolean indicatorExists(Long id);

  Optional<Indicator> getIndicator(Long id);
}
