package com.insurancesystem.Controller;

import com.insurancesystem.Services.DataExportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/export")
@RequiredArgsConstructor
@Slf4j
public class DataExportController {

    private final DataExportService dataExportService;

    // ==================== EXCEL EXPORTS ====================

    @GetMapping("/medicines/excel")
    @PreAuthorize("hasRole('INSURANCE_MANAGER')")
    public ResponseEntity<byte[]> exportMedicinesToExcel() {
        try {
            byte[] data = dataExportService.exportMedicinesToExcel();
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=medicines.xlsx")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(data);
        } catch (Exception e) {
            log.error("Error exporting medicines to Excel: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/lab-tests/excel")
    @PreAuthorize("hasRole('INSURANCE_MANAGER')")
    public ResponseEntity<byte[]> exportLabTestsToExcel() {
        try {
            byte[] data = dataExportService.exportLabTestsToExcel();
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=lab_tests.xlsx")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(data);
        } catch (Exception e) {
            log.error("Error exporting lab tests to Excel: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/radiology/excel")
    @PreAuthorize("hasRole('INSURANCE_MANAGER')")
    public ResponseEntity<byte[]> exportRadiologyToExcel() {
        try {
            byte[] data = dataExportService.exportRadiologyToExcel();
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=radiology.xlsx")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(data);
        } catch (Exception e) {
            log.error("Error exporting radiology to Excel: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/diagnoses/excel")
    @PreAuthorize("hasRole('INSURANCE_MANAGER')")
    public ResponseEntity<byte[]> exportDiagnosesToExcel() {
        try {
            byte[] data = dataExportService.exportDiagnosesToExcel();
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=diagnoses.xlsx")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(data);
        } catch (Exception e) {
            log.error("Error exporting diagnoses to Excel: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/procedures/excel")
    @PreAuthorize("hasRole('INSURANCE_MANAGER')")
    public ResponseEntity<byte[]> exportProceduresToExcel() {
        try {
            byte[] data = dataExportService.exportProceduresToExcel();
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=doctor_procedures.xlsx")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(data);
        } catch (Exception e) {
            log.error("Error exporting procedures to Excel: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // ==================== PDF EXPORTS ====================

    @GetMapping("/medicines/pdf")
    @PreAuthorize("hasRole('INSURANCE_MANAGER')")
    public ResponseEntity<byte[]> exportMedicinesToPdf() {
        try {
            byte[] data = dataExportService.exportMedicinesToPdf();
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=medicines.pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(data);
        } catch (Exception e) {
            log.error("Error exporting medicines to PDF: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/lab-tests/pdf")
    @PreAuthorize("hasRole('INSURANCE_MANAGER')")
    public ResponseEntity<byte[]> exportLabTestsToPdf() {
        try {
            byte[] data = dataExportService.exportLabTestsToPdf();
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=lab_tests.pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(data);
        } catch (Exception e) {
            log.error("Error exporting lab tests to PDF: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/radiology/pdf")
    @PreAuthorize("hasRole('INSURANCE_MANAGER')")
    public ResponseEntity<byte[]> exportRadiologyToPdf() {
        try {
            byte[] data = dataExportService.exportRadiologyToPdf();
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=radiology.pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(data);
        } catch (Exception e) {
            log.error("Error exporting radiology to PDF: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/diagnoses/pdf")
    @PreAuthorize("hasRole('INSURANCE_MANAGER')")
    public ResponseEntity<byte[]> exportDiagnosesToPdf() {
        try {
            byte[] data = dataExportService.exportDiagnosesToPdf();
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=diagnoses.pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(data);
        } catch (Exception e) {
            log.error("Error exporting diagnoses to PDF: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/procedures/pdf")
    @PreAuthorize("hasRole('INSURANCE_MANAGER')")
    public ResponseEntity<byte[]> exportProceduresToPdf() {
        try {
            byte[] data = dataExportService.exportProceduresToPdf();
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=doctor_procedures.pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(data);
        } catch (Exception e) {
            log.error("Error exporting procedures to PDF: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // ==================== POLICIES EXPORTS ====================

    @GetMapping("/policies/excel")
    @PreAuthorize("hasRole('INSURANCE_MANAGER')")
    public ResponseEntity<byte[]> exportPoliciesToExcel() {
        try {
            byte[] data = dataExportService.exportPoliciesToExcel();
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=policies_and_coverages.xlsx")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(data);
        } catch (Exception e) {
            log.error("Error exporting policies to Excel: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/policies/pdf")
    @PreAuthorize("hasRole('INSURANCE_MANAGER')")
    public ResponseEntity<byte[]> exportPoliciesToPdf() {
        try {
            byte[] data = dataExportService.exportPoliciesToPdf();
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=policies_and_coverages.pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(data);
        } catch (Exception e) {
            log.error("Error exporting policies to PDF: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
