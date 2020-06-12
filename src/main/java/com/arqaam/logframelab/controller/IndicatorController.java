package com.arqaam.logframelab.controller;

import com.arqaam.logframelab.controller.dto.FiltersDto;
import com.arqaam.logframelab.exception.WrongFileExtensionException;
import com.arqaam.logframelab.model.Error;
import com.arqaam.logframelab.model.IndicatorResponse;
import com.arqaam.logframelab.model.persistence.Indicator;
import com.arqaam.logframelab.service.IndicatorService;
import com.arqaam.logframelab.util.Logging;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLConnection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@CrossOrigin("*")
@RestController
@RequestMapping("indicator")
@Api(tags = "Indicator")
public class IndicatorController implements Logging {

    private static final String WORD_FILE_EXTENSION = ".docx";
    private static final String WORKSHEET_FILE_EXTENSION = ".xlsx";

    private static final String WORKSHEET_DEFAULT_FORMAT = "xlsx";
    private static final String WORD_FORMAT = "word";
    private static final String DFID_FORMAT = "dfid";

    @Autowired
    private IndicatorService indicatorService;

    @PostMapping(value = "upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "${IndicatorController.handleFileUpload.value}", nickname = "handleFileUpload", response = IndicatorResponse.class, responseContainer = "List")
    @ApiResponses({
            @ApiResponse(code = 200, message = "File was uploaded", response = IndicatorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 409, message = "Wrong file extension", response = Error.class),
            @ApiResponse(code = 500, message = "Failed to upload the file", response = Error.class)
    })
    public ResponseEntity<List<IndicatorResponse>> handleFileUpload(@RequestPart("file") MultipartFile file , @RequestPart("filter") FiltersDto filter) throws IOException {
        logger().info("Extract Indicators from Word File. File Name: {}", file.getOriginalFilename());
        if(!file.getOriginalFilename().matches("\\S+(\\.docx$|\\.doc$)")){
            logger().error("Failed to upload file since it had the wrong file extension. File Name: {}", file.getOriginalFilename());
           throw new WrongFileExtensionException();
        }
        return  ResponseEntity.ok().body(indicatorService.extractIndicatorsFromWordFile(file, filter));
    }

    @PostMapping(value = "download", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "${IndicatorController.downloadIndicators.value}", nickname = "downloadIndicators", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @ApiResponses({
            @ApiResponse(code = 200, message = "File was uploaded"),
            @ApiResponse(code = 404, message = "Template not found", response = Error.class),
            @ApiResponse(code = 409, message = "Failed to download indicators. It cannot be empty", response = Error.class),
            @ApiResponse(code = 500, message = "File failed to upload", response = Error.class)
    })
    public ResponseEntity<Resource> downloadIndicators(@RequestBody List<IndicatorResponse> indicators,
                                                       @RequestParam(value = "format", defaultValue = WORD_FILE_EXTENSION) String format) {

        logger().info("Downloading indicators. format {}, Indicators: {}", format, indicators);
        if(indicators.isEmpty()){
            String msg = "Failed to download indicators. It cannot be empty";
            logger().error(msg);
            throw new IllegalArgumentException(msg);
        }
        ByteArrayOutputStream outputStream;
        String extension = WORD_FILE_EXTENSION;
        switch (format) {
            case WORKSHEET_DEFAULT_FORMAT:
                outputStream = indicatorService.exportIndicatorsInWorksheet(indicators);
                extension = WORKSHEET_FILE_EXTENSION;
                break;
            case DFID_FORMAT:
                outputStream = indicatorService.exportIndicatorsDFIDFormat(indicators);
                extension = WORKSHEET_FILE_EXTENSION;
                break;
            case WORD_FORMAT:
            default:
                outputStream = indicatorService.exportIndicatorsInWordFile(indicators);
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

    @GetMapping("themes")
    @ApiOperation(value = "${IndicatorController.getThemes.value}", nickname = "getThemes", response = String.class, responseContainer = "List")
    @ApiResponses({
            @ApiResponse(code = 200, message = "themes was loaded"),
            @ApiResponse(code = 500, message = "failed to upload themes", response = Error.class)
    })
    public List<String> getThemes(){
        return indicatorService.getAllThemes();
    }

    @PostMapping(value = "import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "${IndicatorController.importIndicatorFile.value}", nickname = "importIndicatorFile", response = IndicatorResponse.class, responseContainer = "List")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Indicators were imported"),
            @ApiResponse(code = 409, message = "Wrong file extension", response = Error.class),
            @ApiResponse(code = 500, message = "Failed to import indicators", response = Error.class)
    })
    public ResponseEntity<List<Indicator>> importIndicatorFile(@RequestParam("file") MultipartFile file) {

        logger().info("Import Indicators from a worksheet File. File Name: {}", file.getOriginalFilename());
        if(!file.getOriginalFilename().endsWith(WORKSHEET_FILE_EXTENSION)){
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
    public ResponseEntity<List<IndicatorResponse>> getIndicators(@RequestParam(required = false) List<String> themes,
                                                                 @RequestParam(required = false) List<String> sources,
                                                                 @RequestParam(required = false) List<Long> levels,
                                                                 @RequestParam(required = false) List<String> sdgCodes,
                                                                 @RequestParam(required = false) List<String> crsCodes) {

        logger().info("Retrieving Indicators with themes: {}, sources: {}, levels: {}, sdgCodes: {}, crsCodes: {}",
                themes, sources, levels, sdgCodes, crsCodes);

        return ResponseEntity.ok(indicatorService.getIndicators(Optional.ofNullable(themes), Optional.ofNullable(sources),
                Optional.ofNullable(levels), Optional.ofNullable(sdgCodes), Optional.ofNullable(crsCodes))
                .stream().map(indicatorService::convertIndicatorToIndicatorResponse).collect(Collectors.toList()));
    }
}
