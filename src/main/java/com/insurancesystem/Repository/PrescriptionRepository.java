package com.insurancesystem.Repository;

import com.insurancesystem.Model.Entity.Prescription;
import com.insurancesystem.Model.Entity.Enums.PrescriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    // 🔹 الوصفات حسب الدكتور
    List<Prescription> findByDoctorId(UUID doctorId);

    // 🔹 الوصفات حسب الصيدلي
    List<Prescription> findByPharmacistId(UUID pharmacistId);

    // 🔹 البحث عن وصفات المريض حسب الحالة
    List<Prescription> findByMemberIdAndStatus(UUID memberId, PrescriptionStatus status);

    // 🔹 البحث عن وصفات المريض مع الحالات المتعددة (PENDING و VERIFIED)
    @Query("SELECT p FROM Prescription p WHERE p.member.id = :memberId AND (p.status = 'PENDING' OR p.status = 'VERIFIED')")
    List<Prescription> findActivePrescriptionsByMember(@Param("memberId") UUID memberId);

    // 🔹 البحث عن وصفات المريض والدكتور
    List<Prescription> findByMemberIdAndDoctorId(UUID memberId, UUID doctorId);

    // 🔹 البحث عن وصفات المريض والصيدلي
    List<Prescription> findByMemberIdAndPharmacistId(UUID memberId, UUID pharmacistId);

    // 🔹 البحث عن وصفات الدكتور والحالة
    List<Prescription> findByDoctorIdAndStatus(UUID doctorId, PrescriptionStatus status);

    // 🔹 البحث عن وصفات الصيدلي والحالة
    List<Prescription> findByPharmacistIdAndStatus(UUID pharmacistId, PrescriptionStatus status);

    // ✅ Custom queries with JOIN FETCH to eagerly load member with dateOfBirth and gender
    // Using JOIN FETCH ensures member entity is fully loaded including dateOfBirth and gender
    @Query("SELECT DISTINCT p FROM Prescription p " +
            "LEFT JOIN FETCH p.member m " +
            "LEFT JOIN FETCH p.doctor d " +
            "LEFT JOIN FETCH p.pharmacist ph " +
            "WHERE p.status = :status")
    List<Prescription> findByStatusWithMember(@Param("status") PrescriptionStatus status);

    // ✅ Use JOIN FETCH to eagerly load member with all fields including dateOfBirth and gender
    // The DISTINCT is needed because JOIN FETCH can create duplicates
    @Query("SELECT DISTINCT p FROM Prescription p " +
           "LEFT JOIN FETCH p.member m " +
           "LEFT JOIN FETCH p.doctor d " +
           "LEFT JOIN FETCH p.pharmacist ph " +
           "WHERE p.pharmacist.id = :pharmacistId " +
           "ORDER BY p.createdAt DESC")
    List<Prescription> findByPharmacistIdWithMember(@Param("pharmacistId") UUID pharmacistId);

    @Query("SELECT DISTINCT p FROM Prescription p " +
            "LEFT JOIN FETCH p.member m " +
            "LEFT JOIN FETCH p.doctor d " +
            "LEFT JOIN FETCH p.pharmacist ph " +
            "WHERE p.member.id = :memberId")
    List<Prescription> findByMemberIdWithMember(@Param("memberId") UUID memberId);

    @Query("SELECT DISTINCT p FROM Prescription p " +
            "LEFT JOIN FETCH p.member m " +
            "LEFT JOIN FETCH p.doctor d " +
            "LEFT JOIN FETCH p.pharmacist ph " +
            "WHERE p.doctor.id = :doctorId")
    List<Prescription> findByDoctorIdWithMember(@Param("doctorId") UUID doctorId);
}
