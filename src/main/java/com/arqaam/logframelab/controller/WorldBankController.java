package com.arqaam.logframelab.controller;

import com.arqaam.logframelab.model.Error;
import com.arqaam.logframelab.model.WorldBankIndicator;
import com.arqaam.logframelab.service.IndicatorService;
import com.arqaam.logframelab.service.WorldBankService;
import com.arqaam.logframelab.util.Logging;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("worldbank")
@Api(tags = "World Bank Data")
public class WorldBankController implements Logging {

    @Autowired
    private IndicatorService indicatorService;

    @Autowired
    private WorldBankService worldBankService;

    @GetMapping(value = "country", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "${WorldBankController.retrieveCountries.value}", nickname = "getCountry", response = HashMap.class)
    @ApiResponses({
            @ApiResponse(code = 200, message = "Countries were retrieved successfully", response = HashMap.class),
            @ApiResponse(code = 500, message = "Unexpected error occurred", response = Error.class)
    })
    public ResponseEntity<Map<String, String>> retrieveCountries() {
        logger().info("Retrieving World Bank available countries");
        return ResponseEntity.ok(worldBankService.getCountries());
    }

    @GetMapping(value = "values", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "${WorldBankController.retrieveIndicatorValues.value}", nickname = "retrieveIndicatorValues", response = WorldBankIndicator.class, responseContainer = "List")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Values for the indicator were retrieved successfully", response = WorldBankIndicator.class, responseContainer = "List"),
            @ApiResponse(code = 422, message = "Data source not in correct format to extract indicator Id", response = Error.class),
            @ApiResponse(code = 500, message = "Unexpected error occurred", response = Error.class)
    })
    public ResponseEntity<List<WorldBankIndicator>> retrieveIndicatorValues(@RequestParam Long indicatorId,
                                                                            @RequestParam String countryId,
                                                                            @RequestParam(required = false) List<Integer> years) {

        logger().info("Retrieving world bank data with indicatorId: {}, countryId: {}, years: {}", indicatorId, countryId, years);
        return ResponseEntity.ok(worldBankService.getIndicatorValues(indicatorService.getIndicator(indicatorId), countryId, years));
    }


}
