package com.insurancesystem.Controller;

import com.insurancesystem.Model.Dto.UsageReportDto;
import com.insurancesystem.Services.UsageReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports/usage")
@RequiredArgsConstructor
public class UsageReportController {

    private final UsageReportService service;

    @GetMapping
    @PreAuthorize("hasRole('INSURANCE_MANAGER')")
    public UsageReportDto getUsageReport() {
        return service.generateReport();
    }
}
