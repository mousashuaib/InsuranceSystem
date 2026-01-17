package com.insurancesystem.Repository;

<<<<<<< HEAD
import com.insurancesystem.Model.Entity.DoctorSpecialization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorSpecializationRepository extends JpaRepository<DoctorSpecialization, Long> {

    Optional<DoctorSpecialization> findByDisplayName(String displayName);

    List<DoctorSpecialization> findAllByOrderByDisplayNameAsc();
}
=======
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

>>>>>>> 59fc73de7f549007a5658aab4146b5707a8a4bd8
