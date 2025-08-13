package com.insurancesystem.Model.Dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CoverageDTO {
    private UUID id;
    private UUID policyId;
    private String serviceName;
    private String description;
    private BigDecimal amount;
    private boolean emergencyEligible;
}
