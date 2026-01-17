package com.insurancesystem.Model.Dto;

import com.insurancesystem.Model.Entity.Enums.ClaimStatus;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
@Data
public class HealthcareProviderClaimMedicalDTO {

    private UUID id;

    // Patient
    private UUID clientId;
    private String clientName;
    private String employeeId;
    private Integer clientAge;
    private String clientGender;
    private String clientNationalId;
    private String clientFaculty;
    private String clientDepartment;
    
    // Family Member Information (if claim is for a family member)
    private UUID familyMemberId;
    private String familyMemberName;
    private String familyMemberRelation;
    private Integer familyMemberAge;
    private String familyMemberGender;
    private String familyMemberInsuranceNumber;
    private String familyMemberNationalId;
    
    private Double amount;

    // Provider
    private UUID providerId;
    private String providerName;
    private String providerRole;
    private String providerEmployeeId;
    private String providerNationalId;
    private String providerSpecialization; // For doctors only
    private String providerPharmacyCode; // For pharmacists
    private String providerLabCode; // For lab techs
    private String providerRadiologyCode; // For radiologists
    private String doctorName; // For client claims (outside network)
    private String roleSpecificData;
    // Medical
    private String diagnosis;
    private String treatmentDetails;
    private LocalDate serviceDate;
    // HealthcareProviderClaimMedicalDTO
    private Boolean returnedByCoordinator;
    private String coordinatorNote;

    // Files
    private String invoiceImagePath;

    // Status
    private ClaimStatus status;
    private Instant submittedAt;
    private String description; // ⭐ أضف هذا

    // Medical Admin
    private String medicalReviewerName;
    private Instant medicalReviewedAt;
    
    // Follow-up visit information
    private Boolean isFollowUp;
    private java.math.BigDecimal originalConsultationFee;
}
