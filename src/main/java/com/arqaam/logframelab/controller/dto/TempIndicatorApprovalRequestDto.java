package com.arqaam.logframelab.controller.dto;

import java.util.List;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TempIndicatorApprovalRequestDto {

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
