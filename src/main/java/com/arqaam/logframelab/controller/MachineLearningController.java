package com.arqaam.logframelab.controller;

import com.arqaam.logframelab.model.Error;
import com.arqaam.logframelab.model.SimilarityResponse;
import com.arqaam.logframelab.model.persistence.Indicator;
import com.arqaam.logframelab.service.IndicatorService;
import com.arqaam.logframelab.service.MachineLearningService;
import com.arqaam.logframelab.util.Logging;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@CrossOrigin("*")
@RestController
@RequestMapping("ml")
@Api(tags = "Machine Learning")
public class MachineLearningController implements Logging {

    @Autowired
    private IndicatorService indicatorService;

    @Autowired
    private MachineLearningService machineLearningService;

    @GetMapping(value = "similarity", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "${MachineLearningController.getSimilarIndicators.value}", nickname = "getSimilarIndicators",
            response = SimilarityResponse.class, responseContainer = "List")
    @ApiResponses({
            @ApiResponse(code = 200, message = "All similar indicators were retrieved", response = SimilarityResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unexpected error occurred", response = Error.class)
    })
    public ResponseEntity<List<SimilarityResponse>> getSimilarIndicators(@RequestParam Double threshold) {
        logger().info("Starting to retrieve similar indicators with threshold {}", threshold);
        List<SimilarityResponse> response = new ArrayList<>();
        List<Indicator> indicatorUnchecked = indicatorService.getIndicatorsWithSimilarity(false);
        List<String> indicatorNames;
        for(Indicator indicator : indicatorUnchecked) {
            indicatorNames = machineLearningService.getSimilarIndicators(indicator.getName(), threshold);
            if(!indicatorNames.isEmpty()){
                response.add(new SimilarityResponse(indicator, indicatorService.getIndicatorWithName(indicatorNames)));
            }
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "indicators", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "${MachineLearningController.scanForIndicators.value}", nickname = "scanForIndicators",
            response = Indicator.class, responseContainer = "List")
    @ApiResponses({
            @ApiResponse(code = 200, message = "The text was scanned for indicators", response = Indicator.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unexpected error occurred", response = Error.class)
    })
    public ResponseEntity<Map<String, Double>> scanForIndicators(@RequestBody String text) {
        logger().info("Starting the scan for indicators of text");
        return ResponseEntity.ok(machineLearningService.scanForIndicators(text));
    }
}
