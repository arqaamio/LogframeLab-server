package com.arqaam.logframelab.model;

import lombok.*;

@AllArgsConstructor
@Builder
@Data
@NoArgsConstructor
public class IndicatorResponse {

    @EqualsAndHashCode.Exclude
    private long id;
    private String level;
    private String color;
    private String name;
    private String description;
    private String var;
    private String themes;
    private String source;
    private Boolean disaggregation;
    private String crsCode;
    private String sdgCode;
    @EqualsAndHashCode.Exclude
    private int numTimes;
}
