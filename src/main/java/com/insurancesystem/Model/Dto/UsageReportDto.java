package com.insurancesystem.Model.Dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsageReportDto {

    private long totalClaims;
    private long approvedClaims;
    private long rejectedClaims;
    private long pendingClaims;

    private long totalPrescriptions;
    private long verifiedPrescriptions;
    private long rejectedPrescriptions;
    private long pendingPrescriptions;

    private long totalLabRequests;
    private long completedLabRequests;
    private long pendingLabRequests;

    private long totalEmergencyRequests;
    private long approvedEmergencyRequests;
    private long rejectedEmergencyRequests;
    private long pendingEmergencyRequests;

    private long totalMedicalRecords;
}
