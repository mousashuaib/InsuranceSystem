package com.insurancesystem.Model.Dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CreatePolicyDTO {

    @NotBlank @Size(max = 50)
    private String policyNo;

    @NotBlank @Size(max = 120)
    private String name;

    private String description;

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;

    @NotNull @DecimalMin(value = "0.00")
    private BigDecimal coverageLimit;

    @NotNull @DecimalMin(value = "0.00")
    private BigDecimal deductible;

    private String emergencyRules;
}
