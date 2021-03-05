package com.arqaam.logframelab.model;

import lombok.Data;

import java.util.List;

@Data
public class IndicatorDownloadRequest {
    private List<IndicatorResponse> indicators;
    private List<StatementResponse> statements;
    private Activities activities;
}