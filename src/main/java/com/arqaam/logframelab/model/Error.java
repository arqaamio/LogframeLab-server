package com.arqaam.logframelab.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Error {

    private Integer code;
    private String exception;
    private String message;
    private OffsetDateTime timestamp;
}
