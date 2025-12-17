package com.insurancesystem.Repository;

import com.insurancesystem.Model.Entity.DoctorSpecializationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DoctorSpecializationRepository extends JpaRepository<DoctorSpecializationEntity, Long> {

    /**
     * Find specialization by display name
     */
    Optional<DoctorSpecializationEntity> findByDisplayName(String displayName);

    /**
     * Find specialization by ID (inherited from JpaRepository)
     */
    // findById is already available from JpaRepository
}

