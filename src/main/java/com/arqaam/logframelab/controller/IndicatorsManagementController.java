package com.arqaam.logframelab.controller;

import com.arqaam.logframelab.controller.dto.IndicatorRequestDto;
import com.arqaam.logframelab.controller.dto.IndicatorsRequestDto;
import com.arqaam.logframelab.model.persistence.Indicator;
import com.arqaam.logframelab.service.IndicatorsManagementService;
import io.swagger.annotations.Api;
import javax.validation.Valid;
import org.hibernate.cfg.NotYetImplementedException;
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
import org.springframework.web.bind.annotation.RestController;

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
}
