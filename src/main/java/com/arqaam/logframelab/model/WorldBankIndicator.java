package com.arqaam.logframelab.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WorldBankIndicator {
    private IndicatorProperties indicator;
    private IndicatorProperties country;
    private String countryiso3code;
    private String date;
    private Integer value;
    private String unit;
    private String obs_status;
    private Integer decimal;

    @Data
    public static class IndicatorProperties {
        private String id;
        private String value;
    }
}
