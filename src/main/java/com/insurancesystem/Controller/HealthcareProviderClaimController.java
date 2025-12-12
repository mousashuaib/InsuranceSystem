package com.insurancesystem.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.insurancesystem.Exception.NotFoundException;
import com.insurancesystem.Model.Dto.HealthcareProviderClaimDTO;
import com.insurancesystem.Model.Dto.CreateHealthcareProviderClaimDTO;
import com.insurancesystem.Model.Dto.HealthcareProviderClaimMedicalDTO;
import com.insurancesystem.Model.Dto.RejectClaimDTO;
import com.insurancesystem.Model.Entity.Client;
import com.insurancesystem.Repository.ClientRepository;
import com.insurancesystem.Services.HealthcareProviderClaimService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/healthcare-provider-claims")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class HealthcareProviderClaimController {

    private final HealthcareProviderClaimService claimService;
    private final ClientRepository clientRepo;
    private final ObjectMapper objectMapper;

    // ✅ البحث عن مريض بـ fullName
    @GetMapping("/clients/by-name")
    @PreAuthorize("hasAnyAuthority('ROLE_DOCTOR', 'ROLE_PHARMACIST', 'ROLE_LAB_TECH', 'ROLE_RADIOLOGIST')")
    public ResponseEntity<?> getClientByFullName(@RequestParam String fullName) {
        try {
            Client client = clientRepo.findByFullName(fullName)
                    .orElseThrow(() -> new NotFoundException("Patient not found with name: " + fullName));

            // ✅ إرجاع بيانات المريض كـ JSON
            Map<String, Object> response = new HashMap<>();
            response.put("id", client.getId());
            response.put("fullName", client.getFullName());
            response.put("username", client.getUsername());
            response.put("email", client.getEmail());
            response.put("phone", client.getPhone());
            response.put("status", client.getStatus());

            return ResponseEntity.ok(response);
        } catch (NotFoundException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.status(404).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Error finding patient: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    // ✅ إنشاء مطالبة - للـ Healthcare Providers فقط
    @PreAuthorize("hasAnyAuthority('ROLE_DOCTOR', 'ROLE_PHARMACIST', 'ROLE_LAB_TECH', 'ROLE_RADIOLOGIST')")
    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<HealthcareProviderClaimDTO> createClaim(
            Authentication auth,
            @RequestPart("data") String reqJson,
            @RequestPart(value = "document", required = false) MultipartFile document
    ) throws IOException {
        try {
            String username = auth.getName();
            Client provider = clientRepo.findByUsername(username)
                    .orElseThrow(() -> new NotFoundException("Healthcare provider not found"));

            CreateHealthcareProviderClaimDTO dto = objectMapper.readValue(reqJson, CreateHealthcareProviderClaimDTO.class);

            HealthcareProviderClaimDTO result = claimService.createClaim(provider.getId(), dto, document);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (NotFoundException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.status(404).body(null);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage() != null ? e.getMessage() : "Failed to create claim");
            return ResponseEntity.status(400).body(null);
        }
    }

    // ✅ جلب مطالبات الـ Provider نفسه
    @PreAuthorize("hasAnyAuthority('ROLE_DOCTOR', 'ROLE_PHARMACIST', 'ROLE_LAB_TECH', 'ROLE_RADIOLOGIST')")
    @GetMapping("/my-claims")
    public ResponseEntity<List<HealthcareProviderClaimDTO>> getProviderClaims(Authentication auth) {
        try {
            String username = auth.getName();
            Client provider = clientRepo.findByUsername(username)
                    .orElseThrow(() -> new NotFoundException("Healthcare provider not found"));

            return ResponseEntity.ok(claimService.getProviderClaims(provider.getId()));
        } catch (NotFoundException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.status(404).body(null);
        }
    }

    // ✅ جلب جميع المطالبات - للمدير فقط
    @PreAuthorize("hasAuthority('ROLE_INSURANCE_MANAGER')")
    @GetMapping("/all")
    public ResponseEntity<List<HealthcareProviderClaimDTO>> getAllClaims() {
        return ResponseEntity.ok(claimService.getAllClaims());
    }

    // ✅ جلب مطالبة واحدة
    @PreAuthorize("hasAnyAuthority('ROLE_INSURANCE_MANAGER', 'ROLE_DOCTOR', 'ROLE_PHARMACIST', 'ROLE_LAB_TECH', 'ROLE_RADIOLOGIST')")
    @GetMapping("/{id}")
    public ResponseEntity<HealthcareProviderClaimDTO> getClaim(@PathVariable UUID id, Authentication auth) {
        try {
            String username = auth.getName();
            Client provider = clientRepo.findByUsername(username)
                    .orElseThrow(() -> new NotFoundException("Healthcare provider not found"));

            boolean isManager = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_INSURANCE_MANAGER"));

            return ResponseEntity.ok(claimService.getClaim(id, provider.getId(), isManager));
        } catch (NotFoundException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.status(404).body(null);
        }
    }

    // ✅ موافقة على المطالبة - للمدير فقط
    @PreAuthorize("hasAuthority('ROLE_INSURANCE_MANAGER')")
    @PatchMapping("/{id}/approve")
    public ResponseEntity<HealthcareProviderClaimDTO> approveClaim(@PathVariable UUID id) {
        try {
            return ResponseEntity.ok(claimService.approveClaim(id));
        } catch (NotFoundException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.status(404).body(null);
        }
    }

    // ✅ رفض المطالبة - للمدير فقط
    @PreAuthorize("hasAuthority('ROLE_INSURANCE_MANAGER')")
    @PatchMapping("/{id}/reject")
    public ResponseEntity<HealthcareProviderClaimDTO> rejectClaim(
            @PathVariable UUID id,
            @RequestBody RejectClaimDTO dto
    ) {
        try {
            return ResponseEntity.ok(claimService.rejectClaim(id, dto));
        } catch (NotFoundException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.status(404).body(null);
        }
    }
    @PreAuthorize("hasAuthority('ROLE_MEDICAL_ADMIN')")
    @PatchMapping("/{id}/reject-medical")
    public ResponseEntity<HealthcareProviderClaimDTO> rejectMedical(
            @PathVariable UUID id,
            @RequestBody RejectClaimDTO dto,
            Authentication auth) {

        Client reviewer = clientRepo.findByUsername(auth.getName())
                .orElseThrow(() -> new NotFoundException("Reviewer not found"));

        return ResponseEntity.ok(
                claimService.rejectMedical(id, dto.getReason(), reviewer.getId())
        );
    }


    @PreAuthorize("hasAuthority('ROLE_MEDICAL_ADMIN')")
    @GetMapping("/medical-review")
    public ResponseEntity<List<HealthcareProviderClaimMedicalDTO>> getMedicalReviewClaims() {
        return ResponseEntity.ok(claimService.getClaimsForMedicalReview());
    }


    @PreAuthorize("hasAuthority('ROLE_MEDICAL_ADMIN')")
    @PatchMapping("/{id}/approve-medical")
    public ResponseEntity<HealthcareProviderClaimDTO> approveMedical(
            @PathVariable UUID id,
            Authentication auth) {

        Client reviewer = clientRepo.findByUsername(auth.getName())
                .orElseThrow(() -> new NotFoundException("Reviewer not found"));

        return ResponseEntity.ok(claimService.approveMedical(id, reviewer.getId()));
    }



}