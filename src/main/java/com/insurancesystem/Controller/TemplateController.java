package com.insurancesystem.Controller;

import com.insurancesystem.Services.TemplateGeneratorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/templates")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class TemplateController {

    private final TemplateGeneratorService templateGeneratorService;

    @GetMapping("/medicine-prices")
    public ResponseEntity<byte[]> getMedicinePricesTemplate() throws IOException {
        byte[] content = templateGeneratorService.generateMedicinePricesTemplate();
        return createExcelResponse(content, "اسعار الادوية - Medicine Prices Template.xlsx");
    }

    @GetMapping("/medicine-data")
    public ResponseEntity<byte[]> getMedicineDataTemplate() throws IOException {
        byte[] content = templateGeneratorService.generateMedicineDataTemplate();
        return createExcelResponse(content, "ملف الادوية - Medicine Data Template.xlsx");
    }

    @GetMapping("/lab-tests")
    public ResponseEntity<byte[]> getLabTestsTemplate() throws IOException {
        byte[] content = templateGeneratorService.generateLabTestsTemplate();
        return createExcelResponse(content, "فحوصات طبية - Lab Tests Template.xlsx");
    }

    @GetMapping("/radiology")
    public ResponseEntity<byte[]> getRadiologyTemplate() throws IOException {
        byte[] content = templateGeneratorService.generateRadiologyTemplate();
        return createExcelResponse(content, "ملف الاشعة - Radiology Template.xlsx");
    }

    @GetMapping("/diagnoses")
    public ResponseEntity<byte[]> getDiagnosesTemplate() throws IOException {
        byte[] content = templateGeneratorService.generateDiagnosesTemplate();
        return createExcelResponse(content, "تشخيصات طبية - Diagnoses Template.xlsx");
    }

    @GetMapping("/medical-center")
    public ResponseEntity<byte[]> getMedicalCenterTemplate() throws IOException {
        byte[] content = templateGeneratorService.generateMedicalCenterTemplate();
        return createExcelResponse(content, "بيانات مركز طبي - Medical Center Template.xlsx");
    }

    @GetMapping("/doctor-procedures")
    public ResponseEntity<byte[]> getDoctorProceduresTemplate() throws IOException {
        byte[] content = templateGeneratorService.generateDoctorProceduresTemplate();
        return createWordResponse(content, "اتفاقية طبيب - Doctor Procedures Template.docx");
    }

    @GetMapping("/policy")
    public ResponseEntity<byte[]> getPolicyTemplate() throws IOException {
        byte[] content = templateGeneratorService.generatePolicyTemplate();
        return createWordResponse(content, "بوليصة تامين - Insurance Policy Template.docx");
    }

    private ResponseEntity<byte[]> createExcelResponse(byte[] content, String filename) {
        String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData("attachment", filename);
        headers.set("Content-Disposition", "attachment; filename=\"" + filename + "\"; filename*=UTF-8''" + encodedFilename);

        return ResponseEntity.ok()
                .headers(headers)
                .body(content);
    }

    private ResponseEntity<byte[]> createWordResponse(byte[] content, String filename) {
        String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document"));
        headers.setContentDispositionFormData("attachment", filename);
        headers.set("Content-Disposition", "attachment; filename=\"" + filename + "\"; filename*=UTF-8''" + encodedFilename);

        return ResponseEntity.ok()
                .headers(headers)
                .body(content);
    }
}
