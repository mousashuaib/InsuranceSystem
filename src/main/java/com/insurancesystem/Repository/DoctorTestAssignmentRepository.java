package com.insurancesystem.Repository;

import com.insurancesystem.Model.Entity.Client;
import com.insurancesystem.Model.Entity.DoctorTestAssignment;
import com.insurancesystem.Model.Entity.MedicalTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DoctorTestAssignmentRepository extends JpaRepository<DoctorTestAssignment, UUID> {

    // Find all assignments for a specific doctor
    List<DoctorTestAssignment> findByDoctorAndActiveTrue(Client doctor);

    Page<DoctorTestAssignment> findByDoctorAndActiveTrue(Client doctor, Pageable pageable);

    // Find all assignments for a specific test
    List<DoctorTestAssignment> findByTestAndActiveTrue(MedicalTest test);

    // Find by specialization
    List<DoctorTestAssignment> findBySpecializationAndActiveTrue(String specialization);

    Page<DoctorTestAssignment> findBySpecializationAndActiveTrue(String specialization, Pageable pageable);

    // Find by test type (LAB or RADIOLOGY)
    Page<DoctorTestAssignment> findByTestTypeAndActiveTrue(String testType, Pageable pageable);

    // Find by specialization and test type
    Page<DoctorTestAssignment> findBySpecializationAndTestTypeAndActiveTrue(String specialization, String testType, Pageable pageable);

    // Check if doctor can request a specific test
    boolean existsByDoctorAndTestAndActiveTrue(Client doctor, MedicalTest test);

    // Find specific assignment
    Optional<DoctorTestAssignment> findByDoctorAndTest(Client doctor, MedicalTest test);

    // Find all active assignments
    Page<DoctorTestAssignment> findByActiveTrue(Pageable pageable);

    // Search by doctor name or test name
    @Query("SELECT dta FROM DoctorTestAssignment dta " +
           "WHERE dta.active = true " +
           "AND (LOWER(dta.doctor.fullName) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(dta.test.testName) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<DoctorTestAssignment> searchByDoctorOrTest(@Param("search") String search, Pageable pageable);

    // Search with test type filter
    @Query("SELECT dta FROM DoctorTestAssignment dta " +
           "WHERE dta.active = true " +
           "AND dta.testType = :testType " +
           "AND (LOWER(dta.doctor.fullName) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(dta.test.testName) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<DoctorTestAssignment> searchByDoctorOrTestAndType(@Param("search") String search, @Param("testType") String testType, Pageable pageable);

    // Get all tests assigned to doctors with a specific specialization
    @Query("SELECT DISTINCT dta.test FROM DoctorTestAssignment dta " +
           "WHERE dta.specialization = :specialization AND dta.active = true")
    List<MedicalTest> findTestsBySpecialization(@Param("specialization") String specialization);

    // Get all doctors who can request a specific test
    @Query("SELECT DISTINCT dta.doctor FROM DoctorTestAssignment dta " +
           "WHERE dta.test.id = :testId AND dta.active = true")
    List<Client> findDoctorsByTest(@Param("testId") UUID testId);

    // Count assignments by specialization
    long countBySpecializationAndActiveTrue(String specialization);

    // Count assignments by test type
    long countByTestTypeAndActiveTrue(String testType);

    // Find assignments by doctor ID
    @Query("SELECT dta FROM DoctorTestAssignment dta " +
           "WHERE dta.doctor.id = :doctorId AND dta.active = true")
    List<DoctorTestAssignment> findByDoctorId(@Param("doctorId") UUID doctorId);

    // Find assignments by doctor ID and test type
    @Query("SELECT dta FROM DoctorTestAssignment dta " +
           "WHERE dta.doctor.id = :doctorId AND dta.testType = :testType AND dta.active = true")
    List<DoctorTestAssignment> findByDoctorIdAndTestType(@Param("doctorId") UUID doctorId, @Param("testType") String testType);

    // Bulk check: Get all test IDs assigned to a doctor
    @Query("SELECT dta.test.id FROM DoctorTestAssignment dta " +
           "WHERE dta.doctor.id = :doctorId AND dta.active = true")
    List<UUID> findTestIdsByDoctorId(@Param("doctorId") UUID doctorId);

    // Get distinct specializations
    @Query("SELECT DISTINCT dta.specialization FROM DoctorTestAssignment dta " +
           "WHERE dta.specialization IS NOT NULL AND dta.active = true")
    List<String> findDistinctSpecializations();
}
