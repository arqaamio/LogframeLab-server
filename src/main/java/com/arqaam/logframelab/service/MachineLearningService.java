package com.arqaam.logframelab.service;

import com.arqaam.logframelab.controller.dto.FiltersDto;
import com.arqaam.logframelab.exception.MLAPIRequestFailedException;
import com.arqaam.logframelab.model.MLScanIndicatorRequest;
import com.arqaam.logframelab.model.MLScanIndicatorResponse;
import com.arqaam.logframelab.model.MLSimilarIndicatorRequest;
import com.arqaam.logframelab.model.MLSimilarIndicatorResponse;
import com.arqaam.logframelab.model.MLStatementQualityRequest;
import com.arqaam.logframelab.model.MLStatementRequest;
import com.arqaam.logframelab.model.MLStatementResponse;
import com.arqaam.logframelab.model.MLScanIndicatorResponse.MLScanIndicator;
import com.arqaam.logframelab.model.MLStatementResponse.MLStatement;
import com.arqaam.logframelab.model.persistence.CRSCode;
import com.arqaam.logframelab.model.persistence.Level;
import com.arqaam.logframelab.model.persistence.SDGCode;
import com.arqaam.logframelab.model.persistence.Source;
import com.arqaam.logframelab.util.Logging;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MachineLearningService implements Logging {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${logframelab.machine-learning-url}")
    private String URL;

    /**
     * Returns list of names of the indicators that are similar to the one sent.
     * @param name Name of the indicator
     * @param threshold Threshold of similarity
     * @return List of names of indicators that are similar
     */
    public List<String> getSimilarIndicators(String name, Double threshold) {
        logger().info("Started to retrieve similar indicators to: {} with threshold: {}", name, threshold);
        MLSimilarIndicatorRequest body = new MLSimilarIndicatorRequest(name, threshold);
        try {
            ResponseEntity<List<MLSimilarIndicatorResponse>> responseEntity = restTemplate.exchange(URL+"indicators/similarity-check",
                    HttpMethod.POST, new HttpEntity<>(body), new ParameterizedTypeReference<List<MLSimilarIndicatorResponse>>() {});
            if(responseEntity.getStatusCode()!= HttpStatus.OK || responseEntity.getBody() == null){
                throw new HttpClientErrorException(responseEntity.getStatusCode());
            }
            // Remove its own indicator and map
            return responseEntity.getBody().stream().filter(x->x.getSimilarity()<0.9999).map(MLSimilarIndicatorResponse::getIndicator).collect(Collectors.toList());
        } catch(HttpClientErrorException | HttpServerErrorException e) {
            logger().error("Failed to retrieve similar indicators from the Machine Learning API", e);
            throw new MLAPIRequestFailedException();
        }
    }

    /**
     * Calls the Machine Learning endpoint to get indicators found in the text
     * @param text Text to be sent to the Machine Learning endpoint
     * @return List of Indicators with score of best fit
     */
    public List<MLScanIndicator> scanForIndicators(String text, @Nullable FiltersDto filtersDto) {
        logger().info("Started to scan for indicators");
        MLScanIndicatorRequest body = new MLScanIndicatorRequest(Collections.singletonList(text), "doc");
        try {
            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(URL+"indicators");
            if(filtersDto != null){
                if(!filtersDto.getLevel().isEmpty())
                    builder = builder.queryParam("level", filtersDto.getLevel().stream().map(Level::getId).collect(Collectors.toList()));
                if(!filtersDto.getSector().isEmpty())
                    builder = builder.queryParam("sector", filtersDto.getSector());
                if(!filtersDto.getSdgCode().isEmpty())
                    builder = builder.queryParam("sdgCode", filtersDto.getSdgCode().stream().map(SDGCode::getId).collect(Collectors.toList()));
                if(!filtersDto.getCrsCode().isEmpty())
                    builder = builder.queryParam("crsCode", filtersDto.getCrsCode().stream().map(CRSCode::getId).collect(Collectors.toList()));
                if(!filtersDto.getSource().isEmpty())
                    builder = builder.queryParam("source", filtersDto.getSource().stream().map(Source::getId).collect(Collectors.toList()));
            }
            logger().info("Request URI: {}", builder.toUriString());
            ResponseEntity<MLScanIndicatorResponse> responseEntity = restTemplate.exchange(builder.toUriString(),
                    HttpMethod.POST, new HttpEntity<>(body, new HttpHeaders()), MLScanIndicatorResponse.class);
            logger().info("Response: {}", responseEntity.getBody());
            if(responseEntity.getStatusCode()!= HttpStatus.OK || responseEntity.getBody() == null){
                throw new HttpClientErrorException(responseEntity.getStatusCode());
            }
            return responseEntity.getBody().getIndicators();
        } catch(HttpClientErrorException | HttpServerErrorException e) {
            logger().error("Failed to scan the text for indicators from the Machine Learning API", e);
            throw new MLAPIRequestFailedException();
        }
    }

    /**
     * Calls the Machine Learning endpoint to get statements found in the text
     * @param text Text to be sent to the Machine Learning endpoint
     * @return Statements found in the text with its quality and divided by level
     */
    public MLStatementResponse scanForStatements(String text) {
        logger().info("Started to scan for statements");
        MLStatementRequest body = new MLStatementRequest(Collections.singletonList(text), "doc");
        try {
            ResponseEntity<MLStatementResponse> responseEntity = restTemplate.exchange(URL+"statement-detector",
                HttpMethod.POST, new HttpEntity<>(body, new HttpHeaders()), MLStatementResponse.class);

            if(responseEntity.getStatusCode()!= HttpStatus.OK || responseEntity.getBody() == null){
                throw new HttpClientErrorException(responseEntity.getStatusCode());
            }
            return responseEntity.getBody();
        } catch(HttpClientErrorException | HttpServerErrorException e) {
            logger().error("Failed to scan the text for statements from the Machine Learning API", e);
            throw new MLAPIRequestFailedException();
        }
    }

    /**
     * Calls the Machine Learning endpoint to verify the quality of the statement
     * @param body The body with the level and the statement to be checked
     * @return The quality of the statement with its percentage
     */
    public MLStatement qualityCheckStatement(MLStatementQualityRequest body) {
        logger().info("Started to quality check statement: {} with level: {}", body.getStatement(), body.getLevel());
        try {
            ResponseEntity<MLStatement> responseEntity = restTemplate.exchange(URL+"quality-checker",
                    HttpMethod.POST, new HttpEntity<>(body, new HttpHeaders()), MLStatement.class);
            if(responseEntity.getStatusCode()!= HttpStatus.OK || responseEntity.getBody() == null){
                throw new HttpClientErrorException(responseEntity.getStatusCode());
            }
            return responseEntity.getBody();
        } catch(HttpClientErrorException | HttpServerErrorException e) {
            logger().error("Failed to quality check the statements from the Machine Learning API", e);
            throw new MLAPIRequestFailedException();
        }
    }
}