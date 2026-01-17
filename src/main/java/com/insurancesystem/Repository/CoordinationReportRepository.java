package com.insurancesystem.Repository;

import com.insurancesystem.Model.Entity.HealthcareProviderClaim;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface CoordinationReportRepository
        extends JpaRepository<HealthcareProviderClaim, UUID> {

    // ===============================
    // 1️⃣ Total expenses by period
    // ===============================
    @Query("""
        SELECT COALESCE(SUM(c.amount), 0)
        FROM HealthcareProviderClaim c
        WHERE c.status = 'APPROVED'
        AND c.serviceDate BETWEEN :from AND :to
    """)
    Double totalExpensesByPeriod(
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );

    // ===============================
    // 2️⃣ Expenses by provider
    // ===============================
    @Query("""
        SELECT c.healthcareProvider.fullName,
               SUM(c.amount)
        FROM HealthcareProviderClaim c
        WHERE c.status = 'APPROVED'
        GROUP BY c.healthcareProvider.fullName
    """)
    List<Object[]> expensesByProvider();

    // ===============================
    // 3️⃣ Patient consumption
    // ===============================
    @Query("""
        SELECT c.clientName,
               SUM(c.amount)
        FROM HealthcareProviderClaim c
        WHERE c.status = 'APPROVED'
        GROUP BY c.clientName
    """)
    List<Object[]> patientConsumption();
}
