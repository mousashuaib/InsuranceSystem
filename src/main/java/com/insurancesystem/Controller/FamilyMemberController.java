package com.insurancesystem.Controller;

import com.insurancesystem.Model.Dto.CreateFamilyMemberDTO;
import com.insurancesystem.Model.Dto.FamilyMemberDTO;
import com.insurancesystem.Model.Dto.RejectReasonDTO;
import com.insurancesystem.Model.Dto.UpdateFamilyMemberStatusDTO;
import com.insurancesystem.Services.FamilyMemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/family-members")
@RequiredArgsConstructor
public class FamilyMemberController {

    private final FamilyMemberService familyService;

    /* ===================== CLIENT ===================== */

    @PreAuthorize("hasRole('INSURANCE_CLIENT')")
    @GetMapping("/my")
    public List<FamilyMemberDTO> myFamily(Authentication auth) {
        return familyService.getFamilyForClient(auth.getName());
    }

    @PreAuthorize("hasRole('INSURANCE_CLIENT')")
    @PostMapping(
            value = "/create",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public FamilyMemberDTO create(
            Authentication auth,
            @Valid @RequestPart("data") CreateFamilyMemberDTO dto,
            @RequestPart(value = "documents", required = false) MultipartFile[] documents
    ) {
        return familyService.createFamilyMember(auth.getName(), dto, documents);
    }

    @PreAuthorize("hasRole('INSURANCE_CLIENT')")
    @DeleteMapping("/{memberId}")
    public void delete(Authentication auth, @PathVariable UUID memberId) {
        familyService.deleteFamilyMember(auth.getName(), memberId);
    }

    /* ===================== DOCTOR / COORDINATION_ADMIN - Get Family by Client ID ===================== */


    @PreAuthorize("hasAnyRole('DOCTOR','COORDINATION_ADMIN','INSURANCE_MANAGER','MEDICAL_ADMIN')")

    @GetMapping("/client/{clientId}")
    public List<FamilyMemberDTO> getFamilyByClientId(@PathVariable UUID clientId) {
        return familyService.getFamilyForClient(clientId);
    }

    /* ===================== MEDICAL ADMIN ===================== */

    @PreAuthorize("hasRole('MEDICAL_ADMIN')")
    @PatchMapping("/{memberId}/status")
    public FamilyMemberDTO updateStatus(
            @PathVariable UUID memberId,
            @RequestBody UpdateFamilyMemberStatusDTO dto
    ) {
        return familyService.updateStatus(memberId, dto.getStatus());
    }

    @PreAuthorize("hasAnyRole('COORDINATION_ADMIN','MEDICAL_ADMIN')")
    @GetMapping("/pending")
    public ResponseEntity<List<FamilyMemberDTO>> getPendingFamilyMembers() {
        return ResponseEntity.ok(
                familyService.getPendingFamilyMembers()
        );
    }
    @PreAuthorize("hasAnyRole('COORDINATION_ADMIN','MEDICAL_ADMIN')")
    @PatchMapping("/{id}/approve")
    public ResponseEntity<Void> approveFamilyMember(@PathVariable UUID id) {
        familyService.approve(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('COORDINATION_ADMIN','MEDICAL_ADMIN')")
    @PatchMapping("/{id}/reject")
    public ResponseEntity<Void> rejectFamilyMember(
            @PathVariable UUID id,
            @RequestBody RejectReasonDTO dto
    ) {
        familyService.reject(id, dto.getReason());
        return ResponseEntity.noContent().build();
    }


}

