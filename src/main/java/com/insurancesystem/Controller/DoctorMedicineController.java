package com.insurancesystem.Controller;

import com.insurancesystem.Model.Entity.Client;
import com.insurancesystem.Model.Entity.DoctorMedicineAssignment;
import com.insurancesystem.Model.Entity.MedicinePrice;
import com.insurancesystem.Repository.ClientRepository;
import com.insurancesystem.Services.DoctorMedicineService;
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
@RequestMapping("/api/doctor-medicines")
@RequiredArgsConstructor
@Slf4j
public class DoctorMedicineController {

    private final DoctorMedicineService doctorMedicineService;
    private final ClientRepository clientRepository;

    // ==================== ASSIGNMENT ENDPOINTS ====================

    /**
     * Assign a medicine to a doctor
     */
    @PostMapping("/assign")
    @PreAuthorize("hasRole('INSURANCE_MANAGER')")
    public ResponseEntity<Map<String, Object>> assignMedicineToDoctor(@RequestBody AssignmentRequest request) {
        try {
            Client manager = getCurrentUser();

            DoctorMedicineAssignment assignment = doctorMedicineService.assignMedicineToDoctor(
                    request.doctorId,
                    request.medicineId,
                    manager,
                    request.specialization,
                    request.maxDailyPrescriptions,
                    request.maxQuantityPerPrescription,
                    request.notes
            );

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Medicine assigned successfully",
                    "assignment", toAssignmentDTO(assignment)
            ));
        } catch (Exception e) {
            log.error("Error assigning medicine: ", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * Bulk assign medicines to a doctor
     */
    @PostMapping("/bulk-assign")
    @PreAuthorize("hasRole('INSURANCE_MANAGER')")
    public ResponseEntity<Map<String, Object>> bulkAssignMedicines(@RequestBody BulkAssignmentRequest request) {
        try {
            Client manager = getCurrentUser();

            int assigned = doctorMedicineService.bulkAssignMedicinesToDoctor(
                    request.doctorId,
                    request.medicineIds,
                    manager,
                    request.specialization
            );

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Assigned " + assigned + " medicines",
                    "assignedCount", assigned
            ));
        } catch (Exception e) {
            log.error("Error bulk assigning medicines: ", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * Assign all medicines to doctors by specialization
     */
    @PostMapping("/assign-by-specialization")
    @PreAuthorize("hasRole('INSURANCE_MANAGER')")
    public ResponseEntity<Map<String, Object>> assignBySpecialization(@RequestBody SpecializationRequest request) {
        try {
            Client manager = getCurrentUser();

            int assigned = doctorMedicineService.assignMedicinesBySpecialization(
                    request.specialization,
                    manager
            );

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Assigned " + assigned + " medicine-doctor pairs",
                    "assignedCount", assigned
            ));
        } catch (Exception e) {
            log.error("Error assigning by specialization: ", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * Revoke a medicine assignment
     */
    @DeleteMapping("/revoke/{assignmentId}")
    @PreAuthorize("hasRole('INSURANCE_MANAGER')")
    public ResponseEntity<Map<String, Object>> revokeAssignment(@PathVariable UUID assignmentId) {
        try {
            doctorMedicineService.revokeMedicineFromDoctor(assignmentId);

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
     * Bulk revoke medicines from a doctor
     */
    @PostMapping("/bulk-revoke")
    @PreAuthorize("hasRole('INSURANCE_MANAGER')")
    public ResponseEntity<Map<String, Object>> bulkRevokeMedicines(@RequestBody BulkRevokeRequest request) {
        try {
            int revoked = doctorMedicineService.bulkRevokeMedicinesFromDoctor(
                    request.doctorId,
                    request.medicineIds
            );

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Revoked " + revoked + " assignments",
                    "revokedCount", revoked
            ));
        } catch (Exception e) {
            log.error("Error bulk revoking medicines: ", e);
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
            DoctorMedicineAssignment assignment = doctorMedicineService.updateAssignment(
                    assignmentId,
                    request.maxDailyPrescriptions,
                    request.maxQuantityPerPrescription,
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
            @RequestParam(required = false) String specialization
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("assignedAt").descending());
        Page<DoctorMedicineAssignment> assignments;

        if (search != null && !search.isEmpty()) {
            assignments = doctorMedicineService.searchAssignments(search, pageable);
        } else if (specialization != null && !specialization.isEmpty()) {
            assignments = doctorMedicineService.getAssignmentsBySpecialization(specialization, pageable);
        } else {
            assignments = doctorMedicineService.getAllAssignments(pageable);
        }

        return ResponseEntity.ok(buildPageResponse(assignments));
    }

    /**
     * Get medicines assigned to a specific doctor
     */
    @GetMapping("/doctor/{doctorId}")
    @PreAuthorize("hasRole('INSURANCE_MANAGER') or hasRole('DOCTOR')")
    public ResponseEntity<Map<String, Object>> getDoctorMedicines(
            @PathVariable UUID doctorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("medicine.drugName").ascending());
        Page<DoctorMedicineAssignment> assignments = doctorMedicineService.getDoctorMedicines(doctorId, pageable);

        return ResponseEntity.ok(buildPageResponse(assignments));
    }

    /**
     * Check if doctor can prescribe a specific medicine
     */
    @GetMapping("/can-prescribe")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('INSURANCE_MANAGER')")
    public ResponseEntity<Map<String, Object>> canPrescribe(
            @RequestParam UUID doctorId,
            @RequestParam UUID medicineId
    ) {
        boolean canPrescribe = doctorMedicineService.canDoctorPrescribeMedicine(doctorId, medicineId);

        return ResponseEntity.ok(Map.of(
                "canPrescribe", canPrescribe
        ));
    }

    // ==================== DROPDOWN DATA ENDPOINTS ====================

    /**
     * Get all doctors for dropdown
     */
    @GetMapping("/doctors")
    @PreAuthorize("hasRole('INSURANCE_MANAGER')")
    public ResponseEntity<List<Map<String, Object>>> getAllDoctors() {
        List<Client> doctors = doctorMedicineService.getAllDoctors();

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
     * Get all medicines for dropdown
     */
    @GetMapping("/medicines")
    @PreAuthorize("hasRole('INSURANCE_MANAGER')")
    public ResponseEntity<List<Map<String, Object>>> getAllMedicines() {
        List<MedicinePrice> medicines = doctorMedicineService.getAllMedicines();

        List<Map<String, Object>> response = medicines.stream()
                .map(m -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", m.getId());
                    map.put("drugName", m.getDrugName());
                    map.put("genericName", m.getGenericName());
                    map.put("type", m.getType());
                    map.put("price", m.getPrice());
                    map.put("coverageStatus", m.getCoverageStatus());
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
        return ResponseEntity.ok(doctorMedicineService.getDistinctSpecializations());
    }

    // ==================== HELPER METHODS ====================

    private Client getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        return clientRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private Map<String, Object> toAssignmentDTO(DoctorMedicineAssignment assignment) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", assignment.getId());
        map.put("doctorId", assignment.getDoctor().getId());
        map.put("doctorName", assignment.getDoctor().getFullName());
        map.put("doctorSpecialization", assignment.getDoctor().getSpecialization());
        map.put("medicineId", assignment.getMedicine().getId());
        map.put("medicineName", assignment.getMedicine().getDrugName());
        map.put("medicineGenericName", assignment.getMedicine().getGenericName());
        map.put("specialization", assignment.getSpecialization());
        map.put("maxDailyPrescriptions", assignment.getMaxDailyPrescriptions());
        map.put("maxQuantityPerPrescription", assignment.getMaxQuantityPerPrescription());
        map.put("notes", assignment.getNotes());
        map.put("active", assignment.isActive());
        map.put("assignedAt", assignment.getAssignedAt());
        if (assignment.getAssignedBy() != null) {
            map.put("assignedBy", assignment.getAssignedBy().getFullName());
        }
        return map;
    }

    private Map<String, Object> buildPageResponse(Page<DoctorMedicineAssignment> page) {
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
        public UUID medicineId;
        public String specialization;
        public Integer maxDailyPrescriptions;
        public Integer maxQuantityPerPrescription;
        public String notes;
    }

    public static class BulkAssignmentRequest {
        public UUID doctorId;
        public List<UUID> medicineIds;
        public String specialization;
    }

    public static class BulkRevokeRequest {
        public UUID doctorId;
        public List<UUID> medicineIds;
    }

    public static class SpecializationRequest {
        public String specialization;
    }

    public static class UpdateAssignmentRequest {
        public Integer maxDailyPrescriptions;
        public Integer maxQuantityPerPrescription;
        public String notes;
    }
}
