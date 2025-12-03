package com.insurancesystem.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.insurancesystem.Exception.NotFoundException;
import com.insurancesystem.Model.Dto.*;
import com.insurancesystem.Model.Entity.Client;
import com.insurancesystem.Model.Entity.Enums.RoleName;
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

    // ============================================================
    // 🟢 إنشاء مطالبة جديدة (عضو أو طبيب)
    // ============================================================
    @PreAuthorize("hasAnyRole('DOCTOR', 'INSURANCE_CLIENT')")
    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ClaimDTO> createClaim(
            Authentication auth,
            @RequestPart("data") String reqJson,
            @RequestPart(value = "invoiceImage", required = false)
            List<MultipartFile> invoiceImage
    ) throws IOException {

        // البحث عن العضو الحالي
        String username = auth.getName();
        Client client = clientRepo.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Client not found"));

        // تحويل JSON إلى DTO
        CreateClaimDTO dto = objectMapper.readValue(reqJson, CreateClaimDTO.class);

        // إنشاء مطالبة جديدة
        return ResponseEntity.ok(
                claimService.createClaim(client.getId(), dto, invoiceImage)
        );
    }

    // ============================================================
    // 📋 عرض كل المطالبات الخاصة بعضو واحد
    // ============================================================
    @PreAuthorize("hasAnyRole('INSURANCE_CLIENT', 'MEDICAL_ADMIN', 'INSURANCE_MANAGER')")
    @GetMapping("/allClaimForOneMember")
    public ResponseEntity<List<ClaimDTO>> getMemberClaims(Authentication auth) {

        Client client = clientRepo.findByUsername(auth.getName())
                .orElseThrow(() -> new NotFoundException("Client not found"));

        return ResponseEntity.ok(
                claimService.getMemberClaims(client.getId())
        );
    }

    // ============================================================
    // 📋 عرض جميع المطالبات لجميع الأعضاء (للإداريين فقط)
    // ============================================================
    @PreAuthorize("hasAnyRole('INSURANCE_MANAGER', 'MEDICAL_ADMIN')")
    @GetMapping("/allClaimsByManager")
    public ResponseEntity<List<ClaimDTO>> getAllClaims() {
        return ResponseEntity.ok(claimService.getAllClaims());
    }

    // ============================================================
    // 📋 عرض مطالبة واحدة بالتفصيل
    // ============================================================
    @PreAuthorize("hasAnyRole('INSURANCE_CLIENT', 'MEDICAL_ADMIN', 'INSURANCE_MANAGER')")
    @GetMapping("/ByIdClaim/{id}")
    public ResponseEntity<ClaimDTO> getClaim(
            @PathVariable UUID id,
            Authentication auth
    ) {

        String username = auth.getName();
        Client client = clientRepo.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Client not found"));

        // هل المستخدم مدير/ إداري طبي؟
        boolean isManager = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_INSURANCE_MANAGER") ||
                        a.getAuthority().equals("ROLE_MEDICAL_ADMIN"));

        return ResponseEntity.ok(
                claimService.getClaim(id, client.getId(), isManager)
        );
    }

    // ============================================================
    // 🟩 موافقة على مطالبة (طبية أو إدارية)
    // ============================================================
    @PreAuthorize("hasAnyRole('INSURANCE_MANAGER', 'MEDICAL_ADMIN')")
    @PatchMapping("/{id}/approve")
    public ResponseEntity<ClaimDTO> approveClaim(
            @PathVariable UUID id,
            Authentication auth
    ) {

        String username = auth.getName();
        Client user = clientRepo.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found"));

        // الحصول على الدور الحقيقي (Manager أو Medical Admin)
        RoleName role = user.getRoles().stream()
                .map(r -> r.getName())
                .filter(r -> r == RoleName.MEDICAL_ADMIN || r == RoleName.INSURANCE_MANAGER)
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Unauthorized role"));

        return ResponseEntity.ok(
                claimService.approveClaim(id, role, user.getId())
        );
    }

    // ============================================================
    // ❌ رفض مطالبة (طبي أو إداري)
    // ============================================================
    @PreAuthorize("hasAnyRole('INSURANCE_MANAGER', 'MEDICAL_ADMIN')")
    @PatchMapping("/{id}/reject")
    public ResponseEntity<ClaimDTO> rejectClaim(
            @PathVariable UUID id,
            @RequestBody RejectClaimDTO dto,
            Authentication auth
    ) {

        String username = auth.getName();
        Client user = clientRepo.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found"));

        RoleName role = user.getRoles().stream()
                .map(r -> r.getName())
                .filter(r -> r == RoleName.MEDICAL_ADMIN || r == RoleName.INSURANCE_MANAGER)
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Unauthorized role"));

        return ResponseEntity.ok(
                claimService.rejectClaim(id, dto, role, user.getId())
        );
    }

    // ============================================================
    // 🩺 المطالبات بانتظار المراجعة الطبية
    // ============================================================
    @PreAuthorize("hasRole('MEDICAL_ADMIN')")
    @GetMapping("/medical-review")
    public ResponseEntity<List<ClaimDTO>> getClaimsForMedicalReview() {
        return ResponseEntity.ok(claimService.getClaimsForMedicalReview());
    }

    // ============================================================
    // 🧾 المطالبات بانتظار المراجعة الإدارية
    // ============================================================
    @PreAuthorize("hasRole('INSURANCE_MANAGER')")
    @GetMapping("/admin-review")
    public ResponseEntity<List<ClaimDTO>> getClaimsForAdminReview() {
        return ResponseEntity.ok(claimService.getClaimsForAdminReview());
    }
}
