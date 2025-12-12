package com.insurancesystem.Services;

import com.insurancesystem.Model.Dto.*;
import com.insurancesystem.Repository.CoordinationReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CoordinationReportService {

    private final CoordinationReportRepository reportRepo;

    // ===============================
    // 1️⃣ Total expenses by period
    // ===============================
    public TotalExpenseDTO getTotalExpenses(
            LocalDate from,
            LocalDate to
    ) {
        Double total = reportRepo.totalExpensesByPeriod(from, to);
        return new TotalExpenseDTO(total);
    }

    // ===============================
    // 2️⃣ Expenses by provider
    // ===============================
    public List<ProviderExpenseDTO> getExpensesByProvider() {
        return reportRepo.expensesByProvider()
                .stream()
                .map(row -> new ProviderExpenseDTO(
                        (String) row[0],
                        (Double) row[1]
                ))
                .toList();
    }

    // ===============================
    // 3️⃣ Patient consumption
    // ===============================
    public List<PatientConsumptionDTO> getPatientConsumption() {
        return reportRepo.patientConsumption()
                .stream()
                .map(row -> new PatientConsumptionDTO(
                        (String) row[0],
                        (Double) row[1]
                ))
                .toList();
    }
}
