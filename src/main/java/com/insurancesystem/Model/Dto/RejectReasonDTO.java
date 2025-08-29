package com.insurancesystem.Model.Dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RejectReasonDTO {
    @NotBlank
    private String reason;
}
