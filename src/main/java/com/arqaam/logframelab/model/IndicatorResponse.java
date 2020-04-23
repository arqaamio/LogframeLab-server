package com.arqaam.logframelab.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@Builder
@Data
@NoArgsConstructor
public class IndicatorResponse {

    private long id;
    private String level;
    private String color;
    private String label;
    private String description;
    private List<String> keys;
    private String var;
}
