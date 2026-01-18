package com.insurancesystem.Repository;

import com.insurancesystem.Model.Entity.DoctorSpecializationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorSpecializationRepository extends JpaRepository<DoctorSpecializationEntity, Long> {

    /**
     * Find specialization by display name
     */
    Optional<DoctorSpecializationEntity> findByDisplayName(String displayName);

    /**
     * Find specialization by display name (case insensitive)
     */
    Optional<DoctorSpecializationEntity> findByDisplayNameIgnoreCase(String displayName);

    /**
     * Check if specialization exists by display name (case insensitive)
     */
    boolean existsByDisplayNameIgnoreCase(String displayName);

    /**
     * Find specialization by ID (inherited from JpaRepository)
     */
    // findById is already available from JpaRepository

    /**
     * Debug: Get raw specialization data with diagnoses
     */
    @Query(value = "SELECT id, display_name, diagnoses, treatment_plans FROM doctor_specialization", nativeQuery = true)
    List<Object[]> findAllSpecializationsRaw();
}

