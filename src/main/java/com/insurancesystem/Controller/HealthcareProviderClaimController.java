package com.insurancesystem.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.insurancesystem.Exception.NotFoundException;

import com.insurancesystem.Model.Dto.*;

import com.insurancesystem.Model.Entity.Client;

import com.insurancesystem.Model.Entity.Enums.ClaimStatus;
import com.insurancesystem.Model.Entity.Enums.ReportType;
import com.insurancesystem.Repository.ClientRepository;

import com.insurancesystem.Services.HealthcareProviderClaimService;

import lombok.RequiredArgsConstructor;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

import org.springframework.http.MediaType;

import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@RestController

@RequestMapping("/api/healthcare-provider-claims")

@RequiredArgsConstructor

@CrossOrigin(origins = "*")

public class HealthcareProviderClaimController {

    private final HealthcareProviderClaimService claimService;

    private final ClientRepository clientRepo;

    private final ObjectMapper objectMapper;

    // ============================================================

    // Provider: Search Client By Name

    // ============================================================

    @PreAuthorize("hasAnyAuthority('ROLE_DOCTOR','ROLE_PHARMACIST','ROLE_LAB_TECH','ROLE_RADIOLOGIST')")

    @GetMapping("/clients/by-name")

    public ResponseEntity<?> getClientByFullName(@RequestParam String fullName) {

        try {

            Client client = clientRepo.findByFullName(fullName)

                    .orElseThrow(() -> new NotFoundException("Patient not found: " + fullName));

            Map<String, Object> response = new HashMap<>();

            response.put("id", client.getId());

            response.put("fullName", client.getFullName());


            response.put("email", client.getEmail());

            response.put("phone", client.getPhone());

            response.put("status", client.getStatus());

            return ResponseEntity.ok(response);

        } catch (NotFoundException e) {

            return ResponseEntity.status(404).body(Map.of("message", e.getMessage()));

        }

    }

    // ============================================================

    // Provider: Create Claim

    // ============================================================

