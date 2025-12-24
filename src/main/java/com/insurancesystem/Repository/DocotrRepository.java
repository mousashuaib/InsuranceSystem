package com.insurancesystem.Repository;

import com.insurancesystem.Model.Entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DocotrRepository extends JpaRepository<Doctor, UUID> {

    long countByDoctorId(UUID doctorId);

}

