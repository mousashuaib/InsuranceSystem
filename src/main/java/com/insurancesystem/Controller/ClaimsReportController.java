package com.insurancesystem.Controller;

import com.insurancesystem.Model.Dto.ClaimsReportDto;
import com.insurancesystem.Services.ClaimsReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports/claims")
@RequiredArgsConstructor
public class ClaimsReportController {

    private final ClaimsReportService reportService;

    @GetMapping
    @PreAuthorize("hasRole('INSURANCE_MANAGER')")
    public ClaimsReportDto getClaimsReport() {
        return reportService.generateReport();
    }
}
