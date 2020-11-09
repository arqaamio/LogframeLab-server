package com.arqaam.logframelab.controller;

import com.arqaam.logframelab.model.Error;
import com.arqaam.logframelab.model.persistence.Statistic;
import com.arqaam.logframelab.service.StatisticService;
import com.arqaam.logframelab.util.Logging;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("statistic")
@Api(tags = "Statistic")
public class StatisticController implements Logging {

    @Autowired
    private StatisticService statisticService;

    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "${StatisticController.retrieveAllStatistics.value}", nickname = "retrieveAllStatistics", response = Statistic.class, responseContainer = "List")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Statistics were retrieved", response = Statistic.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unexpected error occurred", response = Error.class)
    })
    public ResponseEntity<List<Statistic>> retrieveAllStatistics() {
        logger().info("Retrieving all statistics");
        return ResponseEntity.ok(statisticService.getAllStatistics());
    }
}
