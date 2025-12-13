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
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
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
        log.info("🔹 Creating healthcare provider claim...");

        Client provider = clientRepo.findById(providerId)
                .orElseThrow(() -> new NotFoundException("Healthcare provider not found"));

        HealthcareProviderClaim claim = claimMapper.toEntity(dto);
        claim.setHealthcareProvider(provider);
        claim.setStatus(ClaimStatus.PENDING_MEDICAL);

        // حفظ اسم المريض
        Client patient = null;
        if (claim.getClientId() != null) {
            patient = clientRepo.findById(claim.getClientId())
                    .map(c -> {
                        claim.setClientName(c.getFullName());
                        return c;
                    })
                    .orElse(null);
        }

        // Create final reference for use in lambdas
        final Client finalPatient = patient;
        final String patientName = finalPatient != null ? finalPatient.getFullName() : null;

        if (invoiceImage != null && !invoiceImage.isEmpty()) {
            claim.setInvoiceImagePath(saveDocument(invoiceImage));
        }

        HealthcareProviderClaim savedClaim = claimRepo.save(claim);

        // 🔔 إشعار للإداريين الطبيين (جميع الإداريين الطبيين)
        clientRepo.findByRoles_Name(RoleName.MEDICAL_ADMIN)
                .forEach(medicalAdmin -> notificationService.sendToUser(
                        medicalAdmin.getId(),
                        "📋 مطالبة جديدة من " + provider.getFullName() +
                                (patientName != null ? " للمريض " + patientName : "") +
                                " - المبلغ: " + claim.getAmount() + " دينار"
                ));

        // 🔔 إشعار لمقدم الخدمة (Provider)
        notificationService.sendToUser(
                provider.getId(),
                "✅ تم إرسال مطالبتك بنجاح - المبلغ: " + claim.getAmount() + " دينار" +
                        (patientName != null ? " للمريض " + patientName : "") +
                        " - في انتظار المراجعة الطبية"
        );

        // 🔔 إشعار للمريض (إن وجد)
        if (finalPatient != null) {
            notificationService.sendToUser(
                    finalPatient.getId(),
                    "📋 تم إنشاء مطالبة طبية لك من " + provider.getFullName() +
                            " - المبلغ: " + claim.getAmount() + " دينار" +
                            " - في انتظار المراجعة"
            );
        }

        return claimMapper.toDto(savedClaim);
    }

    // ============================================================
    // 🟢 إنشاء مطالبة من قبل العميل (Client Self-Service)
    // ============================================================
    public HealthcareProviderClaimDTO createClientClaim(
            UUID clientId,
            CreateHealthcareProviderClaimDTO dto,
            MultipartFile invoiceImage
    ) {
        log.info("🔹 Creating client self-service claim...");

        Client client = clientRepo.findById(clientId)
                .orElseThrow(() -> new NotFoundException("Client not found"));

        HealthcareProviderClaim claim = claimMapper.toEntity(dto);
        // Set the client as both provider and patient (self-service claim)
        claim.setHealthcareProvider(client);
        claim.setClientId(clientId);
        claim.setClientName(client.getFullName());
        claim.setStatus(ClaimStatus.PENDING_MEDICAL);

        if (invoiceImage != null && !invoiceImage.isEmpty()) {
            claim.setInvoiceImagePath(saveDocument(invoiceImage));
        }

        HealthcareProviderClaim savedClaim = claimRepo.save(claim);

        // 🔔 إشعار للإداريين الطبيين (جميع الإداريين الطبيين)
        clientRepo.findByRoles_Name(RoleName.MEDICAL_ADMIN)
                .forEach(medicalAdmin -> notificationService.sendToUser(
                        medicalAdmin.getId(),
                        "📋 مطالبة جديدة من العميل " + client.getFullName() +
                                " - المبلغ: " + claim.getAmount() + " دينار"
                ));

        // 🔔 إشعار للعميل
        notificationService.sendToUser(
                client.getId(),
                "✅ تم إرسال مطالبتك بنجاح - المبلغ: " + claim.getAmount() + " دينار" +
                        " - في انتظار المراجعة الطبية"
        );

        return claimMapper.toDto(savedClaim);
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
        log.info("🔹 Rejecting claim medically...");

        HealthcareProviderClaim claim = claimRepo.findById(claimId)
                .orElseThrow(() -> new NotFoundException("Claim not found"));

        if (claim.getStatus() != ClaimStatus.PENDING_MEDICAL)
            throw new NotFoundException("Claim was already processed");

        Client reviewer = clientRepo.findById(reviewerId)
                .orElseThrow(() -> new NotFoundException("Reviewer not found"));

        claim.setStatus(ClaimStatus.REJECTED_BY_MEDICAL);
        claim.setRejectedAt(Instant.now());
        claim.setRejectionReason(reason);
        claim.setMedicalReviewerId(reviewerId);
        claim.setMedicalReviewerName(reviewer.getFullName());
        claim.setMedicalReviewedAt(Instant.now());

        HealthcareProviderClaim savedClaim = claimRepo.save(claim);

        // 🔔 إشعار لمقدم الخدمة
        notificationService.sendToUser(
                claim.getHealthcareProvider().getId(),
                "❌ تم رفض مطالبتك من المراجع الطبي " + reviewer.getFullName() +
                        " - المبلغ: " + claim.getAmount() + " دينار" +
                        (reason != null && !reason.isEmpty() ? "\nالسبب: " + reason : "")
        );

        // 🔔 إشعار للمريض (إن وجد)
        if (claim.getClientId() != null) {
            clientRepo.findById(claim.getClientId()).ifPresent(patient ->
                    notificationService.sendToUser(
                            patient.getId(),
                            "❌ تم رفض مطالبتك الطبية من " + claim.getHealthcareProvider().getFullName() +
                                    " - السبب: " + (reason != null && !reason.isEmpty() ? reason : "غير محدد")
                    )
            );
        }

        return claimMapper.toDto(savedClaim);
    }

    // ============================================================
    // 🟩 موافقة طبية → تنتقل للإداري
    // ============================================================
    public HealthcareProviderClaimDTO approveMedical(UUID claimId, UUID reviewerId) {
        log.info("🔹 Approving claim medically...");

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

        HealthcareProviderClaim savedClaim = claimRepo.save(claim);

        // 🔔 إشعار لمقدم الخدمة
        notificationService.sendToUser(
                claim.getHealthcareProvider().getId(),
                "✅ تمت الموافقة على مطالبتك من المراجع الطبي " + reviewer.getFullName() +
                        " - المبلغ: " + claim.getAmount() + " دينار" +
                        " - الآن في انتظار المراجعة الإدارية"
        );

        // 🔔 إشعار للإداريين التنسيقيين (جميع الإداريين التنسيقيين)
        clientRepo.findByRoles_Name(RoleName.COORDINATION_ADMIN)
                .forEach(admin -> notificationService.sendToUser(
                        admin.getId(),
                        "📋 مطالبة بانتظار المراجعة الإدارية من " + claim.getHealthcareProvider().getFullName() +
                                " - المبلغ: " + claim.getAmount() + " دينار" +
                                (claim.getClientName() != null ? " للمريض " + claim.getClientName() : "") +
                                " - تمت الموافقة الطبية من " + reviewer.getFullName()
                ));

        // 🔔 إشعار للمريض (إن وجد)
        if (claim.getClientId() != null) {
            clientRepo.findById(claim.getClientId()).ifPresent(patient ->
                    notificationService.sendToUser(
                            patient.getId(),
                            "✅ تمت الموافقة الطبية على مطالبتك من " + claim.getHealthcareProvider().getFullName() +
                                    " - في انتظار الموافقة الإدارية النهائية"
                    )
            );
        }

        return claimMapper.toDto(savedClaim);
    }

    // ============================================================
    // ❌ رفض إداري
    // ============================================================
    public HealthcareProviderClaimDTO rejectAdmin(UUID claimId, String reason, UUID reviewerId) {
        log.info("🔹 Rejecting claim administratively...");

        HealthcareProviderClaim claim = claimRepo.findById(claimId)
                .orElseThrow(() -> new NotFoundException("Claim not found"));

        if (claim.getStatus() != ClaimStatus.AWAITING_ADMIN_REVIEW)
            throw new NotFoundException("Claim is not ready for administrative review");

        Client reviewer = clientRepo.findById(reviewerId)
                .orElseThrow(() -> new NotFoundException("Reviewer not found"));

        claim.setStatus(ClaimStatus.REJECTED);
        claim.setRejectedAt(Instant.now());
        claim.setRejectionReason(reason);

        HealthcareProviderClaim savedClaim = claimRepo.save(claim);

        // 🔔 إشعار لمقدم الخدمة
        notificationService.sendToUser(
                claim.getHealthcareProvider().getId(),
                "❌ تم رفض مطالبتك من الإدارة " +
                        " - المبلغ: " + claim.getAmount() + " دينار" +
                        (reason != null && !reason.isEmpty() ? "\nالسبب: " + reason : "")
        );

        // 🔔 إشعار للمريض (إن وجد)
        if (claim.getClientId() != null) {
            clientRepo.findById(claim.getClientId()).ifPresent(patient ->
                    notificationService.sendToUser(
                            patient.getId(),
                            "❌ تم رفض مطالبتك الطبية من " + claim.getHealthcareProvider().getFullName() +
                                    " - السبب: " + (reason != null && !reason.isEmpty() ? reason : "غير محدد")
                    )
            );
        }

        // 🔔 إشعار للمراجع الطبي (إعلامي)
        if (claim.getMedicalReviewerId() != null) {
            notificationService.sendToUser(
                    claim.getMedicalReviewerId(),
                    "ℹ️ تم رفض مطالبة طبية تمت مراجعتها - المبلغ: " + claim.getAmount() + " دينار" +
                            " من " + claim.getHealthcareProvider().getFullName()
            );
        }

        return claimMapper.toDto(savedClaim);
    }

    // ============================================================
    // 🟩 موافقة إدارية نهائية
    // ============================================================
    public HealthcareProviderClaimDTO approveAdmin(UUID claimId, UUID reviewerId) {
        log.info("🔹 Approving claim administratively...");

        HealthcareProviderClaim claim = claimRepo.findById(claimId)
                .orElseThrow(() -> new NotFoundException("Claim not found"));

        if (claim.getStatus() != ClaimStatus.AWAITING_ADMIN_REVIEW)
            throw new NotFoundException("Medical approval required first");

        Client reviewer = clientRepo.findById(reviewerId)
                .orElseThrow(() -> new NotFoundException("Reviewer not found"));

        claim.setStatus(ClaimStatus.APPROVED);
        claim.setApprovedAt(Instant.now());

        HealthcareProviderClaim savedClaim = claimRepo.save(claim);

        // 🔔 إشعار لمقدم الخدمة
        notificationService.sendToUser(
                claim.getHealthcareProvider().getId(),
                "✅ تمت الموافقة النهائية على مطالبتك من الإدارة " +
                        " - المبلغ: " + claim.getAmount() + " دينار" +
                        (claim.getClientName() != null ? " للمريض " + claim.getClientName() : "") +
                        " - تمت الموافقة بنجاح!"
        );

        // 🔔 إشعار للمريض (إن وجد)
        if (claim.getClientId() != null) {
            clientRepo.findById(claim.getClientId()).ifPresent(patient ->
                    notificationService.sendToUser(
                            patient.getId(),
                            "✅ تمت الموافقة النهائية على مطالبتك الطبية من " + claim.getHealthcareProvider().getFullName() +
                                    " - المبلغ: " + claim.getAmount() + " دينار"
                    )
            );
        }

        // 🔔 إشعار للمراجع الطبي (إعلامي)
        if (claim.getMedicalReviewerId() != null) {
            notificationService.sendToUser(
                    claim.getMedicalReviewerId(),
                    "✅ تمت الموافقة النهائية على مطالبة طبية تمت مراجعتها - المبلغ: " + claim.getAmount() + " دينار" +
                            " من " + claim.getHealthcareProvider().getFullName()
            );
        }

        return claimMapper.toDto(savedClaim);
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

            // تعبئة clientName + employeeId
            if (claim.getClientId() != null) {
                clientRepo.findById(claim.getClientId()).ifPresent(client -> {
                    dto.setClientName(client.getFullName());
                    dto.setEmployeeId(client.getEmployeeId());
                });
            }

            // ⭐ المهم جداً: أرجع الـ role كما هو بدون أي mapping
            // Check if this is a client self-service claim (provider and patient are the same)
            String role;
            if (claim.getClientId() != null &&
                    claim.getHealthcareProvider().getId().equals(claim.getClientId())) {
                // This is a client self-service claim
                role = "INSURANCE_CLIENT";
            } else {
                // Regular provider claim - get role from provider
                role = claim.getHealthcareProvider()
                        .getRoles()
                        .stream()
                        .findFirst()
                        .map(r -> r.getName().name())
                        .orElse("UNKNOWN");
            }

            dto.setProviderRole(role);

            // ⭐ Ensure description is included in DTO
            dto.setDescription(claim.getDescription());

            // ⭐ Ensure roleSpecificData (contains medicines for pharmacist claims) is included in DTO
            dto.setRoleSpecificData(claim.getRoleSpecificData());

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
                ClaimStatus.AWAITING_ADMIN_REVIEW
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
            // Check if this is a client self-service claim (provider and patient are the same)
            String role;
            if (claim.getClientId() != null &&
                    claim.getHealthcareProvider().getId().equals(claim.getClientId())) {
                // This is a client self-service claim
                role = "INSURANCE_CLIENT";
            } else {
                // Regular provider claim - get role from provider
                role = claim.getHealthcareProvider()
                        .getRoles()
                        .stream()
                        .findFirst()
                        .map(r -> r.getName().name())
                        .orElse("UNKNOWN");
            }

            dto.setProviderRole(role);

            // ⭐ Ensure description is included in DTO
            dto.setDescription(claim.getDescription());

            // ⭐ Ensure roleSpecificData (contains medicines for pharmacist claims) is included in DTO
            dto.setRoleSpecificData(claim.getRoleSpecificData());

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

    // ============================================================
    // 🟩 Batch Administrative Approval
    // ============================================================
    public void approveAdminBatch(List<UUID> claimIds, UUID reviewerId) {
        log.info("🔹 Batch approving claims administratively...");

        Client reviewer = clientRepo.findById(reviewerId)
                .orElseThrow(() -> new NotFoundException("Reviewer not found"));

        List<HealthcareProviderClaim> claims = claimRepo.findAllById(claimIds);
        List<HealthcareProviderClaim> approvedClaims = new java.util.ArrayList<>();

        for (HealthcareProviderClaim claim : claims) {
            // فقط المطالبات الجاهزة
            if (claim.getStatus() != ClaimStatus.AWAITING_ADMIN_REVIEW) {
                continue;
            }

            claim.setStatus(ClaimStatus.APPROVED);
            claim.setApprovedAt(Instant.now());
            approvedClaims.add(claim);
        }

        claimRepo.saveAll(approvedClaims);

        // 🔔 إشعارات مجمعة لمقدمي الخدمة
        for (HealthcareProviderClaim claim : approvedClaims) {
            notificationService.sendToUser(
                    claim.getHealthcareProvider().getId(),
                    "✅ تمت الموافقة النهائية على مطالبتك من الإدارة " +
                            " - المبلغ: " + claim.getAmount() + " دينار" +
                            (claim.getClientName() != null ? " للمريض " + claim.getClientName() : "") +
                            " - تمت الموافقة بنجاح!"
            );

            // 🔔 إشعار للمريض (إن وجد)
            if (claim.getClientId() != null) {
                clientRepo.findById(claim.getClientId()).ifPresent(patient ->
                        notificationService.sendToUser(
                                patient.getId(),
                                "✅ تمت الموافقة النهائية على مطالبتك الطبية من " + claim.getHealthcareProvider().getFullName() +
                                        " - المبلغ: " + claim.getAmount() + " دينار"
                        )
                );
            }
        }
    }

    // ============================================================
    // 📤 Export Approved Claims as CSV
    // ============================================================
    public byte[] exportApprovedClaimsCsv() {
        List<HealthcareProviderClaim> claims =
                claimRepo.findAllApprovedClaims();

        StringBuilder csv = new StringBuilder();

        // ===============================
        // CSV Header (✔ تمت إضافة Medical Reviewer)
        // ===============================
        csv.append(
                "Claim ID," +
                        "Patient Name," +
                        "Employee ID," +
                        "Provider Name," +
                        "Provider Role," +
                        "Medical Reviewer," +
                        "Amount," +
                        "Service Date," +
                        "Approved Date\n"
        );

        for (HealthcareProviderClaim claim : claims) {
            String providerRole = claim.getHealthcareProvider()
                    .getRoles()
                    .stream()
                    .findFirst()
                    .map(r -> r.getName().name())
                    .orElse("UNKNOWN");

            csv.append(claim.getId()).append(",");
            csv.append(escape(claim.getClientName())).append(",");
            csv.append(getEmployeeId(claim)).append(",");
            csv.append(escape(claim.getHealthcareProvider().getFullName())).append(",");
            csv.append(providerRole).append(",");

            // ===============================
            // ⭐ Medical Reviewer
            // ===============================
            csv.append(escape(claim.getMedicalReviewerName())).append(",");
            csv.append(claim.getAmount()).append(",");
            csv.append(claim.getServiceDate()).append(",");
            csv.append(claim.getApprovedAt()).append("\n");
        }

        return csv.toString().getBytes();
    }

    // ===============================
    // Helpers
    // ===============================
    private String escape(String value) {
        if (value == null) return "";
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    private String getEmployeeId(HealthcareProviderClaim claim) {
        if (claim.getClientId() == null) return "";
        return clientRepo.findById(claim.getClientId())
                .map(Client::getEmployeeId)
                .orElse("");
    }
}

