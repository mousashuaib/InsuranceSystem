package com.insurancesystem.Model.Dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CreateCoverageDTO {

    @JsonIgnore              // اختياري: عشان ما يبعثه العميل أصلاً
    private UUID policyId;

    @NotBlank @Size(max = 160)
    private String serviceName;

    private String description;

    @NotNull @DecimalMin(value = "0.00")
    private BigDecimal amount;

    @NotNull
    private Boolean emergencyEligible;
}
