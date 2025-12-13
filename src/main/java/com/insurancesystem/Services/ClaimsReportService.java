package com.insurancesystem.Services;

import com.insurancesystem.Model.Dto.ClaimsReportDto;
import com.insurancesystem.Model.Dto.HealthcareProviderClaimDTO;
import com.insurancesystem.Model.Entity.Enums.ClaimStatus;
import com.insurancesystem.Model.MapStruct.HealthcareProviderClaimMapper;
import com.insurancesystem.Repository.HealthcareProviderClaimRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClaimsReportService {

    private final HealthcareProviderClaimRepository claimRepo;
    private final HealthcareProviderClaimMapper claimMapper;

    public ClaimsReportDto generateReport() {
        long totalClaims = claimRepo.count();
        long approvedClaims = claimRepo.countByStatus(ClaimStatus.APPROVED);
        long rejectedClaims = claimRepo.countByStatus(ClaimStatus.REJECTED);
        long pendingClaims = claimRepo.countByStatus(ClaimStatus.PENDING);

        List<HealthcareProviderClaimDTO> approvedList = claimRepo.findByStatus(ClaimStatus.APPROVED)
                .stream().map(claimMapper::toDto).toList();

        List<HealthcareProviderClaimDTO> rejectedList = claimRepo.findByStatus(ClaimStatus.REJECTED)
                .stream().map(claimMapper::toDto).toList();

        List<HealthcareProviderClaimDTO> pendingList = claimRepo.findByStatus(ClaimStatus.PENDING)
                .stream().map(claimMapper::toDto).toList();

        double totalApprovedAmount = approvedList.stream()
                .mapToDouble(c -> c.getAmount() != null ? c.getAmount() : 0)
                .sum();

        double totalRejectedAmount = rejectedList.stream()
                .mapToDouble(c -> c.getAmount() != null ? c.getAmount() : 0)
                .sum();

        return ClaimsReportDto.builder()
                .totalClaims(totalClaims)
                .approvedClaims(approvedClaims)
                .rejectedClaims(rejectedClaims)
                .pendingClaims(pendingClaims)
                .totalApprovedAmount(totalApprovedAmount)
                .totalRejectedAmount(totalRejectedAmount)
                .approvedList(approvedList)
                .rejectedList(rejectedList)
                .pendingList(pendingList)
                .build();
    }
}
