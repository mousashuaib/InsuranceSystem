package com.insurancesystem.Controller;

import com.insurancesystem.Model.Entity.*;
import com.insurancesystem.Model.Entity.Enums.CoverageStatus;
import com.insurancesystem.Repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/coverage-management")
@RequiredArgsConstructor
@Slf4j
public class CoverageManagementController {

    private final MedicinePriceRepository medicinePriceRepository;
    private final MedicalTestRepository medicalTestRepository;
    private final DoctorProcedureRepository doctorProcedureRepository;
    private final MedicalDiagnosisRepository medicalDiagnosisRepository;

    // ==================== MEDICINES ====================

    @GetMapping("/medicines")
    @PreAuthorize("hasRole('INSURANCE_MANAGER')")
    public ResponseEntity<Map<String, Object>> getMedicines(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) CoverageStatus coverageStatus
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("drugName").ascending());
        Page<MedicinePrice> medicines;

        if (search != null && !search.trim().isEmpty()) {
            medicines = medicinePriceRepository.searchByName(search.trim(), pageable);
        } else if (coverageStatus != null) {
            medicines = medicinePriceRepository.findByCoverageStatusAndActiveTrue(coverageStatus, pageable);
        } else {
            medicines = medicinePriceRepository.findByActiveTrue(pageable);
        }

        return ResponseEntity.ok(buildPageResponse(medicines));
    }

    @PatchMapping("/medicines/{id}/coverage")
    @PreAuthorize("hasRole('INSURANCE_MANAGER')")
    public ResponseEntity<Map<String, Object>> updateMedicineCoverage(
            @PathVariable UUID id,
            @RequestBody CoverageUpdateRequest request
    ) {
        try {
            CoverageStatus status = CoverageStatus.valueOf(request.coverageStatus);
            MedicinePrice medicine = medicinePriceRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Medicine not found"));
            medicine.setCoverageStatus(status);
            if (status == CoverageStatus.COVERED && request.coveragePercentage != null) {
                medicine.setCoveragePercentage(Math.max(0, Math.min(100, request.coveragePercentage)));
            } else if (status != CoverageStatus.COVERED) {
                medicine.setCoveragePercentage(0);
            }
            medicinePriceRepository.save(medicine);
            return ResponseEntity.ok(Map.of("success", true, "message", "Coverage status updated"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // ==================== LAB TESTS ====================

    @GetMapping("/lab-tests")
    @PreAuthorize("hasRole('INSURANCE_MANAGER')")
    public ResponseEntity<Map<String, Object>> getLabTests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) CoverageStatus coverageStatus
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("testName").ascending());
        Page<MedicalTest> tests;

        if (search != null && !search.trim().isEmpty()) {
            tests = medicalTestRepository.searchByNameAndCategory(search.trim(), "LAB", pageable);
        } else if (coverageStatus != null) {
            tests = medicalTestRepository.findByCoverageStatusAndActiveTrue(coverageStatus, pageable);
        } else {
            tests = medicalTestRepository.findByCategoryAndActiveTrue("LAB", pageable);
        }

        return ResponseEntity.ok(buildPageResponse(tests));
    }

    @PatchMapping("/lab-tests/{id}/coverage")
    @PreAuthorize("hasRole('INSURANCE_MANAGER')")
    public ResponseEntity<Map<String, Object>> updateLabTestCoverage(
            @PathVariable UUID id,
            @RequestBody CoverageUpdateRequest request
    ) {
        try {
            CoverageStatus status = CoverageStatus.valueOf(request.coverageStatus);
            MedicalTest test = medicalTestRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Lab test not found"));
            test.setCoverageStatus(status);
            if (status == CoverageStatus.COVERED && request.coveragePercentage != null) {
                test.setCoveragePercentage(Math.max(0, Math.min(100, request.coveragePercentage)));
            } else if (status != CoverageStatus.COVERED) {
                test.setCoveragePercentage(0);
            }
            medicalTestRepository.save(test);
            return ResponseEntity.ok(Map.of("success", true, "message", "Coverage status updated"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // ==================== RADIOLOGY ====================

    @GetMapping("/radiology")
    @PreAuthorize("hasRole('INSURANCE_MANAGER')")
    public ResponseEntity<Map<String, Object>> getRadiology(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) CoverageStatus coverageStatus
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("testName").ascending());
        Page<MedicalTest> tests;

        if (search != null && !search.trim().isEmpty()) {
            tests = medicalTestRepository.searchByNameAndCategory(search.trim(), "RADIOLOGY", pageable);
        } else if (coverageStatus != null) {
            tests = medicalTestRepository.findByCoverageStatusAndActiveTrue(coverageStatus, pageable);
        } else {
            tests = medicalTestRepository.findByCategoryAndActiveTrue("RADIOLOGY", pageable);
        }

        return ResponseEntity.ok(buildPageResponse(tests));
    }

    @PatchMapping("/radiology/{id}/coverage")
    @PreAuthorize("hasRole('INSURANCE_MANAGER')")
    public ResponseEntity<Map<String, Object>> updateRadiologyCoverage(
            @PathVariable UUID id,
            @RequestBody CoverageUpdateRequest request
    ) {
        try {
            CoverageStatus status = CoverageStatus.valueOf(request.coverageStatus);
            MedicalTest test = medicalTestRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Radiology test not found"));
            test.setCoverageStatus(status);
            if (status == CoverageStatus.COVERED && request.coveragePercentage != null) {
                test.setCoveragePercentage(Math.max(0, Math.min(100, request.coveragePercentage)));
            } else if (status != CoverageStatus.COVERED) {
                test.setCoveragePercentage(0);
            }
            medicalTestRepository.save(test);
            return ResponseEntity.ok(Map.of("success", true, "message", "Coverage status updated"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // ==================== DOCTOR PROCEDURES ====================

    @GetMapping("/procedures")
    @PreAuthorize("hasRole('INSURANCE_MANAGER')")
    public ResponseEntity<Map<String, Object>> getProcedures(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) CoverageStatus coverageStatus
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("procedureName").ascending());
        Page<DoctorProcedure> procedures;

        if (search != null && !search.trim().isEmpty()) {
            if (category != null && !category.isEmpty()) {
                procedures = doctorProcedureRepository.searchByNameAndCategory(search.trim(), category, pageable);
            } else {
                procedures = doctorProcedureRepository.searchByName(search.trim(), pageable);
            }
        } else if (category != null && !category.isEmpty()) {
            procedures = doctorProcedureRepository.findByCategoryAndActiveTrue(category, pageable);
        } else if (coverageStatus != null) {
            procedures = doctorProcedureRepository.findByCoverageStatusAndActiveTrue(coverageStatus, pageable);
        } else {
            procedures = doctorProcedureRepository.findByActiveTrue(pageable);
        }

        return ResponseEntity.ok(buildPageResponse(procedures));
    }

    @PatchMapping("/procedures/{id}/coverage")
    @PreAuthorize("hasRole('INSURANCE_MANAGER')")
    public ResponseEntity<Map<String, Object>> updateProcedureCoverage(
            @PathVariable UUID id,
            @RequestBody CoverageUpdateRequest request
    ) {
        try {
            CoverageStatus status = CoverageStatus.valueOf(request.coverageStatus);
            DoctorProcedure procedure = doctorProcedureRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Procedure not found"));
            procedure.setCoverageStatus(status);
            if (status == CoverageStatus.COVERED && request.coveragePercentage != null) {
                procedure.setCoveragePercentage(Math.max(0, Math.min(100, request.coveragePercentage)));
            } else if (status != CoverageStatus.COVERED) {
                procedure.setCoveragePercentage(0);
            }
            doctorProcedureRepository.save(procedure);
            return ResponseEntity.ok(Map.of("success", true, "message", "Coverage status updated"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // ==================== DIAGNOSES ====================

    @GetMapping("/diagnoses")
    @PreAuthorize("hasRole('INSURANCE_MANAGER')")
    public ResponseEntity<Map<String, Object>> getDiagnoses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("englishName").ascending());
        Page<MedicalDiagnosis> diagnoses;

        if (search != null && !search.trim().isEmpty()) {
            diagnoses = medicalDiagnosisRepository.searchByName(search.trim(), pageable);
        } else {
            diagnoses = medicalDiagnosisRepository.findByActiveTrue(pageable);
        }

        return ResponseEntity.ok(buildPageResponse(diagnoses));
    }

    // ==================== BULK OPERATIONS ====================

    @PostMapping("/bulk-update")
    @PreAuthorize("hasRole('INSURANCE_MANAGER')")
    public ResponseEntity<Map<String, Object>> bulkUpdateCoverage(@RequestBody BulkUpdateRequest request) {
        try {
            int updated = 0;
            CoverageStatus status = CoverageStatus.valueOf(request.coverageStatus);
            Integer percentage = (status == CoverageStatus.COVERED && request.coveragePercentage != null)
                    ? Math.max(0, Math.min(100, request.coveragePercentage))
                    : (status != CoverageStatus.COVERED ? 0 : 100);

            switch (request.itemType.toLowerCase()) {
                case "medicines":
                    for (String idStr : request.ids) {
                        UUID id = UUID.fromString(idStr);
                        medicinePriceRepository.findById(id).ifPresent(m -> {
                            m.setCoverageStatus(status);
                            m.setCoveragePercentage(percentage);
                            medicinePriceRepository.save(m);
                        });
                        updated++;
                    }
                    break;
                case "lab-tests":
                case "radiology":
                    for (String idStr : request.ids) {
                        UUID id = UUID.fromString(idStr);
                        medicalTestRepository.findById(id).ifPresent(t -> {
                            t.setCoverageStatus(status);
                            t.setCoveragePercentage(percentage);
                            medicalTestRepository.save(t);
                        });
                        updated++;
                    }
                    break;
                case "procedures":
                    for (String idStr : request.ids) {
                        UUID id = UUID.fromString(idStr);
                        doctorProcedureRepository.findById(id).ifPresent(p -> {
                            p.setCoverageStatus(status);
                            p.setCoveragePercentage(percentage);
                            doctorProcedureRepository.save(p);
                        });
                        updated++;
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Unknown item type: " + request.itemType);
            }

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Updated " + updated + " items",
                    "updatedCount", updated
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // Helper methods

    private <T> Map<String, Object> buildPageResponse(Page<T> page) {
        Map<String, Object> response = new HashMap<>();
        response.put("content", page.getContent());
        response.put("totalElements", page.getTotalElements());
        response.put("totalPages", page.getTotalPages());
        response.put("currentPage", page.getNumber());
        response.put("size", page.getSize());
        response.put("hasNext", page.hasNext());
        response.put("hasPrevious", page.hasPrevious());
        return response;
    }

    // Request classes
    public static class CoverageUpdateRequest {
        public String coverageStatus;
        public Integer coveragePercentage;
    }

    public static class BulkUpdateRequest {
        public String itemType;
        public List<String> ids;
        public String coverageStatus;
        public Integer coveragePercentage;
    }
}
