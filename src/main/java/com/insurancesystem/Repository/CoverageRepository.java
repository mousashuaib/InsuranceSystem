package com.insurancesystem.Repository;

import com.insurancesystem.Model.Entity.Coverage;
import com.insurancesystem.Model.Entity.Policy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CoverageRepository extends JpaRepository<Coverage, UUID> {
    List<Coverage> findByPolicy(Policy policy);
    boolean existsByPolicyAndServiceNameIgnoreCase(Policy policy, String serviceName);
}
