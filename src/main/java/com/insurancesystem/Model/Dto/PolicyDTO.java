package com.insurancesystem.Model.Dto;

import com.insurancesystem.Model.Entity.Enums.PolicyStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class PolicyDTO {
    private UUID id;
    private String policyNo;
    private String name;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private PolicyStatus status;
    private BigDecimal coverageLimit;
    private BigDecimal deductible;
    private String emergencyRules;
    private Instant createdAt;
    private Instant updatedAt;

    private List<CoverageDTO> coverages;

}