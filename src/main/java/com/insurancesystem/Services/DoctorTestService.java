package com.insurancesystem.Services;

import com.insurancesystem.Exception.BadRequestException;
import com.insurancesystem.Exception.NotFoundException;
import com.insurancesystem.Model.Entity.Client;
import com.insurancesystem.Model.Entity.DoctorTestAssignment;
import com.insurancesystem.Model.Entity.Enums.RoleName;
import com.insurancesystem.Model.Entity.MedicalTest;
import com.insurancesystem.Repository.ClientRepository;
import com.insurancesystem.Repository.DoctorTestAssignmentRepository;
import com.insurancesystem.Repository.MedicalTestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DoctorTestService {

    private final DoctorTestAssignmentRepository assignmentRepository;
    private final ClientRepository clientRepository;
    private final MedicalTestRepository medicalTestRepository;

    /**
     * Assign a test to a doctor
     */
    public DoctorTestAssignment assignTestToDoctor(
            UUID doctorId,
            UUID testId,
            Client assignedBy,
            String testType,
            String specialization,
            Integer maxDailyRequests,
            String notes
    ) {
        Client doctor = clientRepository.findById(doctorId)
                .orElseThrow(() -> new NotFoundException("Doctor not found"));

        // Verify the client is a doctor
        if (!doctor.hasRole(RoleName.DOCTOR)) {
            throw new BadRequestException("User is not a doctor");
        }

        MedicalTest test = medicalTestRepository.findById(testId)
                .orElseThrow(() -> new NotFoundException("Test not found"));

        // Check if assignment already exists
        if (assignmentRepository.existsByDoctorAndTestAndActiveTrue(doctor, test)) {
            throw new BadRequestException("Test is already assigned to this doctor");
        }

        // Check if there's an inactive assignment to reactivate
        var existingAssignment = assignmentRepository.findByDoctorAndTest(doctor, test);
        if (existingAssignment.isPresent()) {
            DoctorTestAssignment assignment = existingAssignment.get();
            assignment.setActive(true);
            assignment.setAssignedBy(assignedBy);
            assignment.setTestType(testType != null ? testType : test.getCategory());
            assignment.setSpecialization(specialization != null ? specialization : doctor.getSpecialization());
            assignment.setMaxDailyRequests(maxDailyRequests);
            assignment.setNotes(notes);
            return assignmentRepository.save(assignment);
        }

        // Create new assignment
        DoctorTestAssignment assignment = DoctorTestAssignment.builder()
                .doctor(doctor)
                .test(test)
                .assignedBy(assignedBy)
                .testType(testType != null ? testType : test.getCategory())
                .specialization(specialization != null ? specialization : doctor.getSpecialization())
                .maxDailyRequests(maxDailyRequests)
                .notes(notes)
                .active(true)
                .build();

        log.info("Assigning test {} to doctor {} by manager {}",
                test.getTestName(), doctor.getFullName(), assignedBy.getFullName());

        return assignmentRepository.save(assignment);
    }

    /**
     * Bulk assign tests to a doctor
     */
    public int bulkAssignTestsToDoctor(
            UUID doctorId,
            List<UUID> testIds,
            Client assignedBy,
            String testType,
            String specialization
    ) {
        Client doctor = clientRepository.findById(doctorId)
                .orElseThrow(() -> new NotFoundException("Doctor not found"));

        if (!doctor.hasRole(RoleName.DOCTOR)) {
            throw new BadRequestException("User is not a doctor");
        }

        int assigned = 0;
        for (UUID testId : testIds) {
            try {
                MedicalTest test = medicalTestRepository.findById(testId).orElse(null);
                if (test == null) continue;

                if (!assignmentRepository.existsByDoctorAndTestAndActiveTrue(doctor, test)) {
                    var existing = assignmentRepository.findByDoctorAndTest(doctor, test);
                    if (existing.isPresent()) {
                        existing.get().setActive(true);
                        existing.get().setAssignedBy(assignedBy);
                        assignmentRepository.save(existing.get());
                    } else {
                        DoctorTestAssignment assignment = DoctorTestAssignment.builder()
                                .doctor(doctor)
                                .test(test)
                                .assignedBy(assignedBy)
                                .testType(testType != null ? testType : test.getCategory())
                                .specialization(specialization != null ? specialization : doctor.getSpecialization())
                                .active(true)
                                .build();
                        assignmentRepository.save(assignment);
                    }
                    assigned++;
                }
            } catch (Exception e) {
                log.warn("Failed to assign test {} to doctor {}: {}", testId, doctorId, e.getMessage());
            }
        }

        log.info("Bulk assigned {} tests to doctor {}", assigned, doctor.getFullName());
        return assigned;
    }

    /**
     * Revoke a test assignment from a doctor
     */
    public void revokeTestFromDoctor(UUID assignmentId) {
        DoctorTestAssignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new NotFoundException("Assignment not found"));

        assignment.setActive(false);
        assignmentRepository.save(assignment);

        log.info("Revoked test {} from doctor {}",
                assignment.getTest().getTestName(), assignment.getDoctor().getFullName());
    }

    /**
     * Bulk revoke tests from a doctor
     */
    public int bulkRevokeTestsFromDoctor(UUID doctorId, List<UUID> testIds) {
        Client doctor = clientRepository.findById(doctorId)
                .orElseThrow(() -> new NotFoundException("Doctor not found"));

        int revoked = 0;
        for (UUID testId : testIds) {
            MedicalTest test = medicalTestRepository.findById(testId).orElse(null);
            if (test == null) continue;

            var assignment = assignmentRepository.findByDoctorAndTest(doctor, test);
            if (assignment.isPresent() && assignment.get().isActive()) {
                assignment.get().setActive(false);
                assignmentRepository.save(assignment.get());
                revoked++;
            }
        }

        log.info("Revoked {} tests from doctor {}", revoked, doctor.getFullName());
        return revoked;
    }

    /**
     * Get all tests assigned to a doctor
     */
    @Transactional(readOnly = true)
    public List<DoctorTestAssignment> getDoctorTests(UUID doctorId) {
        Client doctor = clientRepository.findById(doctorId)
                .orElseThrow(() -> new NotFoundException("Doctor not found"));

        return assignmentRepository.findByDoctorAndActiveTrue(doctor);
    }

    /**
     * Get paginated tests assigned to a doctor
     */
    @Transactional(readOnly = true)
    public Page<DoctorTestAssignment> getDoctorTests(UUID doctorId, Pageable pageable) {
        Client doctor = clientRepository.findById(doctorId)
                .orElseThrow(() -> new NotFoundException("Doctor not found"));

        return assignmentRepository.findByDoctorAndActiveTrue(doctor, pageable);
    }

    /**
     * Check if a doctor can request a specific test
     */
    @Transactional(readOnly = true)
    public boolean canDoctorRequestTest(UUID doctorId, UUID testId) {
        Client doctor = clientRepository.findById(doctorId).orElse(null);
        MedicalTest test = medicalTestRepository.findById(testId).orElse(null);

        if (doctor == null || test == null) return false;

        return assignmentRepository.existsByDoctorAndTestAndActiveTrue(doctor, test);
    }

    /**
     * Get all assignments (for manager view)
     */
    @Transactional(readOnly = true)
    public Page<DoctorTestAssignment> getAllAssignments(Pageable pageable) {
        return assignmentRepository.findByActiveTrue(pageable);
    }

    /**
     * Get assignments by test type
     */
    @Transactional(readOnly = true)
    public Page<DoctorTestAssignment> getAssignmentsByTestType(String testType, Pageable pageable) {
        return assignmentRepository.findByTestTypeAndActiveTrue(testType, pageable);
    }

    /**
     * Search assignments
     */
    @Transactional(readOnly = true)
    public Page<DoctorTestAssignment> searchAssignments(String search, Pageable pageable) {
        return assignmentRepository.searchByDoctorOrTest(search, pageable);
    }

    /**
     * Search assignments with test type filter
     */
    @Transactional(readOnly = true)
    public Page<DoctorTestAssignment> searchAssignments(String search, String testType, Pageable pageable) {
        if (testType != null && !testType.isEmpty()) {
            return assignmentRepository.searchByDoctorOrTestAndType(search, testType, pageable);
        }
        return assignmentRepository.searchByDoctorOrTest(search, pageable);
    }

    /**
     * Get assignments by specialization
     */
    @Transactional(readOnly = true)
    public Page<DoctorTestAssignment> getAssignmentsBySpecialization(String specialization, Pageable pageable) {
        return assignmentRepository.findBySpecializationAndActiveTrue(specialization, pageable);
    }

    /**
     * Get assignments by specialization and test type
     */
    @Transactional(readOnly = true)
    public Page<DoctorTestAssignment> getAssignmentsBySpecializationAndTestType(String specialization, String testType, Pageable pageable) {
        return assignmentRepository.findBySpecializationAndTestTypeAndActiveTrue(specialization, testType, pageable);
    }

    /**
     * Get all doctors (for dropdown)
     */
    @Transactional(readOnly = true)
    public List<Client> getAllDoctors() {
        return clientRepository.findAll().stream()
                .filter(c -> c.hasRole(RoleName.DOCTOR))
                .toList();
    }

    /**
     * Get all active tests (for dropdown)
     */
    @Transactional(readOnly = true)
    public List<MedicalTest> getAllTests() {
        return medicalTestRepository.findAll().stream()
                .filter(MedicalTest::isActive)
                .toList();
    }

    /**
     * Get tests by type
     */
    @Transactional(readOnly = true)
    public List<MedicalTest> getTestsByType(String testType) {
        return medicalTestRepository.findAll().stream()
                .filter(MedicalTest::isActive)
                .filter(t -> testType.equalsIgnoreCase(t.getCategory()))
                .toList();
    }

    /**
     * Get distinct specializations
     */
    @Transactional(readOnly = true)
    public List<String> getDistinctSpecializations() {
        return clientRepository.findAll().stream()
                .filter(c -> c.hasRole(RoleName.DOCTOR))
                .map(Client::getSpecialization)
                .filter(s -> s != null && !s.isEmpty())
                .distinct()
                .sorted()
                .toList();
    }

    /**
     * Update assignment restrictions
     */
    public DoctorTestAssignment updateAssignment(
            UUID assignmentId,
            Integer maxDailyRequests,
            String notes
    ) {
        DoctorTestAssignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new NotFoundException("Assignment not found"));

        if (maxDailyRequests != null) {
            assignment.setMaxDailyRequests(maxDailyRequests);
        }
        if (notes != null) {
            assignment.setNotes(notes);
        }

        return assignmentRepository.save(assignment);
    }
}
