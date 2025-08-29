package com.insurancesystem.Controller;

import com.insurancesystem.Model.Dto.FinancialReportDto;
import com.insurancesystem.Services.FinancialReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports/financial")
@RequiredArgsConstructor
public class FinancialReportController {

    private final FinancialReportService service;

    @GetMapping
    @PreAuthorize("hasRole('INSURANCE_MANAGER')")
    public FinancialReportDto getFinancialReport() {
        return service.generateReport();
    }
}
