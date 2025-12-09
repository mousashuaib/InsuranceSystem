package com.insurancesystem.Services;

import com.insurancesystem.Exception.NotFoundException;
import com.insurancesystem.Model.Dto.HealthcareProviderClaimDTO;
import com.insurancesystem.Model.Dto.CreateHealthcareProviderClaimDTO;
import com.insurancesystem.Model.Dto.HealthcareProviderClaimMedicalDTO;
import com.insurancesystem.Model.Dto.RejectClaimDTO;
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

    // ✅ إنشاء مطالبة
    public HealthcareProviderClaimDTO createClaim(UUID providerId, CreateHealthcareProviderClaimDTO dto, MultipartFile invoiceImage) {
        Client provider = clientRepo.findById(providerId)
                .orElseThrow(() -> new NotFoundException("Healthcare provider not found"));

        HealthcareProviderClaim claim = claimMapper.toEntity(dto);
        claim.setHealthcareProvider(provider);
        claim.setStatus(ClaimStatus.PENDING_MEDICAL);

        // ✅ حفظ معرف واسم المريض
        if (claim.getClientId() != null) {
            Client client = clientRepo.findById(claim.getClientId()).orElse(null);
            if (client != null) {
                claim.setClientName(client.getFullName());
            }
        }

        if (invoiceImage != null && !invoiceImage.isEmpty()) {
            claim.setInvoiceImagePath(saveDocument(invoiceImage));
        }

        claimRepo.save(claim);

        // إشعار المدير
        notificationService.sendToRole(
                RoleName.MEDICAL_ADMIN,
                "مطالبة جديدة من " + provider.getFullName()
        );

        return claimMapper.toDto(claim);
    }

    public List<HealthcareProviderClaimDTO> getProviderClaims(UUID providerId) {
        Client provider = clientRepo.findById(providerId)
                .orElseThrow(() -> new NotFoundException("Healthcare provider not found"));

        List<HealthcareProviderClaim> claims = claimRepo.findByHealthcareProvider(provider);

        // ✅ ملء clientName من clientId إذا كان فارغ
        for (HealthcareProviderClaim claim : claims) {
            if ((claim.getClientName() == null || claim.getClientName().trim().isEmpty()) && claim.getClientId() != null) {
                Client client = clientRepo.findById(claim.getClientId()).orElse(null);
                if (client != null) {
                    claim.setClientName(client.getFullName());
                    claimRepo.save(claim);
                }
            }
        }

        return claims.stream()
                .map(claimMapper::toDto)
                .toList();
    }

    public List<HealthcareProviderClaimDTO> getAllClaims() {
        List<HealthcareProviderClaim> claims = claimRepo.findAll();

        // ✅ ملء clientName من clientId إذا كان فارغ
        for (HealthcareProviderClaim claim : claims) {
            if ((claim.getClientName() == null || claim.getClientName().trim().isEmpty()) && claim.getClientId() != null) {
                Client client = clientRepo.findById(claim.getClientId()).orElse(null);
                if (client != null) {
                    claim.setClientName(client.getFullName());
                    claimRepo.save(claim);
                }
            }
        }

        return claims.stream()
                .map(claimMapper::toDto)
                .toList();
    }

    public HealthcareProviderClaimDTO getClaim(UUID claimId, UUID requesterId, boolean isManager) {
        HealthcareProviderClaim claim = claimRepo.findById(claimId)
                .orElseThrow(() -> new NotFoundException("Claim not found"));

        if (!isManager && !claim.getHealthcareProvider().getId().equals(requesterId)) {
            throw new NotFoundException("Claim not found for this provider");
        }

        return claimMapper.toDto(claim);
    }

    public HealthcareProviderClaimDTO approveClaim(UUID claimId) {

        HealthcareProviderClaim claim = claimRepo.findById(claimId)
                .orElseThrow(() -> new NotFoundException("Claim not found"));

        if (claim.getStatus() != ClaimStatus.AWAITING_ADMIN_REVIEW)
            throw new NotFoundException("Medical approval required first");

        claim.setStatus(ClaimStatus.APPROVED);
        claim.setApprovedAt(Instant.now());
        claimRepo.save(claim);

        notificationService.sendToUser(
                claim.getHealthcareProvider().getId(),
                "تمت الموافقة على مطالبتك."
        );

        return claimMapper.toDto(claim);
    }


    // رفض مطالبة
    public HealthcareProviderClaimDTO rejectClaim(UUID claimId, RejectClaimDTO dto) {
        HealthcareProviderClaim claim = claimRepo.findById(claimId)
                .orElseThrow(() -> new NotFoundException("Claim not found"));

        claim.setStatus(ClaimStatus.REJECTED);
        claim.setRejectedAt(Instant.now());
        claim.setRejectionReason(dto.getReason());
        claimRepo.save(claim);

        notificationService.sendToUser(
                claim.getHealthcareProvider().getId(),
                "تم رفض مطالبتك. السبب: " + dto.getReason()
        );

        return claimMapper.toDto(claim);
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

    public List<HealthcareProviderClaimMedicalDTO> getClaimsForMedicalReview() {

        List<HealthcareProviderClaim> claims =
                claimRepo.findByStatus(ClaimStatus.PENDING_MEDICAL);

        return claims.stream().map(claim -> {
            HealthcareProviderClaimMedicalDTO dto = claimMapper.toMedicalDto(claim);

            // ⭐ إضافة التشخيص والعلاج (مهم!)
            dto.setDiagnosis(claim.getDiagnosis());
            dto.setTreatmentDetails(claim.getTreatmentDetails());

            // تعبئة clientName
            if (claim.getClientId() != null) {
                clientRepo.findById(claim.getClientId()).ifPresent(client ->
                        dto.setClientName(client.getFullName())
                );
            }

            // تعبئة Provider Role
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



    public HealthcareProviderClaimDTO rejectMedical(UUID claimId, String reason, UUID reviewerId) {
        HealthcareProviderClaim claim = claimRepo.findById(claimId)
                .orElseThrow(() -> new NotFoundException("Claim not found"));

        if (claim.getStatus() != ClaimStatus.PENDING_MEDICAL)
            throw new NotFoundException("Already processed");

        claim.setStatus(ClaimStatus.REJECTED_BY_MEDICAL);
        claim.setRejectedAt(Instant.now());
        claim.setRejectionReason(reason);

        claimRepo.save(claim);

        return claimMapper.toDto(claim);
    }


    public HealthcareProviderClaimDTO approveMedical(UUID claimId, UUID reviewerId) {
        HealthcareProviderClaim claim = claimRepo.findById(claimId)
                .orElseThrow(() -> new NotFoundException("Claim not found"));

        if (claim.getStatus() != ClaimStatus.PENDING_MEDICAL)
            throw new NotFoundException("Already processed");

        claim.setStatus(ClaimStatus.AWAITING_ADMIN_REVIEW);
        claim.setApprovedAt(Instant.now());

        claimRepo.save(claim);

        return claimMapper.toDto(claim);
    }

}

