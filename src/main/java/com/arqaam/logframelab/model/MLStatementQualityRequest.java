package com.arqaam.logframelab.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MLStatementQualityRequest {
    private String statement;
    private String level;
}
