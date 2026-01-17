package com.insurancesystem.Controller;

import com.insurancesystem.Exception.NotFoundException;
import com.insurancesystem.Model.Dto.ClientDto;
import com.insurancesystem.Model.Dto.PrescriptionDTO;
import com.insurancesystem.Model.Dto.PrescriptionItemDTO;
import com.insurancesystem.Model.Dto.UpdateUserDTO;
import com.insurancesystem.Services.PrescriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/prescriptions")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    // ➕ Doctor ينشئ وصفة (مع أدوية متعددة)
    @PostMapping("/create")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<?> create(@RequestBody @Valid PrescriptionDTO dto) {
        try {
            return ResponseEntity.ok(prescriptionService.create(dto));
        } catch (NotFoundException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (IllegalArgumentException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (IllegalStateException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage() != null ? e.getMessage() : "Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // 📖 Member يشوف وصفاته
    @GetMapping("/get")
    @PreAuthorize("hasRole('INSURANCE_CLIENT')")
    public ResponseEntity<?> getMyPrescriptions() {
        try {
            return ResponseEntity.ok(prescriptionService.getMyPrescriptions());
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // 📖 Pharmacist يشوف الوصفات المعلقة
    @GetMapping("/pending")
    @PreAuthorize("hasRole('PHARMACIST')")
    public ResponseEntity<?> getPending() {
        try {
            return ResponseEntity.ok(prescriptionService.getPending());
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ✅ Pharmacist يوافق (مع إدخال الأسعار)
    @PatchMapping("/{id}/verify")
    @PreAuthorize("hasRole('PHARMACIST')")
    public ResponseEntity<?> verify(
            @PathVariable UUID id,
            @RequestBody List<PrescriptionItemDTO> itemsWithPrices
    ) {
        try {
            return ResponseEntity.ok(prescriptionService.verify(id, itemsWithPrices));
        } catch (NotFoundException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (IllegalArgumentException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ❌ Pharmacist يرفض
    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasRole('PHARMACIST')")
    public ResponseEntity<?> reject(@PathVariable UUID id) {
        try {
            return ResponseEntity.ok(prescriptionService.reject(id));
        } catch (NotFoundException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // 📖 Doctor يشوف وصفاته
    @GetMapping("/doctor/my")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<?> getByDoctor() {
        try {
            return ResponseEntity.ok(prescriptionService.getByDoctor());
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ✏️ Doctor يعدل وصفة (فقط PENDING)
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<?> update(
            @PathVariable UUID id,
            @RequestBody @Valid PrescriptionDTO dto
    ) {
        try {
            return ResponseEntity.ok(prescriptionService.update(id, dto));
        } catch (NotFoundException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (IllegalStateException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ❌ Doctor يحذف وصفة (فقط PENDING)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<?> delete(@PathVariable UUID id) {
        try {
            prescriptionService.delete(id);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Prescription deleted successfully");
            return ResponseEntity.ok(response);
        } catch (NotFoundException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (IllegalStateException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // 📊 Doctor stats
    @GetMapping("/doctor/stats")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<?> getDoctorStats() {
        try {
            return ResponseEntity.ok(prescriptionService.getDoctorStats());
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // 📊 Pharmacist stats
    @GetMapping("/pharmacist/stats")
    @PreAuthorize("hasRole('PHARMACIST')")
    public ResponseEntity<?> getPharmacistStats() {
        try {
            return ResponseEntity.ok(prescriptionService.getPharmacistStats());
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PatchMapping(value = "/pharmacist/me/update", consumes = "multipart/form-data")
    @PreAuthorize("hasRole('PHARMACIST')")
    public ResponseEntity<?> updatePharmacistProfile(
            Authentication auth,
            @RequestPart("data") @Valid UpdateUserDTO dto,
            @RequestPart(value = "universityCard", required = false) MultipartFile[] universityCard
    ) {
        try {
            String username = auth.getName();
            ClientDto updated = prescriptionService.updatePharmacistProfile(username, dto, universityCard);
            return ResponseEntity.ok(updated);
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    // 📖 Pharmacist يشوف كل وصفاته
    @GetMapping("/all")
    @PreAuthorize("hasRole('PHARMACIST')")
    public ResponseEntity<?> getAll() {
        try {
            return ResponseEntity.ok(prescriptionService.getAllForCurrentPharmacist());
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // 📖 جميع الصيادلة
    @GetMapping("/pharmacists")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','DOCTOR')")
    public ResponseEntity<?> getAllPharmacists() {
        try {
            return ResponseEntity.ok(prescriptionService.getAllPharmacists());
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ✅ التحقق من الوصفات النشطة
    @GetMapping("/check-active/{memberName}/{medicineId}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<?> checkActivePrescription(
            @PathVariable String memberName,
            @PathVariable UUID medicineId
    ) {
        try {
            return ResponseEntity.ok(prescriptionService.checkActivePrescription(memberName, medicineId));
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    @PatchMapping("/{id}/bill")
    @PreAuthorize("hasRole('PHARMACIST')")
    public ResponseEntity<?> bill(@PathVariable UUID id) {
        try {
            return ResponseEntity.ok(prescriptionService.bill(id));
        } catch (NotFoundException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (IllegalStateException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
