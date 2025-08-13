package com.insurancesystem.Model.Dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class UpdateCoverageDTO {
    @Size(max = 160)
    private String serviceName;

    private String description;

    @DecimalMin(value = "0.00")
    private BigDecimal amount;

    private Boolean emergencyEligible;
}
