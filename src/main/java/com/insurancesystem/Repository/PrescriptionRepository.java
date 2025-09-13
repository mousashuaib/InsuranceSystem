package com.insurancesystem.Repository;

import com.insurancesystem.Model.Entity.Prescription;
import com.insurancesystem.Model.Entity.Enums.PrescriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PrescriptionRepository extends JpaRepository<Prescription, UUID> {
    // 🔹 المريض
    List<Prescription> findByMemberId(UUID memberId);

    // 🔹 الوصفات حسب الحالة
    List<Prescription> findByStatus(PrescriptionStatus status);

    // 🔹 إحصائيات الدكتور
    long countByDoctorId(UUID doctorId);
    long countByDoctorIdAndStatus(UUID doctorId, PrescriptionStatus status);

    // 🔹 إحصائيات عامة
    long countByStatus(PrescriptionStatus status);
    long countByMemberIdAndStatus(UUID memberId, PrescriptionStatus status);

    // 🔹 إحصائيات الصيدلي
    long countByPharmacistId(UUID pharmacistId);
    long countByPharmacistIdAndStatus(UUID pharmacistId, PrescriptionStatus status);
    // PrescriptionRepository.java
    List<Prescription> findByDoctorId(UUID doctorId);

}
