package com.insurancesystem.Services;

import com.insurancesystem.Exception.NotFoundException;
import com.insurancesystem.Model.Dto.*;
import com.insurancesystem.Model.Entity.*;
import com.insurancesystem.Model.Entity.Enums.*;
import com.insurancesystem.Model.MapStruct.ClaimMapper;
import com.insurancesystem.Repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClaimService {

    private final ClaimRepository claimRepo;
    private final ClaimEngineService claimEngineService;
    private final ClientRepository clientRepo;
    private final PolicyRepository policyRepo;
    private final ClaimMapper claimMapper;
    private final NotificationService notificationService;

    private final String UPLOAD_DIR = "uploads/invoices/";


    // ======================================================
    // 🟢 إنشاء مطالبة جديدة — مع منطق التغطية الجديد
    // ======================================================
    public ClaimDTO createClaim(UUID memberId, CreateClaimDTO dto, List<MultipartFile> invoiceImage) {

        Client member = clientRepo.findById(memberId)
                .orElseThrow(() -> new NotFoundException("Member not found"));

        Policy policy = policyRepo.findById(dto.getPolicyId())
                .orElseThrow(() -> new NotFoundException("Policy not found"));

        Claim claim = claimMapper.toEntity(dto);

        claim.setMember(member);
        claim.setPolicy(policy);
        claim.setStatus(ClaimStatus.PENDING);

        boolean emergency = dto.getEmergency() != null && dto.getEmergency();
        claim.setEmergency(emergency);

        // 🟦🔥 CALL INSURANCE ENGINE HERE
        claimEngineService.applyCoverageRules(claim);

        // حفظ الفواتير
        if (invoiceImage != null && !invoiceImage.isEmpty()) {
            List<String> paths = invoiceImage.stream()
                    .map(this::saveInvoice)
                    .toList();
            claim.setInvoiceImagePath(paths);
        }

        claimRepo.save(claim);

        notificationService.sendToRole(
                RoleName.MEDICAL_ADMIN,
                "مطالبة جديدة بانتظار المراجعة الطبية من " + member.getFullName()
        );

        return claimMapper.toDto(claim);
    }


    // ======================================================
    // 🔹 استعلامات
    // ======================================================

    public List<ClaimDTO> getMemberClaims(UUID memberId) {
        Client member = clientRepo.findById(memberId)
                .orElseThrow(() -> new NotFoundException("Member not found"));
        return claimRepo.findByMember(member).stream().map(claimMapper::toDto).toList();
    }

    public List<ClaimDTO> getAllClaims() {
        return claimRepo.findAll().stream().map(claimMapper::toDto).toList();
    }

    public ClaimDTO getClaim(UUID claimId, UUID requesterId, boolean isManager) {
        Claim claim = claimRepo.findById(claimId)
                .orElseThrow(() -> new NotFoundException("Claim not found"));

        if (!isManager && !claim.getMember().getId().equals(requesterId))
            throw new NotFoundException("Claim not found for this member");

        return claimMapper.toDto(claim);
    }



    // ======================================================
    // 🟩 الموافقة على مطالبة
    // ======================================================

    public ClaimDTO approveClaim(UUID claimId, RoleName role, UUID reviewerId) {
        Claim claim = claimRepo.findById(claimId)
                .orElseThrow(() -> new NotFoundException("Claim not found"));

        if (role == RoleName.MEDICAL_ADMIN) {

            if (claim.getStatus() != ClaimStatus.PENDING)
                throw new NotFoundException("Medical review already processed");

            claim.setStatus(ClaimStatus.APPROVED_BY_MEDICAL);
            claim.setMedicalReviewer(clientRepo.findById(reviewerId).orElse(null));
            claim.setMedicalReviewedAt(Instant.now());

            claim.setStatus(ClaimStatus.AWAITING_ADMIN_REVIEW);

            notificationService.sendToRole(
                    RoleName.INSURANCE_MANAGER,
                    "مطالبة جديدة بانتظار المراجعة الإدارية"
            );
        }

        else if (role == RoleName.INSURANCE_MANAGER) {

            if (claim.getStatus() != ClaimStatus.AWAITING_ADMIN_REVIEW &&
                    claim.getStatus() != ClaimStatus.APPROVED_BY_MEDICAL)
                throw new NotFoundException("Medical approval required first");

            claim.setStatus(ClaimStatus.APPROVED);
            claim.setAdminReviewer(clientRepo.findById(reviewerId).orElse(null));
            claim.setAdminReviewedAt(Instant.now());
            claim.setApprovedAt(Instant.now());

            notificationService.sendToUser(
                    claim.getMember().getId(),
                    "تمت الموافقة النهائية على مطالبتك بمبلغ " + claim.getAmount()
            );
        }

        claimRepo.save(claim);
        return claimMapper.toDto(claim);
    }



    // ======================================================
    // ❌ رفض مطالبة
    // ======================================================

    public ClaimDTO rejectClaim(UUID claimId, RejectClaimDTO dto, RoleName role, UUID reviewerId) {
        Claim claim = claimRepo.findById(claimId)
                .orElseThrow(() -> new NotFoundException("Claim not found"));

        if (role == RoleName.MEDICAL_ADMIN) {

            if (claim.getStatus() != ClaimStatus.PENDING)
                throw new NotFoundException("Cannot reject at this stage");

            claim.setStatus(ClaimStatus.REJECTED_BY_MEDICAL);
            claim.setMedicalReviewer(clientRepo.findById(reviewerId).orElse(null));
            claim.setRejectedAt(Instant.now());
            claim.setRejectionReason(dto.getReason());
        }

        else if (role == RoleName.INSURANCE_MANAGER) {

            if (claim.getStatus() != ClaimStatus.AWAITING_ADMIN_REVIEW &&
                    claim.getStatus() != ClaimStatus.APPROVED_BY_MEDICAL)
                throw new NotFoundException("Medical approval required before admin rejection");

            claim.setStatus(ClaimStatus.REJECTED);
            claim.setAdminReviewer(clientRepo.findById(reviewerId).orElse(null));
            claim.setRejectedAt(Instant.now());
            claim.setRejectionReason(dto.getReason());
        }

        notificationService.sendToUser(
                claim.getMember().getId(),
                "تم رفض مطالبتك. السبب: " + dto.getReason()
        );

        claimRepo.save(claim);
        return claimMapper.toDto(claim);
    }



    // ======================================================
    // 🧾 حفظ ملفات الفواتير
    // ======================================================
    private String saveInvoice(MultipartFile file) {
        try {
            Files.createDirectories(Path.of(UPLOAD_DIR));
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path path = Path.of(UPLOAD_DIR + fileName);
            Files.write(path, file.getBytes());
            return "http://localhost:8080/uploads/invoices/" + fileName;
        } catch (IOException e) {
            throw new RuntimeException("Failed to save invoice image", e);
        }
    }



    // ======================================================
    // 🔍 المطالبات التي بانتظار المراجعات
    // ======================================================

    public List<ClaimDTO> getClaimsForMedicalReview() {
        return claimRepo.findPendingMedicalClaims().stream().map(claimMapper::toDto).toList();
    }

    public List<ClaimDTO> getClaimsForAdminReview() {
        return claimRepo.findPendingAdminClaims().stream().map(claimMapper::toDto).toList();
    }
}
