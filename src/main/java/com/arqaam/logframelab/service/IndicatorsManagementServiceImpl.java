package com.arqaam.logframelab.service;

import com.arqaam.logframelab.controller.dto.IndicatorRequestDto;
import com.arqaam.logframelab.controller.dto.IndicatorsRequestDto;
import com.arqaam.logframelab.controller.dto.TempIndicatorApprovalRequestDto;
import com.arqaam.logframelab.controller.dto.TempIndicatorApprovalRequestDto.Approval;
import com.arqaam.logframelab.model.persistence.Indicator;
import com.arqaam.logframelab.model.persistence.TempIndicator;
import com.arqaam.logframelab.repository.IndicatorRepository;
import com.arqaam.logframelab.repository.LevelRepository;
import com.arqaam.logframelab.repository.TempIndicatorRepository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class IndicatorsManagementServiceImpl implements IndicatorsManagementService {

  private final IndicatorRepository indicatorRepository;
  private final LevelRepository levelRepository;
  private final IndicatorMapper indicatorMapper;
  private final TempIndicatorRepository tempIndicatorRepository;
  private final IndicatorService indicatorService;

  public IndicatorsManagementServiceImpl(IndicatorRepository indicatorRepository,
      LevelRepository levelRepository, IndicatorMapper indicatorMapper,
      TempIndicatorRepository tempIndicatorRepository, IndicatorService indicatorService) {
    this.indicatorRepository = indicatorRepository;
    this.levelRepository = levelRepository;
    this.indicatorMapper = indicatorMapper;
    this.tempIndicatorRepository = tempIndicatorRepository;
    this.indicatorService = indicatorService;
  }

  @Override
  public Page<Indicator> getIndicators(IndicatorsRequestDto indicatorsRequest) {
    PageRequest page = PageRequest
        .of(indicatorsRequest.getPage() - 1, indicatorsRequest.getPageSize());

    if (indicatorsRequest.getSortBy() != null) {
      page = PageRequest.of(indicatorsRequest.getPage() - 1, indicatorsRequest.getPageSize(),
          Direction.fromString(indicatorsRequest.getSortBy().getDirection()),
          indicatorsRequest.getSortBy().getProperty());
    }

    return indicatorRepository.findAll(page);
  }

  @Override
  public Indicator saveIndicator(IndicatorRequestDto indicatorRequest) {
    return indicatorRepository.save(
        Indicator.builder().id(indicatorRequest.getId())
            .description(indicatorRequest.getDescription())
            .name(indicatorRequest.getName())
            .level(levelRepository.findById(indicatorRequest.getLevelId()).orElse(null))
            .keywords(indicatorRequest.getKeywords())
            .crsCode(indicatorRequest.getCrsCode())
            .sdgCode(indicatorRequest.getSdgCode())
            .source(indicatorRequest.getSource())
            .themes(indicatorRequest.getThemes())
            .sourceVerification(indicatorRequest.getSourceVerification())
            .dataSource(indicatorRequest.getDataSource())
            .disaggregation(indicatorRequest.getDisaggregation())
            .build());
  }

  @Override
  public void deleteIndicator(Long id) {
    indicatorRepository.deleteById(id);
  }

  @Override
  public void processFileWithTempIndicators(MultipartFile file) {
    List<Indicator> indicators = indicatorService.extractIndicatorFromFile(file);
    saveForApproval(indicators);
  }

  @Override
  public Page<TempIndicator> getIndicatorsForApproval(IndicatorsRequestDto indicatorsRequest) {
    PageRequest page = PageRequest
        .of(indicatorsRequest.getPage() - 1, indicatorsRequest.getPageSize());

    return tempIndicatorRepository.findAll(page);
  }

  @Override
  @Transactional
  public void processTempIndicatorsApproval(TempIndicatorApprovalRequestDto approvalRequest) {
    List<Approval> listOfApproved = approvalRequest.getApprovals().stream()
        .filter(Approval::getIsApproved)
        .collect(Collectors.toList());

    if (listOfApproved.size() > 0) {
      List<TempIndicator> approvedTempIndicators = tempIndicatorRepository
          .findAllById(listOfApproved.stream().map(Approval::getId).collect(Collectors.toList()));

      List<Indicator> indicators = approvedTempIndicators.stream()
          .map(indicatorMapper::tempIndicatorToIndicator)
          .collect(Collectors.toList());
      indicatorRepository.saveAll(indicators);

      tempIndicatorRepository.deleteAll(approvedTempIndicators);
    }

    List<Approval> listOfUnapproved = approvalRequest.getApprovals().stream()
        .filter(approval -> !approval.getIsApproved()).collect(Collectors.toList());

    if (listOfUnapproved.size() > 0) {
      tempIndicatorRepository.deleteByIdIn(
          listOfUnapproved.stream().map(Approval::getId).collect(Collectors.toList()));
    }
  }

  @Override
  public boolean indicatorExists(Long id) {
    return indicatorRepository.existsById(id);
  }

  @Override
  public Optional<Indicator> getIndicator(Long id) {
    return indicatorRepository.findById(id);
  }

  private void saveForApproval(List<Indicator> indicators) {
    List<TempIndicator> tempIndicators = indicators.stream()
        .map(indicatorMapper::indicatorToTempIndicator)
        .collect(Collectors.toList());

    tempIndicatorRepository.saveAll(tempIndicators);
  }
}
