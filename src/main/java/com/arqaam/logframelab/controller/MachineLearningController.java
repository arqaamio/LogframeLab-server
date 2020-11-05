package com.arqaam.logframelab.controller;

import com.arqaam.logframelab.model.Error;
import com.arqaam.logframelab.model.MLStatementQualityRequest;
import com.arqaam.logframelab.model.MLStatementResponse;
import com.arqaam.logframelab.model.SimilarityResponse;
import com.arqaam.logframelab.model.MLScanIndicatorResponse.MLScanIndicator;
import com.arqaam.logframelab.model.MLStatementResponse.MLStatement;
import com.arqaam.logframelab.model.persistence.Indicator;
import com.arqaam.logframelab.service.IndicatorService;
import com.arqaam.logframelab.service.MachineLearningService;
import com.arqaam.logframelab.util.Logging;
import com.arqaam.logframelab.util.Utils;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
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

    @Autowired
    private Utils utils;

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
    @ApiOperation(value = "${MachineLearningController.scanForIndicators.value}", nickname = "scanForIndicators", response = Indicator.class, responseContainer = "List")
    @ApiResponses({
            @ApiResponse(code = 200, message = "The text was scanned for indicators", response = Indicator.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unexpected error occurred", response = Error.class)
    })
    public ResponseEntity<List<Indicator>> scanForIndicators(@RequestPart("file") MultipartFile file) {
        logger().info("Starting the scan for indicators in the text");
        String text = utils.retrieveTextFromDocument(file);
        logger().info("Text was retrieved from the document");
        List<MLScanIndicator> mlIndicators = machineLearningService.scanForIndicators(text, null);
        logger().info("Retrieved the indicators and its score found in the text");
        List<Indicator> indicatorList = indicatorService.getIndicatorWithId(mlIndicators.stream().map(MLScanIndicator::getId).collect(Collectors.toList()));
        for (int i = 0; i < indicatorList.size(); i++) {
            int finalI = i;
            Optional<MLScanIndicator> scanIndicator = mlIndicators.stream().filter(x->x.getId().equals(indicatorList.get(finalI).getId())).findFirst();
            if(scanIndicator.isPresent())
                indicatorList.get(i).setScore((int)Math.round(scanIndicator.get().getSearchResult().getSimilarity()));
        }
        return ResponseEntity.ok(indicatorList);
    }

    @PostMapping(value = "statements", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "${MachineLearningController.scanForStatements.value}", nickname = "scanForStatements", response = MLStatementResponse.class)
    @ApiResponses({
            @ApiResponse(code = 200, message = "The text was scanned for statements", response = MLStatementResponse.class),
            @ApiResponse(code = 500, message = "Unexpected error occurred", response = Error.class)
    })
    public ResponseEntity<MLStatementResponse> scanForStatements(@RequestPart("file") MultipartFile file) {
        logger().info("Starting the scan for statements by retrieving the text from the document");
        String text = utils.retrieveTextFromDocument(file);
        logger().info("Text was retrieved from the document");
        return ResponseEntity.ok(machineLearningService.scanForStatements(text));
    }

    @PostMapping(value = "statement-quality", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "${MachineLearningController.statementQualityCheck.value}", nickname = "statementQualityCheck", response = MLStatement.class)
    @ApiResponses({
            @ApiResponse(code = 200, message = "The statement was checked for quality", response = MLStatement.class),
            @ApiResponse(code = 500, message = "Unexpected error occurred", response = Error.class)
    })
    public ResponseEntity<MLStatement> statementQualityCheck(@RequestBody MLStatementQualityRequest statementQualityRequest) {
        logger().info("Starting to check quality of statement: {} with level: {}",
            statementQualityRequest.getStatement(), statementQualityRequest.getLevel());
        return ResponseEntity.ok(machineLearningService.qualityCheckStatement(statementQualityRequest));
    }
}
