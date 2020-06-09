package com.arqaam.logframelab.controller.dto;

import java.util.List;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Value;

@Value
public class TempIndicatorApprovalRequestDto {

  @NotEmpty
  List<Approval> approvals;

  @Value
  public static class Approval {
    @NotNull
    Long id;

    @NotNull
    Boolean isApproved;
  }
}
