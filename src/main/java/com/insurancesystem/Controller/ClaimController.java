package com.insurancesystem.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.insurancesystem.Exception.NotFoundException;
import com.insurancesystem.Model.Dto.ClaimDTO;
import com.insurancesystem.Model.Dto.CreateClaimDTO;
import com.insurancesystem.Model.Dto.RejectClaimDTO;
import com.insurancesystem.Model.Entity.Client;
import com.insurancesystem.Repository.ClientRepository;
import com.insurancesystem.Services.ClaimService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/claims")
@RequiredArgsConstructor
public class ClaimController {

    private final ClaimService claimService;
    private final ClientRepository clientRepo;
    private final ObjectMapper objectMapper;

    @PreAuthorize("hasRole('INSURANCE_CLIENT')")
    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ClaimDTO> createClaim(Authentication auth, @RequestPart("data") String reqJson,
            @RequestPart(value = "invoiceImage", required = false) MultipartFile invoiceImage
    ) throws IOException {

        // جلب المستخدم الحالي من الـ Authentication
        String username = auth.getName();
        Client client = clientRepo.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Client not found"));

        // تحويل JSON إلى DTO
        CreateClaimDTO dto = objectMapper.readValue(reqJson, CreateClaimDTO.class);

        return ResponseEntity.ok(claimService.createClaim(client.getId(), dto, invoiceImage));
    }

    @PreAuthorize("hasRole('INSURANCE_CLIENT')")
    @GetMapping("/allClaimForOneMember")
    public ResponseEntity<List<ClaimDTO>> getMemberClaims(Authentication auth) {
        String username = auth.getName();
        Client client = clientRepo.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Client not found"));
        return ResponseEntity.ok(claimService.getMemberClaims(client.getId()));
    }

    @PreAuthorize("hasRole('INSURANCE_MANAGER')")
    @GetMapping("/allClaimsByManager")
    public ResponseEntity<List<ClaimDTO>> getAllClaims() {
        return ResponseEntity.ok(claimService.getAllClaims());
    }

    @PreAuthorize("hasAnyRole('INSURANCE_MANAGER', 'INSURANCE_CLIENT')")
    @GetMapping("/ByIdClaim/{id}")
    public ResponseEntity<ClaimDTO> getClaim(@PathVariable UUID id, Authentication auth) {
        String username = auth.getName();
        Client client = clientRepo.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Client not found"));
        boolean isManager = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_INSURANCE_MANAGER"));
        return ResponseEntity.ok(claimService.getClaim(id, client.getId(), isManager));
    }

    @PreAuthorize("hasRole('INSURANCE_MANAGER')")
    @PatchMapping("/{id}/approve")
    public ResponseEntity<ClaimDTO> approveClaim(@PathVariable UUID id) {
        return ResponseEntity.ok(claimService.approveClaim(id));
    }

    @PreAuthorize("hasRole('INSURANCE_MANAGER')")
    @PatchMapping("/{id}/reject")
    public ResponseEntity<ClaimDTO> rejectClaim(@PathVariable UUID id, @RequestBody RejectClaimDTO dto) {
        return ResponseEntity.ok(claimService.rejectClaim(id, dto));
    }
}
