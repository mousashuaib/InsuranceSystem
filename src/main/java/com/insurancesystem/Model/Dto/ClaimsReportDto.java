package com.insurancesystem.Model.Dto;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClaimsReportDto {

    // ====== Summary ======
    private long totalClaims;
    private long approvedClaims;
    private long rejectedClaims;
    private long pendingClaims;
    private double totalApprovedAmount;
    private double totalRejectedAmount;

    // ====== Detailed lists ======
    private List<ClaimDTO> approvedList;
    private List<ClaimDTO> rejectedList;
    private List<ClaimDTO> pendingList;
}
