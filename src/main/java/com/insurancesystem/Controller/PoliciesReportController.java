package com.insurancesystem.Controller;

import com.insurancesystem.Model.Dto.PoliciesReportDto;
import com.insurancesystem.Services.PoliciesReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports/policies")
@RequiredArgsConstructor
public class PoliciesReportController {

    private final PoliciesReportService service;

    @GetMapping
    @PreAuthorize("hasRole('INSURANCE_MANAGER')")
    public PoliciesReportDto getPoliciesReport() {
        return service.generateReport();
    }
}
