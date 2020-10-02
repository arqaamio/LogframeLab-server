package com.arqaam.logframelab.controller.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class SortDto {

    @NotBlank
    String property;

    @NotBlank
    String direction;
}
