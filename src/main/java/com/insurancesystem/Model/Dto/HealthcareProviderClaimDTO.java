package com.insurancesystem.Model.Dto;

import com.insurancesystem.Model.Entity.Enums.ClaimStatus;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class HealthcareProviderClaimDTO {
    private UUID id;
    private UUID providerId;
    private String providerName;
    private String providerEmployeeId;
    private String providerNationalId;
    private UUID clientId;
    private String clientName;
    // Patient/Client information
    private Integer clientAge;
    private String clientGender;
    private String clientInsuranceNumber;
    private String clientEmployeeId;
    private String clientNationalId;
    private String clientFaculty;
    private String clientDepartment;
    // Family member information (if claim is for family member)
    private UUID familyMemberId;
    private String familyMemberName;
    private String familyMemberRelation;
    private Integer familyMemberAge;
    private String familyMemberGender;
    private String familyMemberInsuranceNumber;
    private String familyMemberNationalId;
    private String description;
    private Double amount;
    private String diagnosis;
    private String treatmentDetails;
    private LocalDate serviceDate;
    private String roleSpecificData;
    private ClaimStatus status;
    private String invoiceImagePath;
    private Instant submittedAt;
    private Instant approvedAt;
    private Instant rejectedAt;
    private String rejectionReason;
    private String medicalReviewerName;
    private Instant medicalReviewedAt;
    private String providerRole;
    private String employeeId;
    
    // Follow-up visit information
    private Boolean isFollowUp;
    private java.math.BigDecimal originalConsultationFee;
}

