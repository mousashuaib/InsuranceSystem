package com.insurancesystem.Controller;

import com.insurancesystem.Model.Dto.ClientDto;
import com.insurancesystem.Model.Dto.RadiologyRequestDTO;
import com.insurancesystem.Model.Dto.UpdateUserDTO;
import com.insurancesystem.Services.RadiologyRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/radiology")
@RequiredArgsConstructor
public class RadiologyRequestController {

    private final RadiologyRequestService radiologyService;

    // ➕ Doctor creates a Radiology Request
    @PostMapping("/create")
    @PreAuthorize("hasRole('DOCTOR')")
    public RadiologyRequestDTO create(@RequestBody RadiologyRequestDTO dto) {
        return radiologyService.create(dto);
    }

    // 📖 Radiologist views pending radiology requests
    @GetMapping("/pending")
    @PreAuthorize("hasRole('RADIOLOGIST')")
    public List<RadiologyRequestDTO> getPendingForRadiologist() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();
        UUID radiologistId = radiologyService.getRadiologistIdByEmail(currentUsername);
        return radiologyService.getPendingRequests(radiologistId);
    }

    // 🧪 Radiologist uploads radiology result with test name and price
    @PatchMapping("/{id}/uploadResult")
    @PreAuthorize("hasRole('RADIOLOGIST')")
    public RadiologyRequestDTO uploadRadiologyResult(
            @PathVariable UUID id,
            @RequestParam("file") MultipartFile file,
            @RequestParam("testName") String testName,
            @RequestParam("price") Double enteredPrice) {
        return radiologyService.uploadRadiologyResult(id, file, testName, enteredPrice);
    }

    // 📖 Member or Doctor views the result
    @GetMapping("/{id}/result")
    @PreAuthorize("hasAnyRole('INSURANCE_CLIENT', 'DOCTOR')")
    public RadiologyRequestDTO getResult(@PathVariable UUID id) {
        return radiologyService.getResult(id);
    }

    // ✏️ Doctor updates the radiology request
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('DOCTOR')")
    public RadiologyRequestDTO update(@PathVariable UUID id, @RequestBody RadiologyRequestDTO dto) {
        return radiologyService.update(id, dto);
    }

    // ❌ Doctor deletes a radiology request
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('DOCTOR')")
    public void delete(@PathVariable UUID id) {
        radiologyService.delete(id);
    }

    // 📖 Doctor views all their radiology requests
    @GetMapping("/doctor/my")
    @PreAuthorize("hasRole('DOCTOR')")
    public List<RadiologyRequestDTO> getByDoctor() {
        return radiologyService.getByDoctor();
    }

    // 📊 Radiologist views stats
    @GetMapping("/stats")
    @PreAuthorize("hasRole('RADIOLOGIST')")
    public RadiologyRequestDTO getRadiologyStats() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();
        UUID radiologistId = radiologyService.getRadiologistIdByEmail(currentUsername);
        return radiologyService.getRadiologyStats(radiologistId);
    }

    // 👤 Radiologist updates their profile
    @PatchMapping(value = "/me/update", consumes = "multipart/form-data")
    @PreAuthorize("hasRole('RADIOLOGIST')")
    public ResponseEntity<ClientDto> updateRadiologistProfile(
            Authentication auth,
            @RequestPart("data") @Valid UpdateUserDTO dto,
            @RequestPart(value = "universityCard", required = false) MultipartFile[] universityCard
    ) {
        String email = auth.getName();
        ClientDto updated = radiologyService.updateRadiologistProfile(email, dto, universityCard);

        return ResponseEntity.ok(updated);
    }

    // 📖 Radiologist views all their requests
    @GetMapping("/my-requests")
    @PreAuthorize("hasRole('RADIOLOGIST')")
    public List<RadiologyRequestDTO> getMyRadiologyRequests() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();
        UUID radiologistId = radiologyService.getRadiologistIdByEmail(currentUsername);
        return radiologyService.getAllForCurrentRadiologist(radiologistId);
    }

    // 📖 Member views their radiology requests
    @GetMapping("/getByMember")
    @PreAuthorize("hasRole('INSURANCE_CLIENT')")
    public List<RadiologyRequestDTO> getByMember() {
        return radiologyService.getByMember();
    }

    // 📖 Get all radiologists
    @GetMapping("/radiologists")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','DOCTOR')")
    public List<ClientDto> getAllRadiologists() {
        return radiologyService.getAllRadiologists();
    }
}