package com.insurancesystem.Repository;

import com.insurancesystem.Model.Entity.CoverageUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CoverageUsageRepository extends JpaRepository<CoverageUsage, UUID> {

    Optional<CoverageUsage> findByClientIdAndProviderSpecializationAndUsageDate(
        UUID clientId, String specialization, LocalDate usageDate);

    @Query("SELECT COALESCE(SUM(cu.visitCount), 0) FROM CoverageUsage cu " +
           "WHERE cu.client.id = :clientId AND cu.year = :year")
    Integer countTotalVisitsForYear(@Param("clientId") UUID clientId, @Param("year") Integer year);

    @Query("SELECT COUNT(cu) > 0 FROM CoverageUsage cu " +
           "WHERE cu.client.id = :clientId " +
           "AND cu.providerSpecialization = :specialization " +
           "AND cu.usageDate = :date")
    boolean existsByClientIdAndSpecializationAndDate(
        @Param("clientId") UUID clientId,
        @Param("specialization") String specialization,
        @Param("date") LocalDate date);
}
