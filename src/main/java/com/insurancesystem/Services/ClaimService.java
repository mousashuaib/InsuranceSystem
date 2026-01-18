package com.insurancesystem.Services;

import com.insurancesystem.Exception.NotFoundException;
import com.insurancesystem.Model.Dto.ClaimDTO;
import com.insurancesystem.Model.Dto.CreateClaimDTO;
import com.insurancesystem.Model.Dto.RejectClaimDTO;
import com.insurancesystem.Model.Entity.Claim;
import com.insurancesystem.Model.Entity.Client;
import com.insurancesystem.Model.Entity.Enums.ClaimStatus;
import com.insurancesystem.Model.Entity.Enums.RoleName;
import com.insurancesystem.Model.Entity.Policy;
import com.insurancesystem.Model.MapStruct.ClaimMapper;
import com.insurancesystem.Repository.ClaimRepository;
import com.insurancesystem.Repository.ClientRepository;
import com.insurancesystem.Repository.PolicyRepository;
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
public class ClaimService {

    private final ClaimRepository claimRepo;
    private final ClientRepository clientRepo;
    private final PolicyRepository policyRepo;
    private final ClaimMapper claimMapper;
    private final NotificationService notificationService;

    private final String UPLOAD_DIR = "uploads/invoices/";

    // إنشاء مطالبة
    public ClaimDTO createClaim(UUID memberId, CreateClaimDTO dto, MultipartFile invoiceImage) {
        Client member = clientRepo.findById(memberId)
                .orElseThrow(() -> new NotFoundException("Member not found"));

        Policy policy = policyRepo.findById(dto.getPolicyId())
                .orElseThrow(() -> new NotFoundException("Policy not found"));

        Claim claim = claimMapper.toEntity(dto);
        claim.setMember(member);
        claim.setPolicy(policy);
        claim.setStatus(ClaimStatus.PENDING);

        if (invoiceImage != null && !invoiceImage.isEmpty()) {
            claim.setInvoiceImagePath(saveInvoice(invoiceImage));
        }

        claimRepo.save(claim);

        notificationService.sendToRole(
                RoleName.INSURANCE_MANAGER,
                "مطالبة جديدة من " + member.getFullName() +
                        " بمبلغ " + dto.getAmount()
        );

        return claimMapper.toDto(claim);
    }

    // جميع المطالبات لعضو معين
    public List<ClaimDTO> getMemberClaims(UUID memberId) {
        Client member = clientRepo.findById(memberId)
                .orElseThrow(() -> new NotFoundException("Member not found"));
        return claimRepo.findByMember(member).stream()
                .map(claimMapper::toDto)
                .toList();
    }

    // جميع المطالبات للمدير
    public List<ClaimDTO> getAllClaims() {
        return claimRepo.findAll().stream()
                .map(claimMapper::toDto)
                .toList();
    }

    // مطالبة حسب ID (للعضو أو المدير)
    public ClaimDTO getClaim(UUID claimId, UUID requesterId, boolean isManager) {
        Claim claim = claimRepo.findById(claimId)
                .orElseThrow(() -> new NotFoundException("Claim not found"));

        if (!isManager && !claim.getMember().getId().equals(requesterId)) {
            throw new NotFoundException("Claim not found for this member");
        }

        return claimMapper.toDto(claim);
    }

    // موافقة على مطالبة
    public ClaimDTO approveClaim(UUID claimId) {
        Claim claim = claimRepo.findById(claimId)
                .orElseThrow(() -> new NotFoundException("Claim not found"));

        claim.setStatus(ClaimStatus.APPROVED);
        claim.setApprovedAt(Instant.now());
        claimRepo.save(claim);

        notificationService.sendToUser(
                claim.getMember().getId(),
                "تمت الموافقة على مطالبتك بمبلغ " + claim.getAmount()
        );
        notificationService.markNotificationAsReadByMessage(
                RoleName.INSURANCE_MANAGER,
                "مطالبة جديدة من " + claim.getMember().getFullName() + " بمبلغ " + claim.getAmount()
        );

        return claimMapper.toDto(claim);
    }

    // رفض مطالبة
    public ClaimDTO rejectClaim(UUID claimId, RejectClaimDTO dto) {
        Claim claim = claimRepo.findById(claimId)
                .orElseThrow(() -> new NotFoundException("Claim not found"));

        claim.setStatus(ClaimStatus.REJECTED);
        claim.setRejectedAt(Instant.now());
        claim.setRejectionReason(dto.getReason());
        claimRepo.save(claim);

        notificationService.sendToUser(
                claim.getMember().getId(),
                "تم رفض مطالبتك. السبب: " + dto.getReason()
        );
        notificationService.markNotificationAsReadByMessage(
                RoleName.INSURANCE_MANAGER,
                "مطالبة جديدة من " + claim.getMember().getFullName() + " بمبلغ " + claim.getAmount()
        );
        return claimMapper.toDto(claim);
    }

    // حفظ الفاتورة
    private String saveInvoice(MultipartFile file) {
        try {
            Files.createDirectories(Path.of(UPLOAD_DIR));
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path path = Path.of(UPLOAD_DIR + fileName);
            Files.write(path, file.getBytes());
            return path.toString();
        } catch (IOException e) {
            throw new RuntimeException("Failed to save invoice image", e);
        }
    }
}
