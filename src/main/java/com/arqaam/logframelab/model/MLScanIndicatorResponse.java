package com.arqaam.logframelab.model;

import lombok.Data;

import java.util.List;

@Data
public class MLScanIndicatorResponse {
    private List<List<String>> indicators;
}