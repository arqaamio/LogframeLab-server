package com.arqaam.logframelab.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MLStatementRequest {
    private List<String> query;
    private String document;
}
