package com.insurancesystem.Services;

import com.insurancesystem.Model.Dto.FinancialReportDto;
import com.insurancesystem.Model.Entity.Enums.ClaimStatus;
import com.insurancesystem.Repository.ClaimRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FinancialReportService {

    private final ClaimRepository claimRepo;

    public FinancialReportDto generateReport() {
        double totalExpenses = claimRepo.sumAmountByStatus(ClaimStatus.APPROVED);

        List<FinancialReportDto.TopProvider> topProviders = claimRepo.findTopProviders()
                .stream()
                .map(r -> FinancialReportDto.TopProvider.builder()
                        .providerName((String) r[0])
                        .totalAmount((Double) r[1])
                        .build())
                .limit(5) // نجيب أعلى 5 فقط
                .collect(Collectors.toList());

        return FinancialReportDto.builder()
                .totalExpenses(totalExpenses)
                .topProviders(topProviders)
                .build();
    }
}
