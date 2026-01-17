package com.insurancesystem.Repository;

import com.insurancesystem.Model.Entity.Client;
import com.insurancesystem.Model.Entity.EmergencyRequest;
import com.insurancesystem.Model.Entity.Enums.EmergencyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EmergencyRequestRepository extends JpaRepository<EmergencyRequest, UUID> {

    // ✅ جميع طلبات الطوارئ لعضو معين
    List<EmergencyRequest> findByMember(Client member);

    // ✅ جميع طلبات الطوارئ التي أنشأها دكتور معين
    List<EmergencyRequest> findByDoctorId(UUID doctorId);

    // ✅ طلبات الطوارئ المعلقة لعضو معين
    List<EmergencyRequest> findByMemberAndStatus(Client member, EmergencyStatus status);

    // ✅ طلبات الطوارئ المعلقة التي أنشأها دكتور معين
    List<EmergencyRequest> findByDoctorIdAndStatus(UUID doctorId, EmergencyStatus status);

    // ✅ عدد طلبات الطوارئ المعلقة
    @Query("SELECT COUNT(e) FROM EmergencyRequest e WHERE e.status = :status")
    long countByStatus(@Param("status") EmergencyStatus status);

    // ✅ جميع طلبات الطوارئ حسب الـ status
    List<EmergencyRequest> findByStatus(EmergencyStatus status);

    // ✅ Eager fetch methods for member data
    @Query("SELECT e FROM EmergencyRequest e LEFT JOIN FETCH e.member WHERE e.member = :member")
    List<EmergencyRequest> findByMemberWithMember(@Param("member") Client member);

    @Query("SELECT e FROM EmergencyRequest e LEFT JOIN FETCH e.member")
    List<EmergencyRequest> findAllWithMember();

    @Query("SELECT e FROM EmergencyRequest e LEFT JOIN FETCH e.member WHERE e.doctorId = :doctorId")
    List<EmergencyRequest> findByDoctorIdWithMember(@Param("doctorId") UUID doctorId);

    @Query("SELECT e FROM EmergencyRequest e LEFT JOIN FETCH e.member WHERE e.id = :id")
    java.util.Optional<EmergencyRequest> findByIdWithMember(@Param("id") UUID id);
}

