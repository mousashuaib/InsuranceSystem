package com.insurancesystem.Controller;

import com.insurancesystem.Model.Dto.LabRequestDTO;
import com.insurancesystem.Services.LabRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
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

    // 🧪 Lab يرفع النتيجة (ملف حقيقي)
    @PatchMapping("/{id}/upload")
    @PreAuthorize("hasRole('LAB_TECH')")
    public LabRequestDTO uploadResult(@PathVariable UUID id,
                                      @RequestParam("file") MultipartFile file) {
        return labService.uploadResult(id, file);
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
    // 📊 Lab Technician stats
    @GetMapping("/stats")
    @PreAuthorize("hasRole('LAB_TECH')")
    public LabRequestDTO getLabStats() {
        return labService.getLabStats();
    }


}
