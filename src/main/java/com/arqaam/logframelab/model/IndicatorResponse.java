package com.arqaam.logframelab.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
//     private List<String> keys; //TODO remove
    private String var;
    private String themes;
    private String source;
    private Boolean disaggregation;
    private String crsCode;
    private String sdgCode;
}
