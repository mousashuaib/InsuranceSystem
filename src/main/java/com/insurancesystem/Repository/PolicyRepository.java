package com.insurancesystem.Repository;

import com.insurancesystem.Model.Entity.Policy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PolicyRepository extends JpaRepository<Policy, UUID> {
    Optional<Policy> findByPolicyNo(String policyNo);
    boolean existsByPolicyNo(String policyNo);
}
