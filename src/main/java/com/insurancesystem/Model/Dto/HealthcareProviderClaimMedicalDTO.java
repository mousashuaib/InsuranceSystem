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

    // === Patient Info ===
    private UUID clientId;
    private String clientName;

    // === Provider Info ===
    private UUID providerId;
    private String providerName;
    private String providerRole; // DOCTOR / LAB_TECH / PHARMACIST / RADIOLOGIST

    // === Medical Info ===
    private String diagnosis;
    private String treatmentDetails;
    private String description;
    private LocalDate serviceDate;

    // === Files ===
    private String invoiceImagePath;

    private ClaimStatus status;
    private Instant submittedAt;
    private Instant approvedAt;
    private Instant rejectedAt;
    private String rejectionReason;
}
