package com.insurancesystem.Model.Entity;

import com.insurancesystem.Model.Entity.Enums.AllowedGender;
import com.insurancesystem.Model.Entity.Enums.CoverageType;
import com.insurancesystem.Model.Entity.Enums.FrequencyPeriod;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(
        name = "coverages",
        uniqueConstraints = @UniqueConstraint(columnNames = {"policy_id", "service_name"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Coverage {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "policy_id")
    private Policy policy;

    @Column(name = "service_name", nullable = false, length = 160)
    private String serviceName;

    @Column(columnDefinition = "text")
    private String description;

    // Existing fields
    @Builder.Default
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "emergency_eligible", nullable = false)
    private boolean emergencyEligible = false;

    @Builder.Default
    @Column(name = "is_covered", nullable = false)
    private boolean covered = true;

    @Builder.Default
    @Column(name = "coverage_percent", nullable = false, precision = 5, scale = 2)
    private BigDecimal coveragePercent = BigDecimal.valueOf(100.00);

    @Builder.Default
    @Column(name = "max_limit", precision = 12, scale = 2)
    private BigDecimal maxLimit = BigDecimal.ZERO;

    // ===========================
    // ✔ New Essential Insurance Fields
    // ===========================

    // 1️⃣ Coverage Type
    @Enumerated(EnumType.STRING)
    @Column(name = "coverage_type", nullable = false)
    private CoverageType coverageType = CoverageType.OUTPATIENT;

    // 2️⃣ Minimum Deductible
    @Builder.Default
    @Column(name = "minimum_deductible", precision = 12, scale = 2)
    private BigDecimal minimumDeductible = BigDecimal.ZERO;

    // 3️⃣ Requires Referral
    @Builder.Default
    @Column(name = "requires_referral", nullable = false)
    private boolean requiresReferral = false;

    // ===========================
    // ✔ Coverage Rules Fields (Phase 3)
    // ===========================

    // 4️⃣ Gender Restriction
    @Enumerated(EnumType.STRING)
    @Column(name = "allowed_gender")
    @Builder.Default
    private AllowedGender allowedGender = AllowedGender.ALL;

    // 5️⃣ Age Restrictions
    @Column(name = "min_age")
    private Integer minAge;

    @Column(name = "max_age")
    private Integer maxAge;

    // 6️⃣ Frequency Limits
    @Column(name = "frequency_limit")
    private Integer frequencyLimit;

    @Enumerated(EnumType.STRING)
    @Column(name = "frequency_period")
    private FrequencyPeriod frequencyPeriod;
}
