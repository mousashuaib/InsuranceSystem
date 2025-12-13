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

    // Provider
    private UUID providerId;
    private String providerName;
    private String providerRole;
    private String roleSpecificData;
    // Medical
    private String diagnosis;
    private String treatmentDetails;
    private LocalDate serviceDate;

    // Files
    private String invoiceImagePath;

    // Status
    private ClaimStatus status;
    private Instant submittedAt;
    private String description; // ⭐ أضف هذا

    // Medical Admin
    private String medicalReviewerName;
    private String medicalReviewedAt;
}
