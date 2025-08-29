package com.insurancesystem.Model.Dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MembersActivityReportDto {

    private long totalMembers;

    private long membersWithClaims;
    private long membersWithPrescriptions;
    private long membersWithLabRequests;
    private long membersWithEmergencyRequests;
    private long membersWithMedicalRecords;
}
