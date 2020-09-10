package com.arqaam.logframelab.service;

import com.arqaam.logframelab.exception.MLAPIRequestFailedException;
import com.arqaam.logframelab.exception.WorldBankAPIRequestFailedException;
import com.arqaam.logframelab.model.MLIndicatorResponse;
import com.arqaam.logframelab.model.MLSimilarIndicatorRequest;
import com.arqaam.logframelab.model.MLSimilarIndicatorResponse;
import com.arqaam.logframelab.model.persistence.Indicator;
import com.arqaam.logframelab.util.Logging;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MachineLearningService implements Logging {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

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
        ResponseEntity<List<MLSimilarIndicatorResponse>> responseEntity = restTemplate.exchange(URL+"/indicators/similarity-check",
                HttpMethod.POST, new HttpEntity<>(body), new ParameterizedTypeReference<>() {});
        if(responseEntity.getStatusCode()!= HttpStatus.OK || responseEntity.getBody() == null){
            logger().error("Failed to retrieve similar indicators from the Machine Learning API. Response: {}", responseEntity);
            throw new MLAPIRequestFailedException();
        }
        // Remove its own indicator and map
        return responseEntity.getBody().stream().filter(x->x.getSimilarity()<0.9999).map(MLSimilarIndicatorResponse::getIndicator).collect(Collectors.toList());
    }

    public Map<String, Double> scanForIndicators(String body) {
        logger().info("Started to scan for indicators");
        ResponseEntity<MLIndicatorResponse> responseEntity = restTemplate.exchange(URL+"/indicators",
                HttpMethod.POST, new HttpEntity<>(body, new HttpHeaders()), MLIndicatorResponse.class);
        if(responseEntity.getStatusCode()!= HttpStatus.OK || responseEntity.getBody() == null){
            logger().error("Failed to scan the text for indicators from the Machine Learning API. Response: {}", responseEntity);
            throw new MLAPIRequestFailedException();
        }
        return responseEntity.getBody().getIndicators();
    }
}