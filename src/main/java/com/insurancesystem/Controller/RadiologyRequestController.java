package com.insurancesystem.Controller;

import com.insurancesystem.Model.Dto.ClientDto;
import com.insurancesystem.Model.Dto.RadiologyRequestDTO;
import com.insurancesystem.Model.Dto.UpdateUserDTO;
import com.insurancesystem.Services.RadiologyRequestService;  // Ensure correct service is injected
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

    private final RadiologyRequestService radiologyService;  // Inject the service

    // ➕ Doctor creates a Radiology Request
    @PostMapping("/create")
    @PreAuthorize("hasRole('DOCTOR')")
    public RadiologyRequestDTO create(@RequestBody RadiologyRequestDTO dto) {
        return radiologyService.create(dto);  // Call the service to create the radiology request
    }

    // 📖 Radiologist views pending radiology requests
    @GetMapping("/pending")
    @PreAuthorize("hasRole('RADIOLOGIST')")
    public List<RadiologyRequestDTO> getPendingForRadiologist() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();  // Get the authenticated username
        UUID radiologistId = radiologyService.getRadiologistIdByUsername(currentUsername);  // Fetch the Radiologist ID
        return radiologyService.getPendingRequests(radiologistId);  // Fetch pending requests for that radiologist
    }

    // 🧪 Radiologist uploads radiology result
    @PatchMapping("/{id}/uploadResult")
    @PreAuthorize("hasRole('RADIOLOGIST')")
    public RadiologyRequestDTO uploadRadiologyResult(@PathVariable UUID id,
                                                     @RequestParam("file") MultipartFile file) {
        return radiologyService.uploadRadiologyResult(id, file);  // Handle file upload and associate with the request
    }

    // 📖 Member or Doctor views the result of a radiology request
    @GetMapping("/{id}/result")
    @PreAuthorize("hasAnyRole('INSURANCE_CLIENT', 'DOCTOR')")
    public RadiologyRequestDTO getResult(@PathVariable UUID id) {
        return radiologyService.getResult(id);  // Fetch the result for the given request
    }

    // ✏️ Doctor updates the radiology request
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('DOCTOR')")
    public RadiologyRequestDTO update(@PathVariable UUID id, @RequestBody RadiologyRequestDTO dto) {
        return radiologyService.update(id, dto);  // Update the radiology request details
    }

    // ❌ Doctor deletes a radiology request
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('DOCTOR')")
    public void delete(@PathVariable UUID id) {
        radiologyService.delete(id);  // Delete the radiology request
    }

    // 📖 Doctor views all their radiology requests
    @GetMapping("/doctor/my")
    @PreAuthorize("hasRole('DOCTOR')")
    public List<RadiologyRequestDTO> getByDoctor() {
        return radiologyService.getByDoctor();  // Get all radiology requests created by the authenticated doctor
    }

    // 📊 Radiologist views stats of radiology requests
    @GetMapping("/stats")
    @PreAuthorize("hasRole('RADIOLOGIST')")
    public RadiologyRequestDTO getRadiologyStats() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();  // Get the authenticated username
        UUID radiologistId = radiologyService.getRadiologistIdByUsername(currentUsername);  // Fetch the Radiologist ID
        return radiologyService.getRadiologyStats(radiologistId);  // Fetch stats for the radiologist
    }

    // 👤 Radiologist updates their profile
    @PatchMapping(value = "/me/update", consumes = "multipart/form-data")
    @PreAuthorize("hasRole('RADIOLOGIST')")
    public ResponseEntity<ClientDto> updateRadiologistProfile(
            Authentication auth,
            @RequestPart("data") @Valid UpdateUserDTO dto,
            @RequestPart(value = "universityCard", required = false) MultipartFile universityCard
    ) {
        String username = auth.getName();  // Get the authenticated username
        ClientDto updated = radiologyService.updateRadiologistProfile(username, dto, universityCard);  // Update the profile
        return ResponseEntity.ok(updated);  // Return the updated client information
    }

    // 📖 Radiologist views all their radiology requests (pending + completed)
    @GetMapping("/my-requests")
    @PreAuthorize("hasRole('RADIOLOGIST')")
    public List<RadiologyRequestDTO> getMyRadiologyRequests() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();  // Get the authenticated username
        UUID radiologistId = radiologyService.getRadiologistIdByUsername(currentUsername);  // Fetch the Radiologist ID
        return radiologyService.getAllForCurrentRadiologist(radiologistId);  // Get all requests for the radiologist
    }

    // 📖 Get all radiologists
    @GetMapping("/radiologists")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','DOCTOR')")
    public List<ClientDto> getAllRadiologists() {
        return radiologyService.getAllRadiologists();  // Get a list of all radiologists (admin, manager, doctor roles can access)
    }
}
