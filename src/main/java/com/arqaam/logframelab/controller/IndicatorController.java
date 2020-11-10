package com.arqaam.logframelab.controller;

import com.arqaam.logframelab.controller.dto.FiltersDto;
import com.arqaam.logframelab.exception.TemplateNotFoundException;
import com.arqaam.logframelab.exception.WrongFileExtensionException;
import com.arqaam.logframelab.model.Error;
import com.arqaam.logframelab.model.IndicatorResponse;
import com.arqaam.logframelab.model.NumIndicatorsSectorLevel;
import com.arqaam.logframelab.model.persistence.Indicator;
import com.arqaam.logframelab.model.projection.CounterSectorLevel;
import com.arqaam.logframelab.service.IndicatorService;
import com.arqaam.logframelab.service.MachineLearningService;
import com.arqaam.logframelab.service.StatisticService;
import com.arqaam.logframelab.util.Constants;
import com.arqaam.logframelab.util.Logging;
import com.arqaam.logframelab.util.Utils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLConnection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@CrossOrigin("*")
@RestController
@RequestMapping("indicator")
@Api(tags = "Indicator")
public class IndicatorController implements Logging {

    private final IndicatorService indicatorService;
    private final MachineLearningService machineLearningService;
    private final StatisticService statisticService;
    private final Utils utils;

    public IndicatorController(IndicatorService indicatorService, MachineLearningService machineLearningService,
                               StatisticService statisticService, Utils utils) {
        this.indicatorService = indicatorService;
        this.machineLearningService = machineLearningService;
        this.statisticService = statisticService;
        this.utils = utils;
    }

