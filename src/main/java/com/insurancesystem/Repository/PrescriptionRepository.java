package com.insurancesystem.Repository;

import com.insurancesystem.Model.Entity.Prescription;
import com.insurancesystem.Model.Entity.Enums.PrescriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PrescriptionRepository extends JpaRepository<Prescription, UUID> {
    List<Prescription> findByMemberId(UUID memberId);
    List<Prescription> findByStatus(PrescriptionStatus status);
}
