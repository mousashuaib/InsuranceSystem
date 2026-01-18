package com.insurancesystem.Services;

import com.insurancesystem.Model.Dto.FinancialReportDto;
import com.insurancesystem.Model.Entity.Enums.ClaimStatus;
import com.insurancesystem.Model.Entity.HealthcareProviderClaim;

import com.insurancesystem.Repository.HealthcareProviderClaimRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FinancialReportService {

    private final HealthcareProviderClaimRepository claimRepo;

    public FinancialReportDto generateReport() {
        double totalExpenses = claimRepo.sumAmountByStatus(ClaimStatus.APPROVED_FINAL);

        List<FinancialReportDto.TopProvider> topProviders = claimRepo.findTopProviders()
                .stream()
                .map(r -> FinancialReportDto.TopProvider.builder()
                        .providerId((UUID) r[0])
                        .providerName((String) r[1])
                        .totalAmount((Double) r[2])
                        .providerType(r[3] != null ? r[3].toString() : "OTHER")
                        .claimCount(((Long) r[4]).intValue())
                        .build())
                .collect(Collectors.toList());

        return FinancialReportDto.builder()
                .totalExpenses(totalExpenses)
                .topProviders(topProviders)
                .build();
    }

    public List<Map<String, Object>> getProviderExpenses(UUID providerId, LocalDate fromDate, LocalDate toDate) {
        List<HealthcareProviderClaim> claims = claimRepo.findProviderExpenses(providerId, fromDate, toDate);

        return claims.stream().map(claim -> {
            Map<String, Object> expense = new HashMap<>();
            expense.put("id", claim.getId());
            expense.put("clientName", claim.getClientName() != null ? claim.getClientName() : "N/A");
            expense.put("clientId", claim.getClientId());
            expense.put("serviceDate", claim.getServiceDate());
            expense.put("amount", claim.getAmount());
            expense.put("providerType", claim.getHealthcareProvider() != null && claim.getHealthcareProvider().getRequestedRole() != null
                    ? claim.getHealthcareProvider().getRequestedRole().toString() : "OTHER");
            expense.put("description", claim.getDescription());
            expense.put("status", claim.getStatus().toString());
            expense.put("doctorName", claim.getDoctorName());
            expense.put("diagnosis", claim.getDiagnosis());
            expense.put("submittedAt", claim.getSubmittedAt());
            expense.put("approvedAt", claim.getApprovedAt());
            expense.put("insuranceCoveredAmount", claim.getInsuranceCoveredAmount());
            expense.put("clientPayAmount", claim.getClientPayAmount());
            expense.put("isCovered", claim.getIsCovered());
            return expense;
        }).collect(Collectors.toList());
    }
}
