package com.insurancesystem.Repository;

import com.insurancesystem.Model.Entity.RadiologyRequest;
import com.insurancesystem.Model.Entity.Enums.LabRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface RadiologistRepository extends JpaRepository<RadiologyRequest, UUID> {

    // ✅ جميع طلبات الأشعة لراديولوجي معين
    List<RadiologyRequest> findByRadiologistId(UUID radiologistId);

    // ✅ جميع طلبات الأشعة لطبيب معين
    List<RadiologyRequest> findByDoctorId(UUID doctorId);

    // ✅ جميع طلبات الأشعة لعضو معين (المريض)
    List<RadiologyRequest> findByMemberId(UUID memberId);

    // ✅ طلبات الأشعة المعلقة (بدون تخصيص راديولوجي محدد)
    List<RadiologyRequest> findByStatusAndRadiologistIsNull(LabRequestStatus status);

    // ✅ عدد طلبات الأشعة المعلقة لراديولوجي معين
    long countByStatusAndRadiologistId(LabRequestStatus status, UUID radiologistId);

    // ✅ جميع طلبات الأشعة بحالة معينة
    List<RadiologyRequest> findByStatus(LabRequestStatus status);

    // Eager fetch member for radiologist queries
    @Query("SELECT DISTINCT rr FROM RadiologyRequest rr " +
           "LEFT JOIN FETCH rr.member " +
           "WHERE rr.radiologist.id = :radiologistId")
    List<RadiologyRequest> findByRadiologistIdWithMember(@Param("radiologistId") UUID radiologistId);

    // Eager fetch member for doctor queries
    @Query("SELECT DISTINCT rr FROM RadiologyRequest rr " +
           "LEFT JOIN FETCH rr.member " +
           "WHERE rr.doctor.id = :doctorId")
    List<RadiologyRequest> findByDoctorIdWithMember(@Param("doctorId") UUID doctorId);

    // Eager fetch member for member queries
    @Query("SELECT DISTINCT rr FROM RadiologyRequest rr " +
           "LEFT JOIN FETCH rr.member " +
           "WHERE rr.member.id = :memberId")
    List<RadiologyRequest> findByMemberIdWithMember(@Param("memberId") UUID memberId);

    // Eager fetch member for status queries
    @Query("SELECT DISTINCT rr FROM RadiologyRequest rr " +
           "LEFT JOIN FETCH rr.member " +
           "WHERE rr.status = :status")
    List<RadiologyRequest> findByStatusWithMember(@Param("status") LabRequestStatus status);

}
