package com.insurancesystem.Controller;

import com.insurancesystem.Services.ExcelImportService;
import com.insurancesystem.Services.WordImportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/import")
@RequiredArgsConstructor
@Slf4j
public class DataImportController {

    private final ExcelImportService excelImportService;
    private final WordImportService wordImportService;

    /**
     * Import medicine prices from Excel file (اسعار الادوية.xlsx)
     */
    @PostMapping("/medicine-prices")
    @PreAuthorize("hasRole('INSURANCE_MANAGER')")
    public ResponseEntity<Map<String, Object>> importMedicinePrices(@RequestParam("file") MultipartFile file) {
        try {
            validateFile(file, "xlsx");
            ExcelImportService.ImportResult result = excelImportService.importMedicinePrices(file);
            return ResponseEntity.ok(buildResponse(result, "Medicine prices imported successfully"));
        } catch (Exception e) {
            log.error("Error importing medicine prices: ", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Error importing medicine prices: " + e.getMessage()
            ));
        }
    }

    /**
     * Import medicine data with coverage status from Excel file (ملف الادوية.xlsx)
     */
    @PostMapping("/medicine-data")
    @PreAuthorize("hasRole('INSURANCE_MANAGER')")
    public ResponseEntity<Map<String, Object>> importMedicineData(@RequestParam("file") MultipartFile file) {
        try {
            validateFile(file, "xlsx");
            ExcelImportService.ImportResult result = excelImportService.importMedicineData(file);
            return ResponseEntity.ok(buildResponse(result, "Medicine data imported successfully"));
        } catch (Exception e) {
            log.error("Error importing medicine data: ", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Error importing medicine data: " + e.getMessage()
            ));
        }
    }

    /**
     * Import lab tests from Excel file (فحوصات طبية.xlsx)
     */
    @PostMapping("/lab-tests")
    @PreAuthorize("hasRole('INSURANCE_MANAGER')")
    public ResponseEntity<Map<String, Object>> importLabTests(@RequestParam("file") MultipartFile file) {
        try {
            validateFile(file, "xlsx");
            ExcelImportService.ImportResult result = excelImportService.importMedicalTests(file);
            return ResponseEntity.ok(buildResponse(result, "Lab tests imported successfully"));
        } catch (Exception e) {
            log.error("Error importing lab tests: ", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Error importing lab tests: " + e.getMessage()
            ));
        }
    }

    /**
     * Import radiology data from Excel file (ملف الاشعة.xlsx)
     */
    @PostMapping("/radiology")
    @PreAuthorize("hasRole('INSURANCE_MANAGER')")
    public ResponseEntity<Map<String, Object>> importRadiology(@RequestParam("file") MultipartFile file) {
        try {
            validateFile(file, "xlsx");
            ExcelImportService.ImportResult result = excelImportService.importRadiologyData(file);
            return ResponseEntity.ok(buildResponse(result, "Radiology data imported successfully"));
        } catch (Exception e) {
            log.error("Error importing radiology data: ", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Error importing radiology data: " + e.getMessage()
            ));
        }
    }

    /**
     * Import diagnoses from Excel file (تشخيصات طبية.xlsx)
     */
    @PostMapping("/diagnoses")
    @PreAuthorize("hasRole('INSURANCE_MANAGER')")
    public ResponseEntity<Map<String, Object>> importDiagnoses(@RequestParam("file") MultipartFile file) {
        try {
            validateFile(file, "xlsx");
            ExcelImportService.ImportResult result = excelImportService.importDiagnoses(file);
            return ResponseEntity.ok(buildResponse(result, "Diagnoses imported successfully"));
        } catch (Exception e) {
            log.error("Error importing diagnoses: ", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Error importing diagnoses: " + e.getMessage()
            ));
        }
    }

    /**
     * Import medical center data (specializations) from Excel file (بيانات مركز طبي.xlsx)
     */
    @PostMapping("/medical-center")
    @PreAuthorize("hasRole('INSURANCE_MANAGER')")
    public ResponseEntity<Map<String, Object>> importMedicalCenterData(@RequestParam("file") MultipartFile file) {
        try {
            validateFile(file, "xlsx");
            ExcelImportService.ImportResult result = excelImportService.importMedicalCenterData(file);
            return ResponseEntity.ok(buildResponse(result, "Medical center data imported successfully"));
        } catch (Exception e) {
            log.error("Error importing medical center data: ", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Error importing medical center data: " + e.getMessage()
            ));
        }
    }

    /**
     * Import doctor procedures from Word file (اتفاقية طبيب.docx)
     */
    @PostMapping("/doctor-procedures")
    @PreAuthorize("hasRole('INSURANCE_MANAGER')")
    public ResponseEntity<Map<String, Object>> importDoctorProcedures(@RequestParam("file") MultipartFile file) {
        try {
            validateFile(file, "docx");
            ExcelImportService.ImportResult result = wordImportService.importDoctorAgreement(file);
            return ResponseEntity.ok(buildResponse(result, "Doctor procedures imported successfully"));
        } catch (Exception e) {
            log.error("Error importing doctor procedures: ", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Error importing doctor procedures: " + e.getMessage()
            ));
        }
    }

    /**
     * Import policy document from Word file (بوليصة تامين.docx)
     */
    @PostMapping("/policy")
    @PreAuthorize("hasRole('INSURANCE_MANAGER')")
    public ResponseEntity<Map<String, Object>> importPolicy(@RequestParam("file") MultipartFile file) {
        try {
            validateFile(file, "docx");
            ExcelImportService.ImportResult result = wordImportService.importPolicyDocument(file);
            return ResponseEntity.ok(buildResponse(result, "Policy document imported successfully"));
        } catch (Exception e) {
            log.error("Error importing policy document: ", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Error importing policy document: " + e.getMessage()
            ));
        }
    }

    // Helper methods

    private void validateFile(MultipartFile file, String expectedExtension) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is required");
        }

        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith("." + expectedExtension)) {
            throw new IllegalArgumentException("File must be a ." + expectedExtension + " file");
        }
    }

    private Map<String, Object> buildResponse(ExcelImportService.ImportResult result, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        response.put("totalRows", result.totalRows);
        response.put("importedRows", result.importedRows);
        response.put("updatedRows", result.updatedRows);
        response.put("errorRows", result.errorRows);
        if (!result.errors.isEmpty()) {
            response.put("errors", result.errors.size() > 10 ? result.errors.subList(0, 10) : result.errors);
        }
        return response;
    }
}
