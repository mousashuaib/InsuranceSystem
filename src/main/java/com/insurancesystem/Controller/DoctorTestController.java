package com.insurancesystem.Controller;

import com.insurancesystem.Model.Entity.Client;
import com.insurancesystem.Model.Entity.DoctorTestAssignment;
import com.insurancesystem.Model.Entity.MedicalTest;
import com.insurancesystem.Repository.ClientRepository;
import com.insurancesystem.Services.DoctorTestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/doctor-tests")
@RequiredArgsConstructor
@Slf4j
public class DoctorTestController {

    private final DoctorTestService doctorTestService;
    private final ClientRepository clientRepository;

    // ==================== ASSIGNMENT ENDPOINTS ====================

    /**
     * Assign a test to a doctor
     */
    @PostMapping("/assign")
    @PreAuthorize("hasRole('INSURANCE_MANAGER')")
    public ResponseEntity<Map<String, Object>> assignTestToDoctor(@RequestBody AssignmentRequest request) {
        try {
            Client manager = getCurrentUser();

            DoctorTestAssignment assignment = doctorTestService.assignTestToDoctor(
                    request.doctorId,
                    request.testId,
                    manager,
                    request.testType,
                    request.specialization,
                    request.maxDailyRequests,
                    request.notes
            );

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Test assigned successfully",
                    "assignment", toAssignmentDTO(assignment)
            ));
        } catch (Exception e) {
            log.error("Error assigning test: ", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * Bulk assign tests to a doctor
     */
    @PostMapping("/bulk-assign")
    @PreAuthorize("hasRole('INSURANCE_MANAGER')")
    public ResponseEntity<Map<String, Object>> bulkAssignTests(@RequestBody BulkAssignmentRequest request) {
        try {
            Client manager = getCurrentUser();

            int assigned = doctorTestService.bulkAssignTestsToDoctor(
                    request.doctorId,
                    request.testIds,
                    manager,
                    request.testType,
                    request.specialization
            );

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Assigned " + assigned + " tests",
                    "assignedCount", assigned
            ));
        } catch (Exception e) {
            log.error("Error bulk assigning tests: ", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * Revoke a test assignment
     */
    @DeleteMapping("/revoke/{assignmentId}")
    @PreAuthorize("hasRole('INSURANCE_MANAGER')")
    public ResponseEntity<Map<String, Object>> revokeAssignment(@PathVariable UUID assignmentId) {
        try {
            doctorTestService.revokeTestFromDoctor(assignmentId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Assignment revoked successfully"
            ));
        } catch (Exception e) {
            log.error("Error revoking assignment: ", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * Bulk revoke tests from a doctor
     */
    @PostMapping("/bulk-revoke")
    @PreAuthorize("hasRole('INSURANCE_MANAGER')")
    public ResponseEntity<Map<String, Object>> bulkRevokeTests(@RequestBody BulkRevokeRequest request) {
        try {
            int revoked = doctorTestService.bulkRevokeTestsFromDoctor(
                    request.doctorId,
                    request.testIds
            );

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Revoked " + revoked + " assignments",
                    "revokedCount", revoked
            ));
        } catch (Exception e) {
            log.error("Error bulk revoking tests: ", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * Update assignment restrictions
     */
    @PatchMapping("/{assignmentId}")
    @PreAuthorize("hasRole('INSURANCE_MANAGER')")
    public ResponseEntity<Map<String, Object>> updateAssignment(
            @PathVariable UUID assignmentId,
            @RequestBody UpdateAssignmentRequest request
    ) {
        try {
            DoctorTestAssignment assignment = doctorTestService.updateAssignment(
                    assignmentId,
                    request.maxDailyRequests,
                    request.notes
            );

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Assignment updated successfully",
                    "assignment", toAssignmentDTO(assignment)
            ));
        } catch (Exception e) {
            log.error("Error updating assignment: ", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    // ==================== QUERY ENDPOINTS ====================

    /**
     * Get all assignments (paginated)
     */
    @GetMapping
    @PreAuthorize("hasRole('INSURANCE_MANAGER')")
    public ResponseEntity<Map<String, Object>> getAllAssignments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String specialization,
            @RequestParam(required = false) String testType
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("assignedAt").descending());
        Page<DoctorTestAssignment> assignments;

        if (search != null && !search.isEmpty()) {
            assignments = doctorTestService.searchAssignments(search, testType, pageable);
        } else if (specialization != null && !specialization.isEmpty() && testType != null && !testType.isEmpty()) {
            assignments = doctorTestService.getAssignmentsBySpecializationAndTestType(specialization, testType, pageable);
        } else if (specialization != null && !specialization.isEmpty()) {
            assignments = doctorTestService.getAssignmentsBySpecialization(specialization, pageable);
        } else if (testType != null && !testType.isEmpty()) {
            assignments = doctorTestService.getAssignmentsByTestType(testType, pageable);
        } else {
            assignments = doctorTestService.getAllAssignments(pageable);
        }

        return ResponseEntity.ok(buildPageResponse(assignments));
    }

    /**
     * Get tests assigned to a specific doctor
     */
    @GetMapping("/doctor/{doctorId}")
    @PreAuthorize("hasRole('INSURANCE_MANAGER') or hasRole('DOCTOR')")
    public ResponseEntity<Map<String, Object>> getDoctorTests(
            @PathVariable UUID doctorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("test.testName").ascending());
        Page<DoctorTestAssignment> assignments = doctorTestService.getDoctorTests(doctorId, pageable);

        return ResponseEntity.ok(buildPageResponse(assignments));
    }

    /**
     * Check if doctor can request a specific test
     */
    @GetMapping("/can-request")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('INSURANCE_MANAGER')")
    public ResponseEntity<Map<String, Object>> canRequest(
            @RequestParam UUID doctorId,
            @RequestParam UUID testId
    ) {
        boolean canRequest = doctorTestService.canDoctorRequestTest(doctorId, testId);

        return ResponseEntity.ok(Map.of(
                "canRequest", canRequest
        ));
    }

    // ==================== DROPDOWN DATA ENDPOINTS ====================

    /**
     * Get all doctors for dropdown
     */
    @GetMapping("/doctors")
    @PreAuthorize("hasRole('INSURANCE_MANAGER')")
    public ResponseEntity<List<Map<String, Object>>> getAllDoctors() {
        List<Client> doctors = doctorTestService.getAllDoctors();

        List<Map<String, Object>> response = doctors.stream()
                .map(d -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", d.getId());
                    map.put("fullName", d.getFullName());
                    map.put("specialization", d.getSpecialization());
                    map.put("email", d.getEmail());
                    return map;
                })
                .toList();

        return ResponseEntity.ok(response);
    }

    /**
     * Get all tests for dropdown
     */
    @GetMapping("/tests")
    @PreAuthorize("hasRole('INSURANCE_MANAGER')")
    public ResponseEntity<List<Map<String, Object>>> getAllTests(
            @RequestParam(required = false) String testType
    ) {
        List<MedicalTest> tests;
        if (testType != null && !testType.isEmpty()) {
            tests = doctorTestService.getTestsByType(testType);
        } else {
            tests = doctorTestService.getAllTests();
        }

        List<Map<String, Object>> response = tests.stream()
                .map(t -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", t.getId());
                    map.put("testName", t.getTestName());
                    map.put("category", t.getCategory());
                    map.put("price", t.getPrice());
                    map.put("coverageStatus", t.getCoverageStatus());
                    return map;
                })
                .toList();

        return ResponseEntity.ok(response);
    }

    /**
     * Get distinct specializations
     */
    @GetMapping("/specializations")
    @PreAuthorize("hasRole('INSURANCE_MANAGER')")
    public ResponseEntity<List<String>> getSpecializations() {
        return ResponseEntity.ok(doctorTestService.getDistinctSpecializations());
    }

    // ==================== HELPER METHODS ====================

    private Client getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        return clientRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private Map<String, Object> toAssignmentDTO(DoctorTestAssignment assignment) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", assignment.getId());
        map.put("doctorId", assignment.getDoctor().getId());
        map.put("doctorName", assignment.getDoctor().getFullName());
        map.put("doctorSpecialization", assignment.getDoctor().getSpecialization());
        map.put("testId", assignment.getTest().getId());
        map.put("testName", assignment.getTest().getTestName());
        map.put("testCategory", assignment.getTest().getCategory());
        map.put("testType", assignment.getTestType());
        map.put("specialization", assignment.getSpecialization());
        map.put("maxDailyRequests", assignment.getMaxDailyRequests());
        map.put("notes", assignment.getNotes());
        map.put("active", assignment.isActive());
        map.put("assignedAt", assignment.getAssignedAt());
        if (assignment.getAssignedBy() != null) {
            map.put("assignedBy", assignment.getAssignedBy().getFullName());
        }
        return map;
    }

    private Map<String, Object> buildPageResponse(Page<DoctorTestAssignment> page) {
        Map<String, Object> response = new HashMap<>();
        response.put("content", page.getContent().stream().map(this::toAssignmentDTO).toList());
        response.put("totalElements", page.getTotalElements());
        response.put("totalPages", page.getTotalPages());
        response.put("currentPage", page.getNumber());
        response.put("size", page.getSize());
        response.put("hasNext", page.hasNext());
        response.put("hasPrevious", page.hasPrevious());
        return response;
    }

    // ==================== REQUEST CLASSES ====================

    public static class AssignmentRequest {
        public UUID doctorId;
        public UUID testId;
        public String testType;
        public String specialization;
        public Integer maxDailyRequests;
        public String notes;
    }

    public static class BulkAssignmentRequest {
        public UUID doctorId;
        public List<UUID> testIds;
        public String testType;
        public String specialization;
    }

    public static class BulkRevokeRequest {
        public UUID doctorId;
        public List<UUID> testIds;
    }

    public static class UpdateAssignmentRequest {
        public Integer maxDailyRequests;
        public String notes;
    }
}