    @PostMapping(value = "upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "${IndicatorController.handleFileUpload.value}", nickname = "handleFileUpload", response = IndicatorResponse.class, responseContainer = "List")
    @ApiResponses({
            @ApiResponse(code = 200, message = "File was uploaded", response = IndicatorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 409, message = "Wrong file extension", response = Error.class),
            @ApiResponse(code = 500, message = "Failed to upload the file", response = Error.class)
    })
    public ResponseEntity<List<IndicatorResponse>> handleFileUpload(@RequestPart("file") MultipartFile file , @RequestPart("filter") FiltersDto filter) {
        logger().info("Extract Indicators from Word File. File Name: {}", file.getOriginalFilename());
        if(!file.getOriginalFilename().toLowerCase().matches(".+(\\.docx$|\\.doc$)")){
            logger().error("Failed to upload file since it had the wrong file extension. File Name: {}", file.getOriginalFilename());
           throw new WrongFileExtensionException();
        }

        List<IndicatorResponse> response  = this.indicatorService.scanForIndicators(utils.retrieveTextFromDocument(file), filter);

        return  ResponseEntity.ok().body(response);
    }

    @PostMapping(value = "download", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "${IndicatorController.downloadIndicators.value}", nickname = "downloadIndicators", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @ApiResponses({
            @ApiResponse(code = 200, message = "Indicators were downloaded"),
            @ApiResponse(code = 404, message = "Template not found", response = Error.class),
            @ApiResponse(code = 409, message = "Failed to download indicators. It cannot be empty", response = Error.class),
            @ApiResponse(code = 500, message = "Unexpected Error", response = Error.class)
    })
    public ResponseEntity<Resource> downloadIndicators(@RequestBody List<IndicatorResponse> indicators,
                                                       @RequestParam(value = "format", defaultValue = Constants.WORD_FILE_EXTENSION) String format) {

        logger().info("Downloading indicators. format {}, Indicators: {}", format, indicators);
        if(indicators.isEmpty()){
            String msg = "Failed to download indicators. It cannot be empty";
            logger().error(msg);
            throw new IllegalArgumentException(msg);
        }
        ByteArrayOutputStream outputStream;
        String extension = Constants.WORD_FILE_EXTENSION;
        switch (format.toUpperCase()) {
            case Constants.XLSX_FORMAT:
                outputStream = indicatorService.exportIndicatorsInWorksheet(indicators);
                extension = Constants.WORKSHEET_FILE_EXTENSION;
                statisticService.addDownloadStatistic(Constants.XLSX_FORMAT);
                break;
            case Constants.DFID_FORMAT:
                outputStream = indicatorService.exportIndicatorsDFIDFormat(indicators);
                extension = Constants.WORKSHEET_FILE_EXTENSION;
                statisticService.addDownloadStatistic(Constants.DFID_FORMAT);
                break;
            case Constants.PRM_FORMAT:
                outputStream = indicatorService.exportIndicatorsPRMFormat(indicators);
                statisticService.addDownloadStatistic(Constants.PRM_FORMAT);
                break;
            case Constants.WORD_FORMAT:
            default:
                outputStream = indicatorService.exportIndicatorsInWordFile(indicators);
                statisticService.addDownloadStatistic(Constants.WORD_FORMAT);
                break;
        }

        //get the mimetype
        String mimeType = URLConnection.guessContentTypeFromName("indicators_export" + extension);
        if (mimeType == null) {
            //unknown mimetype so set the mimetype to application/octet-stream
            mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
            //  mimeType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        }
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("filename", "indicators_export" + extension);
        httpHeaders.set("Access-Control-Expose-Headers", "*");
        httpHeaders.set("Content-Disposition", "inline; filename=\"indicators_export" +extension +"\"");

        return ResponseEntity.ok().headers(httpHeaders).contentLength(outputStream.size())
                .contentType(MediaType.parseMediaType(mimeType))
                .body(new ByteArrayResource(outputStream.toByteArray()));
    }
/* This remains here in case the changes that were done end up not working. After tested, this should be removed.
//    public void downloadIndicators(HttpServletRequest request, HttpServletResponse response, @RequestBody List<IndicatorResponse> indicators) throws IOException {
//        ByteArrayOutputStream outputStream = indicatorService.exportIndicatorsInWordFile(indicators);
//        if (outputStream != null) {
//            //get the mimetype
//            String mimeType = URLConnection.guessContentTypeFromName("indicators_export.docx");
//            if (mimeType == null) {
//                //unknown mimetype so set the mimetype to application/octet-stream
//                mimeType = "application/octet-stream";
//              //  mimeType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
//            }
//            response.setContentType(mimeType);
//            response.setHeader("filename", "indicators_export.docx");
//            response.setHeader("Access-Control-Expose-Headers", "*");
//            response.setHeader("Content-Disposition", String.format("inline; filename=\"indicators_export.docx\""));
//            response.setContentLength(outputStream.size());
//            InputStream inputStream = new BufferedInputStream(new ByteArrayInputStream(outputStream.toByteArray()));
//            FileCopyUtils.copy(inputStream, response.getOutputStream());
//        }
    }
*/

    @PostMapping(value = "import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "${IndicatorController.importIndicatorFile.value}", nickname = "importIndicatorFile", response = IndicatorResponse.class, responseContainer = "List")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Indicators were imported"),
            @ApiResponse(code = 409, message = "Wrong file extension", response = Error.class),
            @ApiResponse(code = 500, message = "Failed to import indicators", response = Error.class)
    })
    public ResponseEntity<List<Indicator>> importIndicatorFile(@RequestParam("file") MultipartFile file) {

        logger().info("Import Indicators from a worksheet File. File Name: {}", file.getOriginalFilename());
        if(!file.getOriginalFilename().endsWith(Constants.WORKSHEET_FILE_EXTENSION)){
            logger().error("Failed to upload file since it had the wrong file extension. File Name: {}", file.getOriginalFilename());
            throw new WrongFileExtensionException();
        }
        return ResponseEntity.ok(indicatorService.importIndicators(file));
    }

    @GetMapping("filters")
    public ResponseEntity<FiltersDto> getFilters() {
        return ResponseEntity.ok(indicatorService.getFilters());
    }

    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "${IndicatorController.getIndicators.value}", nickname = "getIndicators", response = IndicatorResponse.class, responseContainer = "List")
    @ApiResponses({
            @ApiResponse(code = 200, message = "File was uploaded", response = IndicatorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unexpected error occurred", response = Error.class)
    })
    public ResponseEntity<List<IndicatorResponse>> getIndicators(@RequestParam(required = false) List<String> sectors,
                                                                 @RequestParam(required = false) List<Long> sources,
                                                                 @RequestParam(required = false) List<Long> levels,
                                                                 @RequestParam(required = false) List<Long> sdgCodes,
                                                                 @RequestParam(required = false) List<Long> crsCodes,
                                                                 @RequestParam(required = false) String name) {

        logger().info("Retrieving Indicators with sectors: {}, sources: {}, levels: {}, sdgCodes: {}, crsCodes: {}, name: {}",
                sectors, sources, levels, sdgCodes, crsCodes, name);

        return ResponseEntity.ok(indicatorService.getIndicators(Optional.ofNullable(sectors), Optional.ofNullable(sources),
                Optional.ofNullable(levels), Optional.ofNullable(sdgCodes), Optional.ofNullable(crsCodes), name)
                .stream()
                .sorted(Comparator.comparing(Indicator::getLevel))
                .map(indicatorService::convertIndicatorToIndicatorResponse).collect(Collectors.toList()));
    }

    @GetMapping(value = "template/{name}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "${IndicatorController.getTemplate.value}", nickname = "getTemplate", response = Resource.class)
    @ApiResponses({
            @ApiResponse(code = 200, message = "Template was downloaded", response = Resource.class),
            @ApiResponse(code = 500, message = "Unexpected error occurred", response = Error.class)
    })
    public ResponseEntity<Resource> getTemplate(@PathVariable(value = "name") String name) {

        logger().info("Retrieving template with name {}", name);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        String extension = Constants.WORD_FILE_EXTENSION;
        name = name.toUpperCase();

        switch (name) {
            case Constants.XLSX_FORMAT:
            case Constants.DFID_FORMAT:
                extension = Constants.WORKSHEET_FILE_EXTENSION;
                break;
        }

        try {
            outputStream.write(new ClassPathResource(name+"_Template"+ extension).getInputStream().readAllBytes());
        } catch (IOException e) {
            logger().error("Failed to retrieve template since it was not found with name: {} and extension: {}", name, extension);
            throw new TemplateNotFoundException();
        }

        String fileName = "logframe_template_"+name+extension;
        //get the mimetype
        String mimeType = URLConnection.guessContentTypeFromName(fileName);
        if (mimeType == null) {
            //unknown mimetype so set the mimetype to application/octet-stream
            mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
            //  mimeType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        }
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("filename", fileName);
        httpHeaders.set("Access-Control-Expose-Headers", "*");
        httpHeaders.set("Content-Disposition", "inline; filename=\""+ fileName +"\"");

        return ResponseEntity.ok().headers(httpHeaders).contentLength(outputStream.size())
                .contentType(MediaType.parseMediaType(mimeType))
                .body(new ByteArrayResource(outputStream.toByteArray()));
    }

    @GetMapping("total-number")
    @ApiOperation(value = "${IndicatorController.getTotalNumIndicators.value}", nickname = "getTotalNumIndicators", response = Long.class)
    @ApiResponses({
            @ApiResponse(code = 200, message = "Retrieved total number of indicators", response = Long.class),
            @ApiResponse(code = 500, message = "Unexpected error occurred", response = Error.class)
    })
    public ResponseEntity<Long> getTotalNumIndicators() {
        logger().info("Retrieving the total number of indicators");
        return ResponseEntity.ok(indicatorService.getTotalNumIndicators());
    }

    @GetMapping("sector-level-count")
    @ApiOperation(value = "${IndicatorController.getIndicatorsByLevelAndSector.value}", nickname = "getIndicatorsByLevelAndSector", response = NumIndicatorsSectorLevel.class, responseContainer = "List")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Retrieved count of indicators by level and sector", response = NumIndicatorsSectorLevel.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unexpected error occurred", response = Error.class)
    })
    public ResponseEntity<List<NumIndicatorsSectorLevel>> getIndicatorsByLevelAndSector() {
        logger().info("Retrieving the count of indicators per level and sector");
        return ResponseEntity.ok(indicatorService.getIndicatorsByLevelAndSector());
    }
}
