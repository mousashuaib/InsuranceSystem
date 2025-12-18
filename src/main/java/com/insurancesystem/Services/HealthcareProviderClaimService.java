package com.insurancesystem.Services;
import com.insurancesystem.Exception.BadRequestException;
import com.insurancesystem.Model.Entity.Enums.ReportType;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;

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
import java.time.LocalDate;
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
    public List<HealthcareProviderClaimDTO> getProviderClaims(UUID userId) {

        Client user = clientRepo.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        boolean isClient = user.getRoles()
                .stream()
                .anyMatch(r -> r.getName() == RoleName.INSURANCE_CLIENT);

        List<HealthcareProviderClaim> claims;

        if (isClient) {
            // ✅ العميل يرى كل مطالبه (سواء أنشأها بنفسه أو أنشأها المنسق له)
            claims = claimRepo.findByClientId(user.getId());
        } else {
            // ✅ مقدم الخدمة / الطبيب / الصيدلي / المنسق
            claims = claimRepo.findByHealthcareProvider(user);
        }

        return claims.stream().map(claim -> {
            HealthcareProviderClaimDTO dto = claimMapper.toDto(claim);
            dto.setMedicalReviewerName(claim.getMedicalReviewerName());
            dto.setMedicalReviewedAt(claim.getMedicalReviewedAt());
            return dto;
        }).toList();

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

        claim.setStatus(ClaimStatus.REJECTED_FINAL);
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
        claim.setStatus(ClaimStatus.APPROVED_FINAL);

        HealthcareProviderClaim savedClaim = claimRepo.save(claim);

        // 🔔 إشعار لمقدم الخدمة
        notificationService.sendToUser(
                claim.getHealthcareProvider().getId(),
                "✅ تمت الموافقة على مطالبتك من المراجع الطبي " + reviewer.getFullName() +
                        " - المبلغ: " + claim.getAmount() + " دينار" +
                        " - تمت الموافقة النهائية"
        );



        // 🔔 إشعار للمريض (إن وجد)
        if (claim.getClientId() != null) {
            clientRepo.findById(claim.getClientId()).ifPresent(patient ->
                    notificationService.sendToUser(
                            patient.getId(),
                            "✅ تمت الموافقة الطبية على مطالبتك من " + claim.getHealthcareProvider().getFullName() +
                                    " - تمت الموافقة النهائية على مطالبتك الطبية"
                    )
            );
        }

        return claimMapper.toDto(savedClaim);
    }
    // ============================================================
    // Medical Review List
    // ============================================================
    public List<HealthcareProviderClaimMedicalDTO> getClaimsForMedicalReview() {
        List<HealthcareProviderClaim> claims =
                claimRepo.findByStatusIn(
                        List.of(
                                ClaimStatus.PENDING_MEDICAL,
                                ClaimStatus.RETURNED_FOR_REVIEW
                        )
                );

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
                ClaimStatus.APPROVED_FINAL,
                ClaimStatus.REJECTED_FINAL
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
// 📤 Export Approved Claims as PDF
// ============================================================
    public byte[] exportApprovedClaimsPdf() {

        List<HealthcareProviderClaim> claims =
                claimRepo.findAllApprovedClaims();

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(document, out);

            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            document.add(new Paragraph("Approved Claims Report", titleFont));
            document.add(new Paragraph(" "));
            document.add(new Paragraph("Generated by Coordination Admin"));
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);

            table.addCell("Claim ID");
            table.addCell("Patient Name");
            table.addCell("Provider Name");
            table.addCell("Medical Reviewer");
            table.addCell("Amount");
            table.addCell("Service Date");

            for (HealthcareProviderClaim claim : claims) {
                table.addCell(claim.getId().toString());
                table.addCell(
                        claim.getClientName() != null ? claim.getClientName() : "-"
                );
                table.addCell(
                        claim.getHealthcareProvider().getFullName()
                );
                table.addCell(
                        claim.getMedicalReviewerName() != null
                                ? claim.getMedicalReviewerName()
                                : "-"
                );
                table.addCell(claim.getAmount() + " NIS");
                table.addCell(claim.getServiceDate().toString());
            }

            document.add(table);
            document.close();

            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF report", e);
        }
    }

    private byte[] generatePdf(
            ReportType reportType,
            List<HealthcareProviderClaim> claims
    ) {

        boolean hideClientName = reportType != ReportType.CLIENT;

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(document, out);

            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            document.add(new Paragraph(
                    reportType.name() + " Claims Report",
                    titleFont
            ));
            document.add(new Paragraph(" "));
            document.add(new Paragraph("Generated by Coordination Admin"));
            document.add(new Paragraph(" "));

            // عدد الأعمدة حسب نوع التقرير
            int columns = hideClientName ? 5 : 6;
            PdfPTable table = new PdfPTable(columns);
            table.setWidthPercentage(100);

            // ===== Header =====
            table.addCell("Claim ID");
            table.addCell("Provider Name");

            if (!hideClientName) {
                table.addCell("Client Name");
            }

            table.addCell("Amount");
            table.addCell("Status");
            table.addCell("Service Date");

            // ===== Data =====
            for (HealthcareProviderClaim claim : claims) {

                table.addCell(claim.getId().toString());
                table.addCell(claim.getHealthcareProvider().getFullName());

                if (!hideClientName) {
                    table.addCell(
                            claim.getClientName() != null
                                    ? claim.getClientName()
                                    : "-"
                    );
                }

                table.addCell(claim.getAmount() + " NIS");
                table.addCell(claim.getStatus().name());
                table.addCell(
                        claim.getServiceDate() != null
                                ? claim.getServiceDate().toString()
                                : "-"
                );
            }

            document.add(table);
            document.close();

            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF report", e);
        }
    }


    public byte[] exportReportPdf(
            ReportType reportType,
            ClaimStatus status,
            LocalDate from,
            LocalDate to
    )
 {

        RoleName roleFilter = switch (reportType) {
            case DOCTOR -> RoleName.DOCTOR;
            case PHARMACY -> RoleName.PHARMACIST;
            case LAB -> RoleName.LAB_TECH;
            case RADIOLOGY -> RoleName.RADIOLOGIST;
            case CLIENT -> null; // clients handled separately
        };

        List<HealthcareProviderClaim> claims =
                claimRepo.filterClaims(status, from, to, roleFilter);

        return generatePdf(reportType, claims);
    }

    public HealthcareProviderClaimDTO createClaimByCoordinationAdmin(
            UUID adminId,
            CreateHealthcareProviderClaimDTO dto,
            MultipartFile invoiceImage
    ) {

        Client admin = clientRepo.findById(adminId)
                .orElseThrow(() -> new NotFoundException("Coordination admin not found"));

        // ✅ تأكيد الدور
        boolean isCoordinator = admin.getRoles().stream()
                .anyMatch(r -> r.getName() == RoleName.COORDINATION_ADMIN);

        if (!isCoordinator) {
            throw new BadRequestException("Only coordination admin can create claims this way");
        }

        // ✅ clientId إلزامي
        if (dto.getClientId() == null) {
            throw new BadRequestException("Client ID is required for coordination admin claim");
        }

        Client client = clientRepo.findById(dto.getClientId())
                .orElseThrow(() -> new NotFoundException("Client not found"));

        HealthcareProviderClaim claim = claimMapper.toEntity(dto);

        // 🔴 إدخال منسق
        claim.setHealthcareProvider(admin);

        // 🟢 تثبيت بيانات المؤمن
        claim.setClientId(client.getId());
        claim.setClientName(client.getFullName());

        claim.setStatus(ClaimStatus.PENDING_MEDICAL);
        claim.setApprovedAt(null);


        // ❌ لا مراجع طبي
        claim.setMedicalReviewerId(null);
        claim.setMedicalReviewerName(null);
        claim.setMedicalReviewedAt(null);

        if (invoiceImage != null && !invoiceImage.isEmpty()) {
            claim.setInvoiceImagePath(saveDocument(invoiceImage));
        }

        return claimMapper.toDto(claimRepo.save(claim));
    }

    public HealthcareProviderClaimDTO returnToMedical(
            UUID claimId,
            String reason,
            UUID coordinatorId
    ) {
        HealthcareProviderClaim claim = claimRepo.findById(claimId)
                .orElseThrow(() -> new NotFoundException("Claim not found"));

        if (claim.getStatus() != ClaimStatus.APPROVED_FINAL) {
            throw new BadRequestException("Only finally approved claims can be returned");
        }


        claim.setStatus(ClaimStatus.RETURNED_FOR_REVIEW);
        claim.setRejectionReason(reason); // أو returnReason لو تحب تفصل
        claim.setRejectedAt(Instant.now());

        HealthcareProviderClaim saved = claimRepo.save(claim);

        // 🔔 إشعار لمقدم الطلب
        notificationService.sendToUser(
                claim.getHealthcareProvider().getId(),
                "⚠️ تمت إعادة مطالبتك للمراجعة الطبية بسبب ملاحظة إدارية:\n" + reason
        );

        return claimMapper.toDto(saved);
    }
    public List<HealthcareProviderClaimDTO> getClaimsForCoordinationReview() {

        List<HealthcareProviderClaim> claims =
                claimRepo.findByStatus(ClaimStatus.APPROVED_FINAL);

        return claims.stream()
                .map(claimMapper::toDto)
                .toList();
    }

}

