package com.insurancesystem.Controller;

import com.insurancesystem.Model.Dto.*;
import com.insurancesystem.Services.CoordinationReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/coordination/reports")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_COORDINATION_ADMIN')")
public class CoordinationReportController {

    private final CoordinationReportService reportService;

    // ===============================
    // 1️⃣ Total expenses by period
    // ===============================
    @GetMapping("/total-expenses")
    public TotalExpenseDTO totalExpenses(
            @RequestParam LocalDate from,
            @RequestParam LocalDate to
    ) {
        return reportService.getTotalExpenses(from, to);
    }

    // ===============================
    // 2️⃣ Expenses by provider
    // ===============================
    @GetMapping("/by-provider")
    public List<ProviderExpenseDTO> byProvider() {
        return reportService.getExpensesByProvider();
    }

    // ===============================
    // 3️⃣ Patient consumption
    // ===============================
    @GetMapping("/by-patient")
    public List<PatientConsumptionDTO> byPatient() {
        return reportService.getPatientConsumption();
    }
}
