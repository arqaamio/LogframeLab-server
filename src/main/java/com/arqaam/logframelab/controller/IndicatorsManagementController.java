package com.arqaam.logframelab.controller;

import com.arqaam.logframelab.controller.dto.IndicatorRequestDto;
import com.arqaam.logframelab.controller.dto.IndicatorsRequestDto;
import com.arqaam.logframelab.controller.dto.TempIndicatorApprovalRequestDto;
import com.arqaam.logframelab.model.persistence.Indicator;
import com.arqaam.logframelab.service.IndicatorsManagementService;
import io.swagger.annotations.Api;
import javax.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("indicators")
@Api(tags = "Indicator")
public class IndicatorsManagementController {

  private final IndicatorsManagementService indicatorsManagementService;

  public IndicatorsManagementController(IndicatorsManagementService indicatorsManagementService) {
    this.indicatorsManagementService = indicatorsManagementService;
  }

  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<?> getIndicators(@Valid IndicatorsRequestDto indicatorsRequest) {
    Page<Indicator> indicators = indicatorsManagementService.getIndicators(indicatorsRequest);
    return ResponseEntity.ok(indicators);
  }

  @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<?> updateIndicator(@RequestBody IndicatorRequestDto request) {
    return ResponseEntity.ok(indicatorsManagementService.saveIndicator(request));
  }

  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<?> createIndicator(@RequestBody IndicatorRequestDto request) {
    return ResponseEntity.ok(indicatorsManagementService.saveIndicator(request));
  }

  @DeleteMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<?> deleteIndicator(@PathVariable(value = "id") Long id) {
    indicatorsManagementService.deleteIndicator(id);
    return ResponseEntity.ok().build();
  }

  @PostMapping(value = "upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<?> uploadIndicatorFile(@RequestParam("file") MultipartFile file) {
    indicatorsManagementService.processFileWithTempIndicators(file);
    return ResponseEntity.ok().build();
  }

  @GetMapping(value = "approvals", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<?> getTempIndicatorsForApproval(IndicatorsRequestDto indicatorsRequest) {
    return ResponseEntity
        .ok(indicatorsManagementService.getIndicatorsForApproval(indicatorsRequest));
  }

  @PostMapping(value = "approvals", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<?> approvalStatusUpdate(TempIndicatorApprovalRequestDto approvalRequest) {
    indicatorsManagementService.processTempIndicatorsApproval(approvalRequest);
    return ResponseEntity.ok().build();
  }

}
