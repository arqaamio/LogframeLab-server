package com.arqaam.logframelab.service;

import com.arqaam.logframelab.controller.dto.IndicatorRequestDto;
import com.arqaam.logframelab.controller.dto.IndicatorsRequestDto;
import com.arqaam.logframelab.controller.dto.IndicatorsRequestDto.FilterRequestDto;
import com.arqaam.logframelab.controller.dto.IndicatorApprovalRequestDto;
import com.arqaam.logframelab.controller.dto.IndicatorApprovalRequestDto.Approval;
import com.arqaam.logframelab.exception.IndicatorNotFoundException;
import com.arqaam.logframelab.model.persistence.Indicator;
import com.arqaam.logframelab.repository.IndicatorRepository;
import com.arqaam.logframelab.repository.LevelRepository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.arqaam.logframelab.util.Logging;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class IndicatorsManagementServiceImpl implements IndicatorsManagementService, Logging {

  private final IndicatorRepository indicatorRepository;
  private final LevelRepository levelRepository;
  private final IndicatorService indicatorService;

  public IndicatorsManagementServiceImpl(IndicatorRepository indicatorRepository,
      LevelRepository levelRepository,
      IndicatorService indicatorService) {
    this.indicatorRepository = indicatorRepository;
    this.levelRepository = levelRepository;
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

    FilterRequestDto filters = indicatorsRequest.getFilters();

    Specification<Indicator> specification = indicatorService.specificationFromFilter(filters, false);

    return indicatorRepository.findAll(specification, page);
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
            .sector(indicatorRequest.getSector())
            .sourceVerification(indicatorRequest.getSourceVerification())
            .dataSource(indicatorRequest.getDataSource())
            .disaggregation(indicatorRequest.getDisaggregation())
            .build());
  }

  @Override
  public void deleteIndicator(Long id) {
    logger().info("Deleting indicator with id: {}", id);
    try {
      indicatorRepository.deleteById(id);
    } catch (EmptyResultDataAccessException e) {
      logger().error("Failed to delete indicator, because it was not found. id: {}", id);
      throw new IndicatorNotFoundException();
    }
  }

  @Override
  public void processFileWithTempIndicators(MultipartFile file) {
    List<Indicator> indicators = indicatorService.extractIndicatorFromFile(file);
    if (!indicators.isEmpty()) {
      saveForApproval(indicators);
    }
  }

  @Override
  public Page<Indicator> getIndicatorsForApproval(IndicatorsRequestDto indicatorsRequest) {
    PageRequest page = PageRequest
        .of(indicatorsRequest.getPage() - 1, indicatorsRequest.getPageSize());

    FilterRequestDto filters = indicatorsRequest.getFilters();

    Specification<Indicator> specification = indicatorService.specificationFromFilter(filters, true);

    return indicatorRepository.findAll(specification, page);
  }

  @Override
  @Transactional
  public void processTempIndicatorsApproval(IndicatorApprovalRequestDto approvalRequest) {
    List<Long> approvedIds = approvalRequest.getApprovals().stream()
        .filter(Approval::getIsApproved).map(Approval::getId)
        .collect(Collectors.toList());

    if (approvedIds.size() > 0) {
      indicatorRepository.updateToApproved(approvedIds);
    }

    List<Long> unapprovedIds = approvalRequest.getApprovals().stream()
        .filter(approval -> !approval.getIsApproved()).map(Approval::getId).collect(Collectors.toList());

    if (unapprovedIds.size() > 0) {
      indicatorRepository.deleteDisapprovedByIds(unapprovedIds);
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

  /**
   * Saves all the indicators that don't already exist in the database
   * @param indicators The indicators to be filtered and saved
   */
  private void saveForApproval(List<Indicator> indicators) {
    logger().info("Starting to check for duplicates");
    ExampleMatcher matcher = ExampleMatcher.matchingAll()
            .withIgnorePaths("id")
            .withIgnorePaths("temp")
            .withIgnoreCase();
    // Filter to remove duplicated indicators
    indicators = indicators.stream().filter(x -> !indicatorRepository.exists(Example.of(x, matcher)))
            .peek(indicator -> indicator.setTemp(true)).collect(Collectors.toList());

    // Get the ids of the indicators to be updated (they are meant to be updated if an indicator with the same name already exists)
    logger().info("Adding the ids to the indicators that are meant to be update");
    List<Indicator> toBeUpdated = indicatorRepository.findAllByNameIn(indicators.stream().map(Indicator::getName).collect(Collectors.toSet()));
    if(!toBeUpdated.isEmpty()) {
      indicators = indicators.stream().peek(indicator -> indicator.setId(toBeUpdated.stream()
              .filter(x -> x.getName().equals(indicator.getName())).findFirst().orElse(indicator).getId()))
              .collect(Collectors.toList());
    }
    logger().info("Saving the indicators to the database.");
    // saveAll saves and updates depending if the objects have id or not
    indicatorRepository.saveAll(indicators);
  }
}
