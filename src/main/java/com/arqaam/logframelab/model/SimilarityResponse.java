package com.arqaam.logframelab.model;

import com.arqaam.logframelab.model.persistence.Indicator;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SimilarityResponse {
    private Indicator indicator;
    private List<Indicator> similarIndicators;
}
