package com.insurancesystem.Model.Dto;

import lombok.Data;

@Data
public class HealthcareAdminReviewDTO {

    private String id;

    // Patient
    private String clientName;
    private String employeeId;

    // Provider
    private String providerName;
    private String providerRole;

    // Financial
    private Double amount;
    private String serviceDate;

    // Administrative
    private String status;
    private String submittedAt;

    // Medical admin approval info
    private String medicalReviewerName;
    private String medicalReviewedAt;

    private String approvedAt;
    private String rejectedAt;
    private String rejectionReason;

    // Attachment
    private String invoiceImagePath;
}
