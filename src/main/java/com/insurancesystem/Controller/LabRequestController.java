package com.insurancesystem.Controller;

import com.insurancesystem.Model.Dto.ClientDto;
import com.insurancesystem.Model.Dto.LabRequestDTO;
import com.insurancesystem.Model.Dto.UpdateUserDTO;
import com.insurancesystem.Services.LabRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/labs")
@RequiredArgsConstructor
public class LabRequestController {

    private final LabRequestService labService;

    // ➕ Doctor ينشئ طلب فحص
    @PostMapping("/create")
    @PreAuthorize("hasRole('DOCTOR')")
    public LabRequestDTO create(@RequestBody LabRequestDTO dto) {
        return labService.create(dto);
    }

    // 📖 Member يشوف طلباته
    @GetMapping("/getByMember")
    @PreAuthorize("hasRole('INSURANCE_CLIENT')")
    public List<LabRequestDTO> getMyLabs() {
        return labService.getMyLabs();
    }

    // 📖 Lab Technician يشوف الطلبات المعلقة
    @GetMapping("/pending")
    @PreAuthorize("hasRole('LAB_TECH')")
    public List<LabRequestDTO> getPending() {
        return labService.getPending();
    }

    // 🧪 Lab Technician يرفع النتيجة والسعر
    @PatchMapping("/{id}/upload")
    @PreAuthorize("hasRole('LAB_TECH')")
    public LabRequestDTO uploadResult(@PathVariable UUID id,
                                      @RequestParam("file") MultipartFile file,
                                      @RequestParam("price") Double enteredPrice) {
        return labService.uploadResult(id, file, enteredPrice);
    }

    // 📖 Member أو Doctor يشوف نتيجة
    @GetMapping("/{id}/result")
    @PreAuthorize("hasAnyRole('INSURANCE_CLIENT','DOCTOR')")
    public LabRequestDTO getResult(@PathVariable UUID id) {
        return labService.getResult(id);
    }

    // ✏️ Doctor يعدل طلب
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('DOCTOR')")
    public LabRequestDTO update(@PathVariable UUID id, @RequestBody LabRequestDTO dto) {
        return labService.update(id, dto);
    }

    // ❌ Doctor يحذف طلب
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('DOCTOR')")
    public void delete(@PathVariable UUID id) {
        labService.delete(id);
    }

    @GetMapping("/doctor/my")
    @PreAuthorize("hasRole('DOCTOR')")
    public List<LabRequestDTO> getByDoctor() {
        return labService.getByDoctor();
    }

    // 📊 Lab Technician يشوف إحصائيات الطلبات
    @GetMapping("/stats")
    @PreAuthorize("hasRole('LAB_TECH')")
    public LabRequestDTO getLabStats() {
        return labService.getLabStats();
    }

    // 👤 Lab Worker يحدّث بروفايله
    @PatchMapping(value = "/me/update", consumes = "multipart/form-data")
    @PreAuthorize("hasRole('LAB_TECH')")
    public ResponseEntity<ClientDto> updateLabWorkerProfile(
            Authentication auth,
            @RequestPart("data") @Valid UpdateUserDTO dto,
            @RequestPart(value = "universityCard", required = false) MultipartFile[] universityCard
    ) {
        String username = auth.getName();
        ClientDto updated = labService.updateLabWorkerProfile(username, dto, universityCard);
        return ResponseEntity.ok(updated);
    }


    // 📖 Lab Tech يشوف كل طلباته
    @GetMapping("/my-requests")
    @PreAuthorize("hasRole('LAB_TECH')")
    public List<LabRequestDTO> getMyLabRequests() {
        return labService.getAllForCurrentLab();
    }

    // 📖 جميع الفنيين (Lab Technicians)
    @GetMapping("/labtechs")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','DOCTOR')")
    public List<ClientDto> getAllLabTechs() {
        return labService.getAllLabTechs();
    }

}

