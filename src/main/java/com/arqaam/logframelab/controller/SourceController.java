package com.arqaam.logframelab.controller;

import com.arqaam.logframelab.model.Error;
import com.arqaam.logframelab.model.persistence.Source;
import com.arqaam.logframelab.model.persistence.Statistic;
import com.arqaam.logframelab.service.SourceService;
import com.arqaam.logframelab.service.StatisticService;
import com.arqaam.logframelab.util.Logging;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("source")
@Api(tags = "Source")
public class SourceController implements Logging {

    @Autowired
    private SourceService sourceService;

    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "${SourceController.retrieveAllSources.value}", nickname = "retrieveAllSources", response = Source.class, responseContainer = "List")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Sources were retrieved", response = Source.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unexpected error occurred", response = Error.class)
    })
    public ResponseEntity<List<Source>> retrieveAllSources() {
        logger().info("Retrieving all sources");
        return ResponseEntity.ok(sourceService.getSources());
    }

    @GetMapping(value = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "${SourceController.retrieveSourceById.value}", nickname = "retrieveSourceById", response = Source.class)
    @ApiResponses({
            @ApiResponse(code = 200, message = "Source was retrieved", response = Source.class),
            @ApiResponse(code = 404, message = "Source with id was not found", response = Error.class),
            @ApiResponse(code = 500, message = "Unexpected error occurred", response = Error.class)
    })
    public ResponseEntity<Source> retrieveSourceById(@PathVariable Long id) {
        logger().info("Retrieving source with id: {}", id);
        return ResponseEntity.ok(sourceService.getSourceById(id));
    }

    @PostMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "${SourceController.createSource.value}", nickname = "createSource", response = Source.class)
    @ApiResponses({
            @ApiResponse(code = 200, message = "Source was created", response = Source.class),
            @ApiResponse(code = 500, message = "Unexpected error occurred", response = Error.class)
    })
    public ResponseEntity<Source> createSource(@RequestBody Source source) {
        logger().info("Creating source with name: {}", source.getName());
        return ResponseEntity.ok(sourceService.createSource(source.getName()));
    }

    @PutMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "${SourceController.updateSource.value}", nickname = "updateSource", response = Source.class)
    @ApiResponses({
            @ApiResponse(code = 200, message = "Source was updated", response = Source.class),
            @ApiResponse(code = 404, message = "Source with id was not found", response = Error.class),
            @ApiResponse(code = 500, message = "Unexpected error occurred", response = Error.class)
    })
    public ResponseEntity<Source> updateSource(@RequestBody Source source) {
        logger().info("Updating source with id: {}, with name: {}", source.getId(), source.getName());
        return ResponseEntity.ok(sourceService.updateSource(source.getId(), source.getName()));
    }

    @DeleteMapping(value = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "${SourceController.deleteSource.value}", nickname = "deleteSource", response = Source.class)
    @ApiResponses({
            @ApiResponse(code = 200, message = "Source was deleted", response = Source.class),
            @ApiResponse(code = 404, message = "Source with id was not found", response = Error.class),
            @ApiResponse(code = 500, message = "Unexpected error occurred", response = Error.class)
    })
    public ResponseEntity<Source> deleteSource(@PathVariable Long id) {
        logger().info("Deleting source with id: {}", id);
        return ResponseEntity.ok(sourceService.deleteSourceById(id));
    }
}
