package com.insurancesystem.Model.Dto;

import com.insurancesystem.Model.Entity.Enums.ClaimStatus;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
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
    private List<String> invoiceImagePath;
    private Instant submittedAt;
    private Instant approvedAt;
    private Instant rejectedAt;
    private String rejectionReason;
    private String medicalReviewerName;
    private String adminReviewerName;
    private Instant medicalReviewedAt;
    private Instant adminReviewedAt;
    private Boolean emergency;

    private Boolean isCovered;
    private String coverageMessage;
    private Double insuranceCoveredAmount;
    private Double clientPayAmount;
    private Double coveragePercentUsed;
    private Double maxCoverageUsed;

}