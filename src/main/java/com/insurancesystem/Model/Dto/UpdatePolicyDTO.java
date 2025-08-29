package com.insurancesystem.Model.Dto;

import com.insurancesystem.Model.Entity.Enums.PolicyStatus;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class UpdatePolicyDTO {
    @Size(max = 120)
    private String name;

    private String description;

    private LocalDate startDate;

    private LocalDate endDate;

    private PolicyStatus status;

    @DecimalMin(value = "0.00")
    private BigDecimal coverageLimit;

    @DecimalMin(value = "0.00")
    private BigDecimal deductible;

    private String emergencyRules;
}
