package com.arqaam.logframelab.model;

import com.arqaam.logframelab.model.persistence.CRSCode;
import com.arqaam.logframelab.model.persistence.SDGCode;
import com.arqaam.logframelab.model.persistence.Source;
import lombok.*;

import java.util.Set;

@AllArgsConstructor
@Builder
@Data
@NoArgsConstructor
public class IndicatorResponse {

    @EqualsAndHashCode.Exclude
    private long id;
    private String level;
    private String name;
    private String description;
    private String sector;
    private Set<Source> source;
    private Boolean disaggregation;
    private Set<CRSCode> crsCode;
    private Set<SDGCode> sdgCode;
    @EqualsAndHashCode.Exclude
    // private int numTimes;
    private Integer score;

    private String date;
    private String value;
}
