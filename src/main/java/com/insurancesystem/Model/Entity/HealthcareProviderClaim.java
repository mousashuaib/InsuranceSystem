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
<<<<<<< HEAD
    @Column(nullable = false, length = 50)
=======
    @Column(nullable = false, length = 30)
>>>>>>> 59fc73de7f549007a5658aab4146b5707a8a4bd8
    private ClaimStatus status;

    @Column(name = "invoice_image_path")
    private String invoiceImagePath;

    private Instant submittedAt;

    private Instant approvedAt;

    private Instant rejectedAt;
<<<<<<< HEAD

    private Instant medicalReviewedAt;

=======
    private String doctorName;
>>>>>>> 59fc73de7f549007a5658aab4146b5707a8a4bd8
    @Column(columnDefinition = "text")
    private String rejectionReason;

    // Additional fields for frontend display
    @Column(name = "provider_role")
    private String providerRole;

    @Column(columnDefinition = "text")
    private String diagnosis;

    @Column(name = "treatment_details", columnDefinition = "text")
    private String treatmentDetails;

    @Column(name = "is_follow_up")
    private Boolean isFollowUp;

    // Client info fields for display
    @Column(name = "client_age")
    private Integer clientAge;

    @Column(name = "client_gender")
    private String clientGender;

    @Column(name = "client_employee_id")
    private String clientEmployeeId;

    @Column(name = "client_national_id")
    private String clientNationalId;

    @Column(name = "client_faculty")
    private String clientFaculty;

    @Column(name = "client_department")
    private String clientDepartment;

    // Provider info fields for display
    @Column(name = "provider_name")
    private String providerName;

    @Column(name = "provider_employee_id")
    private String providerEmployeeId;

    @Column(name = "provider_national_id")
    private String providerNationalId;

    @Column(name = "provider_specialization")
    private String providerSpecialization;

    @Column(name = "provider_pharmacy_code")
    private String providerPharmacyCode;

    @Column(name = "provider_lab_code")
    private String providerLabCode;

    @Column(name = "provider_radiology_code")
    private String providerRadiologyCode;

    // Family member fields
    @Column(name = "family_member_name")
    private String familyMemberName;

    @Column(name = "family_member_relation")
    private String familyMemberRelation;

    @Column(name = "family_member_age")
    private Integer familyMemberAge;

    @Column(name = "family_member_gender")
    private String familyMemberGender;

    @Column(name = "family_member_insurance_number")
    private String familyMemberInsuranceNumber;

    @Column(name = "family_member_national_id")
    private String familyMemberNationalId;

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

    // Follow-up visit flag
    @Column(name = "is_follow_up", nullable = false)
    @Builder.Default
    private Boolean isFollowUp = false;

    // Original consultation fee (for follow-up visits, this is what patient pays)
    @Column(name = "original_consultation_fee", precision = 12, scale = 2)
    private BigDecimal originalConsultationFee = BigDecimal.ZERO;

}
