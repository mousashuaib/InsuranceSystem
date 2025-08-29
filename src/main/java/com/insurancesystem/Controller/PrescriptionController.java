package com.insurancesystem.Controller;

import com.insurancesystem.Model.Dto.PrescriptionDTO;
import com.insurancesystem.Services.PrescriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/prescriptions")
@RequiredArgsConstructor
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    // ➕ Doctor ينشئ وصفة
    @PostMapping("/create")
    @PreAuthorize("hasRole('DOCTOR')")
    public PrescriptionDTO create(@RequestBody PrescriptionDTO dto) {
        return prescriptionService.create(dto);
    }

    // 📖 Member يشوف وصفاته
    @GetMapping("/get")
    @PreAuthorize("hasRole('INSURANCE_CLIENT')")
    public List<PrescriptionDTO> getMyPrescriptions() {
        return prescriptionService.getMyPrescriptions();
    }

    // 📖 Pharmacist يشوف الوصفات المعلقة
    @GetMapping("/pending")
    @PreAuthorize("hasRole('PHARMACIST')")
    public List<PrescriptionDTO> getPending() {
        return prescriptionService.getPending();
    }

    // ✅ Pharmacist يوافق
    @PatchMapping("/{id}/verify")
    @PreAuthorize("hasRole('PHARMACIST')")
    public PrescriptionDTO verify(@PathVariable UUID id) {
        return prescriptionService.verify(id);
    }

    // ❌ Pharmacist يرفض
    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasRole('PHARMACIST')")
    public PrescriptionDTO reject(@PathVariable UUID id) {
        return prescriptionService.reject(id);
    }

    // ✏️ Doctor يعدل وصفة
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('DOCTOR')")
    public PrescriptionDTO update(@PathVariable UUID id, @RequestBody PrescriptionDTO dto) {
        return prescriptionService.update(id, dto);
    }

    // ❌ Doctor يحذف وصفة
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('DOCTOR')")
    public void delete(@PathVariable UUID id) {
        prescriptionService.delete(id);
    }

}
