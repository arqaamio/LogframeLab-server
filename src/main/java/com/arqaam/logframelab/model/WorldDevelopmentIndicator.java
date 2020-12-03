package com.arqaam.logframelab.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class WorldDevelopmentIndicator {
    @JsonProperty("WDI")
    private WDI WDI;


    @Data
    public static class WDI {
        private String label;
        private String source;
        private String updated;
        private List<String> value;
        private Dimension dimension;

        @Data
        public static class Dimension {
            private List<String> id;
            private List<Integer> size;
            private Role role;
            private WDIProperty country;
            private WDIProperty series;
            private WDIProperty year;

            @Data
            public static class Role {
                private List<String> time;
                private List<String> geo;
                private List<String> metric;
            }

            @Data
            public static class WDIProperty {
                private String label;
                private Category category;

                @Data
                public static class Category {
                    private Map<String, Integer> index;
                    private Map<String, String> label;
                    private Map<String, String> unit;
                }
            }
        }
    }

}
