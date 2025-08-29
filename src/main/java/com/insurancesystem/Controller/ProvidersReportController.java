package com.insurancesystem.Controller;

import com.insurancesystem.Model.Dto.ProvidersReportDto;
import com.insurancesystem.Services.ProvidersReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports/providers")
@RequiredArgsConstructor
public class ProvidersReportController {

    private final ProvidersReportService service;

    @GetMapping
    @PreAuthorize("hasRole('INSURANCE_MANAGER')")
    public ProvidersReportDto getProvidersReport() {
        return service.generateReport();
    }
}
