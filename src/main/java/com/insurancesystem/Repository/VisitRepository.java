package com.insurancesystem.Repository;

import com.insurancesystem.Model.Entity.Visit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VisitRepository extends JpaRepository<Visit, UUID> {

    // Count normal (non-follow-up) visits for a patient in a year
    @Query("SELECT COUNT(v) FROM Visit v WHERE " +
           "((v.patient.id = :patientId AND v.patient IS NOT NULL) OR " +
           "(v.familyMember.id = :familyMemberId AND v.familyMember IS NOT NULL)) " +
           "AND v.visitType = 'NORMAL' AND v.visitYear = :year")
    Long countNormalVisitsByPatientAndYear(
        @Param("patientId") UUID patientId,
        @Param("familyMemberId") UUID familyMemberId,
        @Param("year") Integer year
    );

    // Find all visits for a patient (employee or family member) in a year
    @Query("SELECT v FROM Visit v WHERE " +
           "((v.patient.id = :patientId AND v.patient IS NOT NULL) OR " +
           "(v.familyMember.id = :familyMemberId AND v.familyMember IS NOT NULL)) " +
           "AND v.visitYear = :year ORDER BY v.visitDate DESC, v.createdAt DESC")
    List<Visit> findVisitsByPatientAndYear(
        @Param("patientId") UUID patientId,
        @Param("familyMemberId") UUID familyMemberId,
        @Param("year") Integer year
    );

    // Find visits for a patient on a specific date
    @Query("SELECT v FROM Visit v WHERE " +
           "((v.patient.id = :patientId AND v.patient IS NOT NULL) OR " +
           "(v.familyMember.id = :familyMemberId AND v.familyMember IS NOT NULL)) " +
           "AND v.visitDate = :visitDate ORDER BY v.createdAt DESC")
    List<Visit> findVisitsByPatientAndDate(
        @Param("patientId") UUID patientId,
        @Param("familyMemberId") UUID familyMemberId,
        @Param("visitDate") LocalDate visitDate
    );

    // Find visits for a patient with same specialization on same day
    @Query("SELECT v FROM Visit v WHERE " +
           "((v.patient.id = :patientId AND v.patient IS NOT NULL) OR " +
           "(v.familyMember.id = :familyMemberId AND v.familyMember IS NOT NULL)) " +
           "AND v.visitDate = :visitDate " +
           "AND v.doctorSpecialization = :specialization")
    List<Visit> findVisitsByPatientDateAndSpecialization(
        @Param("patientId") UUID patientId,
        @Param("familyMemberId") UUID familyMemberId,
        @Param("visitDate") LocalDate visitDate,
        @Param("specialization") String specialization
    );

    // Find last visit with same doctor for a patient
    @Query("SELECT v FROM Visit v WHERE " +
           "((v.patient.id = :patientId AND v.patient IS NOT NULL) OR " +
           "(v.familyMember.id = :familyMemberId AND v.familyMember IS NOT NULL)) " +
           "AND v.doctor.id = :doctorId " +
           "ORDER BY v.visitDate DESC, v.createdAt DESC")
    List<Visit> findLastVisitsByPatientAndDoctor(
        @Param("patientId") UUID patientId,
        @Param("familyMemberId") UUID familyMemberId,
        @Param("doctorId") UUID doctorId
    );

    // Find visits for a patient with same doctor on same day
    @Query("SELECT v FROM Visit v WHERE " +
           "((v.patient.id = :patientId AND v.patient IS NOT NULL) OR " +
           "(v.familyMember.id = :familyMemberId AND v.familyMember IS NOT NULL)) " +
           "AND v.doctor.id = :doctorId " +
           "AND v.visitDate = :visitDate " +
           "ORDER BY v.createdAt DESC")
    List<Visit> findVisitsByPatientDoctorAndDate(
        @Param("patientId") UUID patientId,
        @Param("familyMemberId") UUID familyMemberId,
        @Param("doctorId") UUID doctorId,
        @Param("visitDate") LocalDate visitDate
    );

    // Find all visits for a patient (for history)
    @Query("SELECT v FROM Visit v WHERE " +
           "((v.patient.id = :patientId AND v.patient IS NOT NULL) OR " +
           "(v.familyMember.id = :familyMemberId AND v.familyMember IS NOT NULL)) " +
           "ORDER BY v.visitDate DESC, v.createdAt DESC")
    List<Visit> findAllVisitsByPatient(
        @Param("patientId") UUID patientId,
        @Param("familyMemberId") UUID familyMemberId
    );
}




