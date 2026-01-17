package com.insurancesystem.Repository;

import com.insurancesystem.Model.Entity.LabRequest;
import com.insurancesystem.Model.Entity.Enums.LabRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface LabRequestRepository extends JpaRepository<LabRequest, UUID> {
    List<LabRequest> findByMemberId(UUID memberId);
    List<LabRequest> findByStatus(LabRequestStatus status);
    long countByStatus(LabRequestStatus status);
    long countByStatusAndLabTechId(LabRequestStatus status, UUID labTechId);
    List<LabRequest> findByDoctorId(UUID doctorId);
    long countByDoctorId(UUID doctorId);
    List<LabRequest> findByLabTechId(UUID labTechId);
    
    // Eager fetch member for doctor queries
    @Query("SELECT DISTINCT lr FROM LabRequest lr " +
           "LEFT JOIN FETCH lr.member " +
           "WHERE lr.doctor.id = :doctorId")
    List<LabRequest> findByDoctorIdWithMember(@Param("doctorId") UUID doctorId);
    
    // Eager fetch member for member queries
    @Query("SELECT DISTINCT lr FROM LabRequest lr " +
           "LEFT JOIN FETCH lr.member " +
           "WHERE lr.member.id = :memberId")
    List<LabRequest> findByMemberIdWithMember(@Param("memberId") UUID memberId);

    // Eager fetch member with dateOfBirth and gender for lab tech queries
    @Query("SELECT DISTINCT lr FROM LabRequest lr " +
           "LEFT JOIN FETCH lr.member " +
           "WHERE lr.labTech.id = :labTechId")
    List<LabRequest> findByLabTechIdWithMember(@Param("labTechId") UUID labTechId);

    // Eager fetch member with dateOfBirth and gender for status queries
    @Query("SELECT DISTINCT lr FROM LabRequest lr " +
           "LEFT JOIN FETCH lr.member " +
           "WHERE lr.status = :status")
    List<LabRequest> findByStatusWithMember(@Param("status") LabRequestStatus status);

    // Eager fetch member with dateOfBirth and gender for single entity by ID
    @Query("SELECT DISTINCT lr FROM LabRequest lr " +
           "LEFT JOIN FETCH lr.member " +
           "WHERE lr.id = :id")
    java.util.Optional<LabRequest> findByIdWithMember(@Param("id") UUID id);

}