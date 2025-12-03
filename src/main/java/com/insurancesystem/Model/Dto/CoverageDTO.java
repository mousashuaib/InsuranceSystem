package com.insurancesystem.Model.Dto;

import com.insurancesystem.Model.Entity.Enums.CoverageType;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class CoverageDTO {
    private UUID id;
    private UUID policyId;

    private String serviceName;
    private String description;

    private BigDecimal amount;
    private boolean emergencyEligible;
    private boolean covered;
    private BigDecimal coveragePercent;
    private BigDecimal maxLimit;

    private CoverageType coverageType;
    private BigDecimal minimumDeductible;
    private boolean requiresReferral;
}
