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

    @JsonProperty("indicator-id")
    private Long indicatorId;
    @JsonProperty("semantic-similarity")
    private Double similarity;
}
