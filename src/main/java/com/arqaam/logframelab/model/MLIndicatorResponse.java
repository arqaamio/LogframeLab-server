package com.arqaam.logframelab.model;

import lombok.Data;

import java.util.Map;

@Data
public class MLIndicatorResponse {
    private Map<String, Double> indicators;
}