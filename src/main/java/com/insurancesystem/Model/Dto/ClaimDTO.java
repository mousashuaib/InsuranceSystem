package com.insurancesystem.Model.Dto;

import com.insurancesystem.Model.Entity.Enums.ClaimStatus;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class ClaimDTO {
    private UUID id;
    private UUID memberId;
    private String memberName;
    private UUID policyId;
    private String policyName;
    private String description;
    private String diagnosis;
    private String treatmentDetails;
    private Double amount;
    private LocalDate serviceDate;
    private String providerName;
    private String doctorName;
    private ClaimStatus status;
    private String invoiceImagePath;
    private Instant submittedAt;
    private Instant approvedAt;
    private Instant rejectedAt;
    private String rejectionReason;
}
