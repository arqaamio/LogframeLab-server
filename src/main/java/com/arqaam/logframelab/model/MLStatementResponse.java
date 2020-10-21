package com.arqaam.logframelab.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MLStatementResponse {
    private List<MLStatement> impact;
    private List<MLStatement> outcome;
    private List<MLStatement> output;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MLStatement {
        private String statement;
        private String status;
        private Double score;
    }
}
