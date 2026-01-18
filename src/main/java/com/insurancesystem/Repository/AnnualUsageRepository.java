package com.insurancesystem.Repository;

import com.insurancesystem.Model.Entity.AnnualUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AnnualUsageRepository extends JpaRepository<AnnualUsage, UUID> {

    Optional<AnnualUsage> findByClientIdAndYearAndServiceType(UUID clientId, Integer year, String serviceType);

    List<AnnualUsage> findByClientIdAndYear(UUID clientId, Integer year);

    List<AnnualUsage> findByClientId(UUID clientId);
}
