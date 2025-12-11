package com.insurancesystem.Services;

import com.insurancesystem.Exception.NotFoundException;
import com.insurancesystem.Model.Dto.*;
import com.insurancesystem.Model.Entity.HealthcareProviderClaim;
import com.insurancesystem.Model.Entity.Client;
import com.insurancesystem.Model.Entity.Enums.ClaimStatus;
import com.insurancesystem.Model.Entity.Enums.RoleName;
import com.insurancesystem.Model.MapStruct.HealthcareProviderClaimMapper;
import com.insurancesystem.Repository.HealthcareProviderClaimRepository;
import com.insurancesystem.Repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HealthcareProviderClaimService {

    private final HealthcareProviderClaimRepository claimRepo;
    private final ClientRepository clientRepo;
    private final HealthcareProviderClaimMapper claimMapper;
    private final NotificationService notificationService;

    private final String UPLOAD_DIR = "uploads/healthcare-claims/";

    // ============================================================
    // 🟢 إنشاء مطالبة
    // ============================================================
    public HealthcareProviderClaimDTO createClaim(
            UUID providerId,
            CreateHealthcareProviderClaimDTO dto,
            MultipartFile invoiceImage
    ) {
        Client provider = clientRepo.findById(providerId)
                .orElseThrow(() -> new NotFoundException("Healthcare provider not found"));

        HealthcareProviderClaim claim = claimMapper.toEntity(dto);
        claim.setHealthcareProvider(provider);
        claim.setStatus(ClaimStatus.PENDING_MEDICAL);

        // حفظ اسم المريض
        if (claim.getClientId() != null) {
            clientRepo.findById(claim.getClientId())
                    .ifPresent(c -> claim.setClientName(c.getFullName()));
        }

        if (invoiceImage != null && !invoiceImage.isEmpty()) {
            claim.setInvoiceImagePath(saveDocument(invoiceImage));
        }

        claimRepo.save(claim);

        // إشعار الإداري الطبي
        notificationService.sendToRole(
                RoleName.MEDICAL_ADMIN,
                "مطالبة جديدة من " + provider.getFullName()
        );

        return claimMapper.toDto(claim);
    }

    // ============================================================
    // 🔍 Provider claims
    // ============================================================
    public List<HealthcareProviderClaimDTO> getProviderClaims(UUID providerId) {

        Client provider = clientRepo.findById(providerId)
                .orElseThrow(() -> new NotFoundException("Healthcare provider not found"));

        return claimRepo.findByHealthcareProvider(provider)
                .stream()
                .map(claimMapper::toDto)
                .toList();
    }

    // ============================================================
    // 🔍 جميع المطالبات
    // ============================================================
    public List<HealthcareProviderClaimDTO> getAllClaims() {
        return claimRepo.findAll()
                .stream()
                .map(claimMapper::toDto)
                .toList();
    }

    public HealthcareProviderClaimDTO getClaim(UUID id, UUID requesterId, boolean isManager) {
        HealthcareProviderClaim claim = claimRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Claim not found"));

        if (!isManager && !claim.getHealthcareProvider().getId().equals(requesterId))
            throw new NotFoundException("Claim not found for this provider");

        return claimMapper.toDto(claim);
    }

    // ============================================================
    // ❌ رفض طبي
    // ============================================================
    public HealthcareProviderClaimDTO rejectMedical(UUID claimId, String reason, UUID reviewerId) {

        HealthcareProviderClaim claim = claimRepo.findById(claimId)
                .orElseThrow(() -> new NotFoundException("Claim not found"));

        if (claim.getStatus() != ClaimStatus.PENDING_MEDICAL)
            throw new NotFoundException("Claim was already processed");

        claim.setStatus(ClaimStatus.REJECTED_BY_MEDICAL);
        claim.setRejectedAt(Instant.now());
        claim.setRejectionReason(reason);

        claimRepo.save(claim);

        return claimMapper.toDto(claim);
    }

    // ============================================================
    // 🟩 موافقة طبية → تنتقل للإداري
    // ============================================================
    public HealthcareProviderClaimDTO approveMedical(UUID claimId, UUID reviewerId) {

        HealthcareProviderClaim claim = claimRepo.findById(claimId)
                .orElseThrow(() -> new NotFoundException("Claim not found"));

        if (claim.getStatus() != ClaimStatus.PENDING_MEDICAL)
            throw new NotFoundException("Claim already processed");

        // ✔ تم تعيين بيانات المراجع الطبي
        Client reviewer = clientRepo.findById(reviewerId)
                .orElseThrow(() -> new NotFoundException("Reviewer not found"));

        claim.setApprovedAt(Instant.now());

        claim.setMedicalReviewerId(reviewerId);
        claim.setMedicalReviewerName(reviewer.getFullName());
        claim.setMedicalReviewedAt(Instant.now());
        claim.setStatus(ClaimStatus.AWAITING_ADMIN_REVIEW);

        claimRepo.save(claim);

        notificationService.sendToRole(
                RoleName.COORDINATION_ADMIN,
                "مطالبة بانتظار المراجعة الإدارية"
        );

        claimRepo.save(claim);

        return claimMapper.toDto(claim);
    }



    // ============================================================
    // ❌ رفض إداري
    // ============================================================
    public HealthcareProviderClaimDTO rejectAdmin(UUID claimId, String reason, UUID reviewerId) {

        HealthcareProviderClaim claim = claimRepo.findById(claimId)
                .orElseThrow(() -> new NotFoundException("Claim not found"));

        if (claim.getStatus() != ClaimStatus.AWAITING_ADMIN_REVIEW)
            throw new NotFoundException("Claim is not ready for administrative review");

        claim.setStatus(ClaimStatus.REJECTED);
        claim.setRejectedAt(Instant.now());
        claim.setRejectionReason(reason);

        claimRepo.save(claim);

        return claimMapper.toDto(claim);
    }

    // ============================================================
    // 🟩 موافقة إدارية نهائية
    // ============================================================
    public HealthcareProviderClaimDTO approveAdmin(UUID claimId, UUID reviewerId) {

        HealthcareProviderClaim claim = claimRepo.findById(claimId)
                .orElseThrow(() -> new NotFoundException("Claim not found"));

        if (claim.getStatus() != ClaimStatus.AWAITING_ADMIN_REVIEW)
            throw new NotFoundException("Medical approval required first");

        claim.setStatus(ClaimStatus.APPROVED);
        claim.setApprovedAt(Instant.now());

        claimRepo.save(claim);

        return claimMapper.toDto(claim);
    }

    // ============================================================
    // Medical Review List
    // ============================================================
    public List<HealthcareProviderClaimMedicalDTO> getClaimsForMedicalReview() {

        List<HealthcareProviderClaim> claims =
                claimRepo.findByStatus(ClaimStatus.PENDING_MEDICAL);

        return claims.stream().map(claim -> {
            HealthcareProviderClaimMedicalDTO dto = claimMapper.toMedicalDto(claim);

            // ⭐ إضافة التشخيص والعلاج
            dto.setDiagnosis(claim.getDiagnosis());
            dto.setTreatmentDetails(claim.getTreatmentDetails());

            // تعبئة clientName
            // تعبئة clientName + employeeId
            if (claim.getClientId() != null) {
                clientRepo.findById(claim.getClientId()).ifPresent(client -> {
                    dto.setClientName(client.getFullName());
                    dto.setEmployeeId(client.getEmployeeId());   // ⭐ تمت الإضافة هنا
                });
            }


            // ⭐ المهم جداً: أرجع الـ role كما هو بدون أي mapping
            String role = claim.getHealthcareProvider()
                    .getRoles()
                    .stream()
                    .findFirst()
                    .map(r -> r.getName().name())
                    .orElse("UNKNOWN");

            dto.setProviderRole(role);

            return dto;
        }).toList();
    }


    // ============================================================
    // Final Decisions Page
    // ============================================================
    public List<HealthcareProviderClaimMedicalDTO> getFinalDecisions() {

        List<ClaimStatus> statuses = List.of(
                ClaimStatus.APPROVED,
                ClaimStatus.REJECTED,
                ClaimStatus.APPROVED_BY_MEDICAL,
                ClaimStatus.REJECTED_BY_MEDICAL,
                ClaimStatus.AWAITING_ADMIN_REVIEW   // ⭐ تمت الإضافة هنا

        );

        List<HealthcareProviderClaim> claims = claimRepo.findByStatusIn(statuses);

        return claims.stream().map(claim -> {
            HealthcareProviderClaimMedicalDTO dto = claimMapper.toMedicalDto(claim);

            // اسم المريض
            if (claim.getClientId() != null) {
                clientRepo.findById(claim.getClientId())
                        .ifPresent(c -> dto.setClientName(c.getFullName()));
            }

            // Provider Role
            String role = claim.getHealthcareProvider()
                    .getRoles()
                    .stream()
                    .findFirst()
                    .map(r -> r.getName().name())
                    .orElse("UNKNOWN");

            dto.setProviderRole(role);

            return dto;
        }).toList();
    }





    private String saveDocument(MultipartFile file) {
        try {
            Files.createDirectories(Path.of(UPLOAD_DIR));
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path path = Path.of(UPLOAD_DIR + fileName);
            Files.write(path, file.getBytes());
            return "http://localhost:8080/uploads/healthcare-claims/" + fileName;
        } catch (IOException e) {
            throw new RuntimeException("Failed to save document", e);
        }
    }

    // ============================================================
// 🟦 Claims Awaiting Admin Review (Coordination Admin)
// ============================================================
    public List<HealthcareAdminReviewDTO> getClaimsForAdminReview() {

        List<HealthcareProviderClaim> claims =
                claimRepo.findByStatus(ClaimStatus.AWAITING_ADMIN_REVIEW);

        return claims.stream().map(claim -> {

            HealthcareAdminReviewDTO dto = new HealthcareAdminReviewDTO();

            dto.setId(claim.getId().toString());

            // Patient info
            Client patient = clientRepo.findById(claim.getClientId()).orElse(null);
            if (patient != null) {
                dto.setClientName(patient.getFullName());
                dto.setEmployeeId(patient.getEmployeeId());
            }

            // Provider info
            Client provider = claim.getHealthcareProvider();
            dto.setProviderName(provider.getFullName());
            dto.setProviderRole(
                    provider.getRoles().stream()
                            .findFirst()
                            .map(r -> r.getName().name())
                            .orElse("UNKNOWN")
            );

            // Financial info
            dto.setAmount(claim.getAmount());
            dto.setServiceDate(claim.getServiceDate().toString());

            // Admin info
            dto.setStatus(claim.getStatus().name());
            dto.setSubmittedAt(claim.getSubmittedAt().toString());

            // ⭐ Correct medical admin info
            dto.setMedicalReviewerName(claim.getMedicalReviewerName());
            dto.setMedicalReviewedAt(
                    claim.getMedicalReviewedAt() != null
                            ? claim.getMedicalReviewedAt().toString()
                            : null
            );

            dto.setInvoiceImagePath(claim.getInvoiceImagePath());

            return dto;

        }).toList();
    }
    // ============================================================
// 🟩 Claims that are FINAL (approved or rejected by admin)
// ============================================================
    public List<HealthcareAdminReviewDTO> getFinalizedClaims() {

        List<HealthcareProviderClaim> claims =
                claimRepo.findByStatusIn(List.of(
                        ClaimStatus.APPROVED,
                        ClaimStatus.REJECTED
                ));

        return claims.stream().map(claim -> {

            HealthcareAdminReviewDTO dto = new HealthcareAdminReviewDTO();

            dto.setId(claim.getId().toString());

            // Patient info
            Client patient = clientRepo.findById(claim.getClientId()).orElse(null);
            if (patient != null) {
                dto.setClientName(patient.getFullName());
                dto.setEmployeeId(patient.getEmployeeId());
            }

            // Provider info
            Client provider = claim.getHealthcareProvider();
            dto.setProviderName(provider.getFullName());
            dto.setProviderRole(
                    provider.getRoles().stream()
                            .findFirst()
                            .map(r -> r.getName().name())
                            .orElse("UNKNOWN")
            );

            // Financial + dates
            dto.setAmount(claim.getAmount());
            dto.setServiceDate(claim.getServiceDate().toString());
            dto.setStatus(claim.getStatus().name());

            dto.setSubmittedAt(claim.getSubmittedAt().toString());
            dto.setApprovedAt(claim.getApprovedAt() != null ? claim.getApprovedAt().toString() : null);
            dto.setRejectedAt(claim.getRejectedAt() != null ? claim.getRejectedAt().toString() : null);
            dto.setRejectionReason(claim.getRejectionReason());

            dto.setMedicalReviewerName(claim.getMedicalReviewerName());
            dto.setMedicalReviewedAt(
                    claim.getMedicalReviewedAt() != null
                            ? claim.getMedicalReviewedAt().toString()
                            : null
            );

            dto.setInvoiceImagePath(claim.getInvoiceImagePath());

            return dto;

        }).toList();
    }


}
