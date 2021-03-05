package com.arqaam.logframelab.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Activities {
    private List<ActivityNameMap> activity;
    private String political_means;
    private String technical_means;
    private String cost;
    private String assumption;
}
