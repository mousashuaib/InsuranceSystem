package com.insurancesystem.Model.Entity;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.insurancesystem.Model.Entity.Enums.ClaimStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "healthcare_provider_claims")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HealthcareProviderClaim {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "provider_id")
    private Client healthcareProvider;

    @Column(columnDefinition = "TEXT")
    private String diagnosis;

    @Column(columnDefinition = "TEXT")
    private String treatmentDetails;

    @Column(name = "client_id")
    private UUID clientId;

    @Column(name = "client_name")
    private String clientName;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Double amount;

    @Column(nullable = false)
    private LocalDate serviceDate;

    @Column(columnDefinition = "TEXT")
    private String roleSpecificData;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ClaimStatus status;

    @Column(name = "invoice_image_path")
    private String invoiceImagePath;

    private Instant submittedAt;

    private Instant approvedAt;

    private Instant rejectedAt;
    private String doctorName;
    @Column(columnDefinition = "text")
    private String rejectionReason;

    @PrePersist
    void onCreate() {
        this.submittedAt = Instant.now();
        if (this.status == null) {
            this.status = ClaimStatus.PENDING_MEDICAL;
        }
    }

    // === Medical Admin Reviewer Info ===
    @Column(name = "medical_reviewer_id")
    private UUID medicalReviewerId;

    @Column(name = "medical_reviewer_name")
    private String medicalReviewerName;

    @Column(name = "medical_reviewed_at")
    private Instant medicalReviewedAt;
    @ManyToOne(optional = true)
    @JoinColumn(name = "policy_id", nullable = true)
    private Policy policy;

    // =======================
    //      COVERAGE FIELDS
    // =======================

    @Column(nullable = true)
    private Boolean isCovered;

    @Column(nullable = true)
    private Boolean emergency;


    @Column(columnDefinition = "text")
    private String coverageMessage;

    @Column(precision = 12, scale = 2)
    private BigDecimal insuranceCoveredAmount = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    private BigDecimal clientPayAmount = BigDecimal.ZERO;

    @Column(precision = 5, scale = 2)
    private BigDecimal coveragePercentUsed = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    private BigDecimal maxCoverageUsed = BigDecimal.ZERO;


}
