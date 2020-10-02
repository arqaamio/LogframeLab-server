package com.arqaam.logframelab.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IndicatorApprovalRequestDto {

    @NotEmpty
    private List<Approval> approvals;

    @Value
  public static class Approval {
    @NotNull
    Long id;

    @NotNull
    Boolean isApproved;
  }
}
