package com.arqaam.logframelab.controller;

import com.arqaam.logframelab.controller.dto.IndicatorApprovalRequestDto;
import com.arqaam.logframelab.controller.dto.IndicatorRequestDto;
import com.arqaam.logframelab.controller.dto.IndicatorsRequestDto;
import com.arqaam.logframelab.exception.IndicatorNotFoundException;
import com.arqaam.logframelab.model.Error;
import com.arqaam.logframelab.model.IndicatorResponse;
import com.arqaam.logframelab.model.persistence.Indicator;
import com.arqaam.logframelab.service.IndicatorsManagementService;
import io.swagger.annotations.*;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.Optional;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("indicators")
@Api(tags = "Indicators Management")
@PreAuthorize("hasAuthority('CRUD_INDICATOR')")
public class IndicatorsManagementController {

    private final IndicatorsManagementService indicatorsManagementService;

    public IndicatorsManagementController(IndicatorsManagementService indicatorsManagementService) {
    this.indicatorsManagementService = indicatorsManagementService;
  }

  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(value = "${IndicatorManagementController.getIndicators.value}", nickname = "getIndicators",
          response = Indicator.class, responseContainer = "Page", authorizations = { @Authorization(value="jwtToken") })
  @ApiResponses({
          @ApiResponse(code = 200, message = "Indicators were retrieved"),
          @ApiResponse(code = 500, message = "Failed to retrieve indicators", response = Error.class)
  })
  public ResponseEntity<Page<Indicator>> getIndicators(
      @Valid IndicatorsRequestDto indicatorsRequest) {
    return ResponseEntity.ok(indicatorsManagementService.getIndicators(indicatorsRequest));
  }

  @GetMapping(value = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(value = "${IndicatorManagementController.getIndicator.value}", nickname = "getIndicator",
          response = Indicator.class, responseContainer = "Page", authorizations = { @Authorization(value="jwtToken") })
  @ApiResponses({
          @ApiResponse(code = 200, message = "The indicator was retrieved"),
          @ApiResponse(code = 500, message = "Failed to import indicators", response = Error.class)
  })
  public ResponseEntity<Indicator> getIndicator(@PathVariable(value = "id") Long id) {
    Optional<Indicator> indicator = indicatorsManagementService.getIndicator(id);
    return indicator.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
  }

  @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(value = "${IndicatorManagementController.updateIndicator.value}", nickname = "updateIndicator",
          response = Indicator.class, authorizations = { @Authorization(value="jwtToken") })
  @ApiResponses({
          @ApiResponse(code = 200, message = "Indicator was updated"),
          @ApiResponse(code = 404, message = "Indicator was not found"),
          @ApiResponse(code = 500, message = "Failed to updated indicator", response = Error.class)
  })
  public ResponseEntity<Indicator> updateIndicator(@RequestBody IndicatorRequestDto request) {
    if (indicatorsManagementService.indicatorExists(request.getId())) {
      return ResponseEntity.ok(indicatorsManagementService.saveIndicator(request));
    } else {
      throw new IndicatorNotFoundException();
    }
  }

  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(value = "${IndicatorManagementController.createIndicator.value}", nickname = "createIndicator",
          response = Indicator.class, authorizations = { @Authorization(value="jwtToken") })
  @ApiResponses({
          @ApiResponse(code = 200, message = "Indicators was created"),
          @ApiResponse(code = 400, message = "New indicators must not have id"),
          @ApiResponse(code = 500, message = "Failed to create indicator", response = Error.class)
  })
  public ResponseEntity<Indicator> createIndicator(@RequestBody IndicatorRequestDto request) {
    if (request.getId() != null) {
      return ResponseEntity.badRequest().build();
    }
    return ResponseEntity.ok(indicatorsManagementService.saveIndicator(request));
  }

  @DeleteMapping(value = "{id}")
  @ApiOperation(value = "${IndicatorManagementController.deleteIndicator.value}", nickname = "deleteIndicator", authorizations = { @Authorization(value="jwtToken") })
  @ApiResponses({
          @ApiResponse(code = 200, message = "Indicator was deleted"),
          @ApiResponse(code = 404, message = "Indicator was not found", response = Error.class),
          @ApiResponse(code = 500, message = "Failed to delete indicator", response = Error.class)
  })
  public ResponseEntity<?> deleteIndicator(@PathVariable(value = "id") Long id) {
    indicatorsManagementService.deleteIndicator(id);
    return ResponseEntity.ok().build();
  }

  @PreAuthorize("isAuthenticated() || isAnonymous()")
  @PostMapping(value = "upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(value = "${IndicatorManagementController.uploadIndicatorFile.value}", nickname = "uploadIndicatorFile", authorizations = { @Authorization(value="jwtToken") })
  @ApiResponses({
          @ApiResponse(code = 200, message = "Uploads temporary indicators"),
          @ApiResponse(code = 500, message = "Failed to open worksheet", response = Error.class),
  })
  public ResponseEntity<?> uploadIndicatorFile(@RequestParam("file") MultipartFile file) {
    indicatorsManagementService.processFileWithTempIndicators(file);
    return ResponseEntity.ok().build();
  }

  @GetMapping(value = "approvals", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(value = "${IndicatorManagementController.getTempIndicatorsForApproval.value}",
          nickname = "getTempIndicatorsForApproval", authorizations = { @Authorization(value="jwtToken") })
  @ApiResponses({
          @ApiResponse(code = 200, message = "Indicators waiting for approval were retrieved", response = Indicator.class, responseContainer = "Page"),
  })
  public ResponseEntity<Page<Indicator>> getTempIndicatorsForApproval(
      IndicatorsRequestDto indicatorsRequest) {
    return ResponseEntity
        .ok(indicatorsManagementService.getIndicatorsForApproval(indicatorsRequest));
  }

  @PostMapping(value = "approvals", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(value = "${IndicatorManagementController.approvalStatusUpdate.value}", nickname = "approvalStatusUpdate", authorizations = { @Authorization(value="jwtToken") })
  @ApiResponses({
          @ApiResponse(code = 200, message = "The indicators were approved", response = IndicatorResponse.class, responseContainer = "List"),
  })
  public ResponseEntity<?> approvalStatusUpdate(@RequestBody IndicatorApprovalRequestDto approvalRequest) {
    indicatorsManagementService.processTempIndicatorsApproval(approvalRequest);
    return ResponseEntity.ok().build();
  }

}
