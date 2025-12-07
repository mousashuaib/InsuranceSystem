package com.insurancesystem.Model.Entity;

import com.insurancesystem.Model.Entity.Enums.ClaimStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "claims")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Claim {

    @Id
    @GeneratedValue
    private UUID id;

    private String memberName;

    @ManyToOne(optional = false)
    @JoinColumn(name = "member_id")
    private Client member;

    @ManyToOne(optional = false)
    @JoinColumn(name = "policy_id")
    private Policy policy;

    @Column(nullable = false)
    private String description;

    private String diagnosis;
    private String treatmentDetails;

    @Column(nullable = false)
    private Double amount;

    @Column(nullable = false)
    private LocalDate serviceDate;

    private String providerName;
    private String doctorName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ClaimStatus status;

    @ElementCollection
    @CollectionTable(name = "claim_invoice_images", joinColumns = @JoinColumn(name = "claim_id"))
    @Column(name = "image_path")
    private List<String> invoiceImagePath = new ArrayList<>();

    private Instant submittedAt;
    private Instant medicalReviewedAt;
    private Instant adminReviewedAt;
    private Instant approvedAt;
    private Instant rejectedAt;

    @Column(columnDefinition = "text")
    private String rejectionReason;

    @ManyToOne
    @JoinColumn(name = "medical_reviewer_id")
    private Client medicalReviewer;

    @ManyToOne
    @JoinColumn(name = "admin_reviewer_id")
    private Client adminReviewer;

    @PrePersist
    void onCreate() {
        this.submittedAt = Instant.now();
        if (this.status == null)
            this.status = ClaimStatus.PENDING;
    }

    // =======================
    //      COVERAGE FIELDS
    // =======================

    @Column(nullable = true)
    private Boolean isCovered;

    @Column(name = "emergency", nullable = false)
    private Boolean emergency = false;

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
