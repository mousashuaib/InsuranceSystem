package com.insurancesystem.Repository;

import com.insurancesystem.Model.Entity.HealthcareProviderClaim;
import com.insurancesystem.Model.Entity.Client;
import com.insurancesystem.Model.Entity.Enums.ClaimStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface HealthcareProviderClaimRepository extends JpaRepository<HealthcareProviderClaim, UUID> {

    List<HealthcareProviderClaim> findByHealthcareProvider(Client provider);

    List<HealthcareProviderClaim> findByStatus(ClaimStatus status);

    long countByStatus(ClaimStatus status);

    @Query("SELECT COALESCE(SUM(c.amount),0) FROM HealthcareProviderClaim c WHERE c.status = 'APPROVED'")
    double sumApprovedAmounts();

    @Query("SELECT SUM(c.amount) FROM HealthcareProviderClaim c WHERE c.status = 'APPROVED'")
    Double getTotalApprovedAmount();

    @Query("SELECT COALESCE(SUM(c.amount),0) FROM HealthcareProviderClaim c WHERE c.status = :status")
    double sumAmountByStatus(@Param("status") ClaimStatus status);

    List<HealthcareProviderClaim> findByStatusIn(List<ClaimStatus> statuses);
    @Query("""
    SELECT c FROM HealthcareProviderClaim c
    WHERE c.status = com.insurancesystem.Model.Entity.Enums.ClaimStatus.APPROVED
""")
    List<HealthcareProviderClaim> findAllApprovedClaims();



}

