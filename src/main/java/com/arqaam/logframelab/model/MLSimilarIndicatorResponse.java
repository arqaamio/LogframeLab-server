package com.arqaam.logframelab.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MLSimilarIndicatorResponse {

    private String indicator;
    @JsonProperty("semantic-similarity")
    private Double similarity;

//    private List<MLSimilarIndicator> results;
//
//    @Data
//    @NoArgsConstructor
//    @AllArgsConstructor
//    public static class MLSimilarIndicator {
//        private String indicator;
//        @JsonProperty("semantic-similarity")
//        private Double similarity;
//    }
}
