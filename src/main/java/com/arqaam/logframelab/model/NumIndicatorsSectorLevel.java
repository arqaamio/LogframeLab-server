package com.arqaam.logframelab.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NumIndicatorsSectorLevel {

    private String sector;
    private List<CountIndicatorsByLevel> counter;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CountIndicatorsByLevel {
        private String level;
        private Long count;
    }
}
