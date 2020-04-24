package com.arqaam.logframelab.controller;

import com.arqaam.logframelab.model.IndicatorResponse;
import com.arqaam.logframelab.service.IndicatorService;
import com.arqaam.logframelab.util.Logging;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@CrossOrigin(origins = "*")
@RestController
@Api(tags = "Indicator")
public class IndicatorController implements Logging {

    @Autowired
    private IndicatorService indicatorService;

    @ResponseStatus(HttpStatus.OK)
    @PostMapping(value = "/indicator/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "${IndicatorController.handleFileUpload.value}", nickname = "handleFileUpload", response = IndicatorResponse.class, responseContainer = "List")
    @ApiResponses({
            @ApiResponse(code = 200, message = "File was uploaded", response = IndicatorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Failed to upload the file", response = Error.class)
    })
    public List<IndicatorResponse> handleFileUpload(@RequestParam("file") MultipartFile file) throws IOException {

        logger().info("Extract Indicators Form Word File .... ");
        Path tmpFilePath = Paths.get(System.getProperty("user.home")).resolve("tmp" + UUID.randomUUID()+".docx");
        Files.copy(file.getInputStream(), tmpFilePath);
        return indicatorService.extractIndicatorsFromWordFile(tmpFilePath);
    }

    @PostMapping(value = "/indicator/download", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @ApiOperation(value = "${IndicatorController.downloadIndicators.value}", nickname = "handleFileUpload")
    @ApiResponses({
            @ApiResponse(code = 200, message = "File was uploaded"),
            @ApiResponse(code = 500, message = "File failed to upload", response = Error.class)
    })
    public void downloadIndicators(HttpServletRequest request, HttpServletResponse response, @RequestBody List<IndicatorResponse> indicators) throws IOException {
        ByteArrayOutputStream outputStream = indicatorService.exportIndicatorsInWordFile(indicators);
        if (outputStream != null) {
            //get the mimetype
            String mimeType = URLConnection.guessContentTypeFromName("indicators_export.docx");
            if (mimeType == null) {
                //unknown mimetype so set the mimetype to application/octet-stream
                mimeType = "application/octet-stream";
              //  mimeType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            }
            response.setContentType(mimeType);
            response.setHeader("filename", "indicators_export.docx");
            response.setHeader("Access-Control-Expose-Headers", "*");
            response.setHeader("Content-Disposition", String.format("inline; filename=\"indicators_export.docx\""));
            response.setContentLength(outputStream.size());
            InputStream inputStream = new BufferedInputStream(new ByteArrayInputStream(outputStream.toByteArray()));
            FileCopyUtils.copy(inputStream, response.getOutputStream());
        }
    }
}