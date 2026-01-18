package com.insurancesystem.Controller;

import com.insurancesystem.Model.Dto.FinancialReportDto;
import com.insurancesystem.Services.FinancialReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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

    @GetMapping("/provider/{providerId}/expenses")
    @PreAuthorize("hasRole('INSURANCE_MANAGER')")
    public ResponseEntity<List<Map<String, Object>>> getProviderExpenses(
            @PathVariable UUID providerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
    ) {
        List<Map<String, Object>> expenses = service.getProviderExpenses(providerId, fromDate, toDate);
        return ResponseEntity.ok(expenses);
    }
}
