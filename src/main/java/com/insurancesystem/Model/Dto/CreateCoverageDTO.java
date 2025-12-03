package com.insurancesystem.Model.Dto;

import com.insurancesystem.Model.Entity.Enums.CoverageType;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class CreateCoverageDTO {

    private UUID policyId;

    private String serviceName;
    private String description;

    private BigDecimal amount = BigDecimal.ZERO;
    private boolean emergencyEligible = false;
    private boolean covered = true;
    private BigDecimal coveragePercent = BigDecimal.valueOf(100);

    private BigDecimal maxLimit = BigDecimal.ZERO;

    private CoverageType coverageType = CoverageType.OUTPATIENT;

    private BigDecimal minimumDeductible = BigDecimal.ZERO;

    private boolean requiresReferral = false;
}
