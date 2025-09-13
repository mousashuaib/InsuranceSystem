package com.insurancesystem.Controller;

import com.insurancesystem.Model.Dto.ClientDto;
import com.insurancesystem.Model.Dto.MedicalRecordDTO;
import com.insurancesystem.Model.Dto.UpdateUserDTO;
import com.insurancesystem.Services.MedicalRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/medical-records")
@RequiredArgsConstructor
public class MedicalRecordController {

    private final MedicalRecordService recordService;

    // ➕ إضافة سجل (Doctor فقط)
    @PostMapping("/create-medical")
    @PreAuthorize("hasRole('DOCTOR')")
    public MedicalRecordDTO create(@RequestBody MedicalRecordDTO dto) {
        return recordService.createRecord(dto);
    }

    // 📖 جلب جميع السجلات (Doctor أو Admin)
    @GetMapping("getAll")
    @PreAuthorize("hasAnyRole('DOCTOR','INSURANCE_MANAGER')")
    public List<MedicalRecordDTO> getAll() {
        return recordService.getAll();
    }

    // 📖 جلب سجل عضو معين (Member يشوف سجلاته فقط)
    @GetMapping("/Bymember/{memberId}")
    @PreAuthorize("hasAnyRole('DOCTOR','INSURANCE_CLIENT')")
    public List<MedicalRecordDTO> getByMember(@PathVariable UUID memberId) {
        return recordService.getByMember(memberId);
    }

    // 📖 جلب سجل واحد
    @GetMapping("/ByRecordId/{id}")
    @PreAuthorize("hasAnyRole('DOCTOR','INSURANCE_CLIENT')")
    public MedicalRecordDTO getById(@PathVariable UUID id) {
        return recordService.getById(id);
    }

    // ✏️ تحديث سجل (Doctor فقط)
    @PatchMapping("/update/{id}")
    @PreAuthorize("hasRole('DOCTOR')")
    public MedicalRecordDTO update(@PathVariable UUID id, @RequestBody MedicalRecordDTO dto) {
        return recordService.updateRecord(id, dto);
    }

    // ❌ حذف سجل (Doctor فقط)
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('DOCTOR')")
    public void delete(@PathVariable UUID id) {
        recordService.deleteRecord(id);
    }
    @PatchMapping(value = "/me/update", consumes = "multipart/form-data")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<ClientDto> updateDoctorProfile(
            Authentication auth,
            @RequestPart("data") @Valid UpdateUserDTO dto,
            @RequestPart(value = "universityCard", required = false) MultipartFile universityCard
    ) {
        String username = auth.getName();
        ClientDto updated = recordService.updateProfile(username, dto, universityCard);
        return ResponseEntity.ok(updated);
    }
    @GetMapping("/stats")
    @PreAuthorize("hasRole('DOCTOR')")
    public Map<String, Long> getDoctorStats(Authentication auth) {
        String username = auth.getName();
        return recordService.getDoctorStats(username);
    }

}