    @PreAuthorize("hasAnyAuthority('ROLE_DOCTOR','ROLE_PHARMACIST','ROLE_LAB_TECH','ROLE_RADIOLOGIST')")

    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)

    public ResponseEntity<?> createClaim(

            Authentication auth,

            @RequestPart("data") String json,

            @RequestPart(value = "document", required = false) MultipartFile document

    ) throws IOException {

        try {

            Client provider = clientRepo.findByEmail(auth.getName().toLowerCase())
                    .orElseThrow(() -> new NotFoundException("Provider not found"));

            CreateHealthcareProviderClaimDTO dto =

                    objectMapper.readValue(json, CreateHealthcareProviderClaimDTO.class);

            return ResponseEntity.status(HttpStatus.CREATED)

                    .body(claimService.createClaim(provider.getId(), dto, document));

        } catch (Exception e) {

            return ResponseEntity.status(400)

                    .body(Map.of("message", "Failed to create claim: " + e.getMessage()));

        }

    }

    // ============================================================

    // Client: Create Self-Service Claim

    // ============================================================

    @PreAuthorize("hasRole('INSURANCE_CLIENT')")

    @PostMapping(value = "/client/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)

    public ResponseEntity<?> createClientClaim(

            Authentication auth,

            @RequestPart("data") String json,

            @RequestPart(value = "document", required = false) MultipartFile document

    ) throws IOException {

        try {

            Client client = clientRepo.findByEmail(auth.getName().toLowerCase())
                    .orElseThrow(() -> new NotFoundException("Client not found"));


            CreateHealthcareProviderClaimDTO dto =

                    objectMapper.readValue(json, CreateHealthcareProviderClaimDTO.class);

            return ResponseEntity.status(HttpStatus.CREATED)

                    .body(claimService.createClientClaim(client.getId(), dto, document));

        } catch (Exception e) {

            return ResponseEntity.status(400)

                    .body(Map.of("message", "Failed to create claim: " + e.getMessage()));

        }

    }

    // ============================================================

    // Provider: My Claims (including clients)

    // ============================================================

    @PreAuthorize("hasAnyAuthority('ROLE_DOCTOR','ROLE_PHARMACIST','ROLE_LAB_TECH','ROLE_RADIOLOGIST','ROLE_INSURANCE_CLIENT')")

    @GetMapping("/my-claims")

    public ResponseEntity<?> getProviderClaims(Authentication auth) {

        try {

            Client provider = clientRepo.findByEmail(auth.getName().toLowerCase())
                    .orElseThrow(() -> new NotFoundException("Provider not found"));


            return ResponseEntity.ok(claimService.getProviderClaims(provider.getId()));

        } catch (NotFoundException e) {

            return ResponseEntity.status(404).body(Map.of("message", e.getMessage()));

        }

    }

    // ============================================================

    // MANAGER → Get All Claims

    // ============================================================

    @PreAuthorize("hasAuthority('ROLE_COORDINATION_ADMIN')")

    @GetMapping("/all")

    public ResponseEntity<?> getAllClaims() {

        return ResponseEntity.ok(claimService.getAllClaims());

    }

    // ============================================================

    // Get Single Claim (Manager OR Provider)

    // ============================================================

    @PreAuthorize("hasAnyAuthority('ROLE_INSURANCE_MANAGER','ROLE_DOCTOR','ROLE_PHARMACIST','ROLE_LAB_TECH','ROLE_RADIOLOGIST')")

    @GetMapping("/{id}")

    public ResponseEntity<?> getClaim(@PathVariable UUID id, Authentication auth) {

        try {

            Client requester = clientRepo.findByEmail(auth.getName().toLowerCase())
                    .orElseThrow(() -> new NotFoundException("Requester not found"));

            boolean isManager = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_INSURANCE_MANAGER"));

            return ResponseEntity.ok(claimService.getClaim(id, requester.getId(), isManager));


        } catch (NotFoundException e) {

            return ResponseEntity.status(404).body(Map.of("message", e.getMessage()));

        }

    }

    // ============================================================

    // Medical Admin → Review List

    // ============================================================

    @PreAuthorize("hasAuthority('ROLE_MEDICAL_ADMIN')")

    @GetMapping("/medical-review")

    public ResponseEntity<?> medicalReviewList() {

        return ResponseEntity.ok(claimService.getClaimsForMedicalReview());

    }


    @PreAuthorize("hasAuthority('ROLE_COORDINATION_ADMIN')")
    @GetMapping("/final-decisions")
    public ResponseEntity<?> finalDecisions() {

        return ResponseEntity.ok(claimService.getFinalDecisions());

    }

    // ============================================================

    // Medical Admin → Approve

    // ============================================================

    @PreAuthorize("hasAuthority('ROLE_MEDICAL_ADMIN')")

    @PatchMapping("/{id}/approve-medical")

    public ResponseEntity<?> approveMedical(@PathVariable UUID id, Authentication auth) {

        try {

            Client reviewer = clientRepo.findByEmail(auth.getName().toLowerCase())
                    .orElseThrow(() -> new NotFoundException("Reviewer not found"));

            return ResponseEntity.ok(claimService.approveMedical(id, reviewer.getId()));


        } catch (NotFoundException e) {

            return ResponseEntity.status(404).body(Map.of("message", e.getMessage()));

        }

    }

    // ============================================================

    // Medical Admin → Reject

    // ============================================================

    @PreAuthorize("hasAuthority('ROLE_MEDICAL_ADMIN')")

    @PatchMapping("/{id}/reject-medical")

    public ResponseEntity<?> rejectMedical(

            @PathVariable UUID id,

            @RequestBody RejectClaimDTO dto,

            Authentication auth) {

        try {

            Client reviewer = clientRepo.findByEmail(auth.getName().toLowerCase())
                    .orElseThrow(() -> new NotFoundException("Reviewer not found"));

            return ResponseEntity.ok(claimService.rejectMedical(id, dto.getReason(), reviewer.getId()));


        } catch (NotFoundException e) {

            return ResponseEntity.status(404).body(Map.of("message", e.getMessage()));

        }

    }

    @PreAuthorize("hasAnyAuthority('ROLE_COORDINATION_ADMIN','ROLE_INSURANCE_MANAGER')")
    @GetMapping(value = "/reports/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> exportReportsPdf(

            @RequestParam ReportType type,
            @RequestParam(required = false) ClaimStatus status,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate from,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate to
    ) {

        byte[] pdf = claimService.exportReportPdf(
                type,
                status,
                from,
                to
        );

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=report_" + type.name().toLowerCase() + ".pdf"
                )
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }


    @PreAuthorize("hasAuthority('ROLE_COORDINATION_ADMIN')")
    @PostMapping(value = "/admin/create-direct", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createClaimByAdmin(
            Authentication auth,
            @RequestPart("data") String json,
            @RequestPart(value = "document", required = false) MultipartFile document
    ) throws IOException {

        Client admin = clientRepo.findByEmail(auth.getName().toLowerCase())
                .orElseThrow(() -> new NotFoundException("Admin not found"));

        CreateHealthcareProviderClaimDTO dto =
                objectMapper.readValue(json, CreateHealthcareProviderClaimDTO.class);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(claimService.createClaimByCoordinationAdmin(
                        admin.getId(),
                        dto,
                        document
                ));
    }

    @PreAuthorize("hasAuthority('ROLE_COORDINATION_ADMIN')")
    @PatchMapping("/{id}/return-to-medical")
    public ResponseEntity<?> returnToMedical(
            @PathVariable UUID id,
            @RequestBody RejectReasonDTO dto,
            Authentication auth
    ) {
        Client reviewer = clientRepo.findByEmail(auth.getName().toLowerCase())
                .orElseThrow(() -> new NotFoundException("Coordinator not found"));

        return ResponseEntity.ok(
                claimService.returnToMedical(id, dto.getReason(), reviewer.getId())
        );
    }

    @PreAuthorize("hasAuthority('ROLE_COORDINATION_ADMIN')")
    @GetMapping("/coordination-review")
    public ResponseEntity<?> coordinationReviewList() {
        return ResponseEntity.ok(
                claimService.getClaimsForCoordinationReview()
        );
    }

}

