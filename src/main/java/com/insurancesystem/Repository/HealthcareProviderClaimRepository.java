package com.insurancesystem.Repository;

import com.insurancesystem.Model.Entity.Enums.RoleName;
import com.insurancesystem.Model.Entity.HealthcareProviderClaim;
import com.insurancesystem.Model.Entity.Client;
import com.insurancesystem.Model.Entity.Enums.ClaimStatus;

import com.insurancesystem.Model.Entity.Policy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface HealthcareProviderClaimRepository extends JpaRepository<HealthcareProviderClaim, UUID> {

    List<HealthcareProviderClaim> findByHealthcareProvider(Client provider);
    
    @Query("""
        SELECT c FROM HealthcareProviderClaim c
        WHERE c.healthcareProvider.id = :providerId
    """)
    List<HealthcareProviderClaim> findByHealthcareProviderId(@Param("providerId") UUID providerId);

    List<HealthcareProviderClaim> findByStatus(ClaimStatus status);
    
    @Query("""
        SELECT c FROM HealthcareProviderClaim c
        JOIN FETCH c.healthcareProvider
        WHERE c.status = :status
    """)
    List<HealthcareProviderClaim> findByStatusWithProvider(@Param("status") ClaimStatus status);

    void deleteAllByPolicy(Policy policy);


    long countByStatus(ClaimStatus status);

    @Query("""
    SELECT c.healthcareProvider.fullName AS providerName,
           SUM(c.amount) AS totalAmount
    FROM HealthcareProviderClaim c
    WHERE c.status = com.insurancesystem.Model.Entity.Enums.ClaimStatus.APPROVED_FINAL
    GROUP BY c.healthcareProvider.fullName
    ORDER BY totalAmount DESC
""")
    List<Object[]> findTopProviders();



    @Query("""
    SELECT COALESCE(SUM(c.amount),0)
    FROM HealthcareProviderClaim c
    WHERE c.status = com.insurancesystem.Model.Entity.Enums.ClaimStatus.APPROVED_FINAL
""")
    Double getTotalApprovedAmount();


    @Query("SELECT COALESCE(SUM(c.amount),0) FROM HealthcareProviderClaim c WHERE c.status = :status")
    double sumAmountByStatus(@Param("status") ClaimStatus status);

    List<HealthcareProviderClaim> findByStatusIn(List<ClaimStatus> statuses);
    
    @Query("""
        SELECT c FROM HealthcareProviderClaim c
        JOIN FETCH c.healthcareProvider
        WHERE c.status IN :statuses
    """)
    List<HealthcareProviderClaim> findByStatusInWithProvider(@Param("statuses") List<ClaimStatus> statuses);
    @Query("""
    SELECT c FROM HealthcareProviderClaim c
    WHERE c.status = com.insurancesystem.Model.Entity.Enums.ClaimStatus.APPROVED_FINAL
""")
    List<HealthcareProviderClaim> findAllApprovedClaims();


    @Query("""
        SELECT c.doctorName, COUNT(c)
        FROM HealthcareProviderClaim c
        WHERE c.doctorName IS NOT NULL AND c.doctorName <> ''
        GROUP BY c.doctorName
        ORDER BY COUNT(c) DESC
    """)
    List<Object[]> findTopDoctorsByClaims();

    @Query("""
SELECT DISTINCT c FROM HealthcareProviderClaim c
JOIN c.healthcareProvider.roles r
WHERE (:status IS NULL OR c.status = :status)
AND (:roleName IS NULL OR r.name = :roleName)
AND (:from IS NULL OR c.serviceDate >= :from)
AND (:to IS NULL OR c.serviceDate <= :to)
""")
    List<HealthcareProviderClaim> filterClaims(
            @Param("status") ClaimStatus status,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to,
            @Param("roleName") RoleName roleName
    );
    List<HealthcareProviderClaim> findByClientId(UUID clientId);



}

