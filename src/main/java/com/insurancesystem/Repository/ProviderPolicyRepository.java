package com.insurancesystem.Repository;

import com.insurancesystem.Model.Entity.ProviderPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProviderPolicyRepository extends JpaRepository<ProviderPolicy, UUID> {

    List<ProviderPolicy> findByProviderId(UUID providerId);

    List<ProviderPolicy> findByProviderIdAndActiveTrue(UUID providerId);

    @Query("SELECT pp FROM ProviderPolicy pp WHERE pp.provider.id = :providerId " +
           "AND pp.serviceName = :serviceName " +
           "AND pp.active = true " +
           "AND (pp.effectiveFrom IS NULL OR pp.effectiveFrom <= :date) " +
           "AND (pp.effectiveTo IS NULL OR pp.effectiveTo >= :date)")
    Optional<ProviderPolicy> findActivePolicy(
        @Param("providerId") UUID providerId,
        @Param("serviceName") String serviceName,
        @Param("date") LocalDate date);
}
