package com.arqaam.logframelab.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class MLScanIndicatorResponse {
    private List<MLScanIndicator> indicators;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MLScanIndicator {
        private String indicator;
        @JsonProperty("indicator-id")
        private Long id;
        @JsonProperty("search-result")
        private MLSearchResult searchResult;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MLSearchResult {
        private Double similarity;
    }
}