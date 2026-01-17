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
import com.insurancesystem.Repository.FamilyMemberRepository;
import com.insurancesystem.Repository.PrescriptionRepository;
import com.insurancesystem.Repository.LabRequestRepository;
import com.insurancesystem.Repository.RadiologistRepository;
import com.insurancesystem.Model.Entity.FamilyMember;
import com.insurancesystem.Model.Entity.Prescription;
import com.insurancesystem.Model.Entity.LabRequest;
import com.insurancesystem.Model.MapStruct.PrescriptionMapper;
import com.insurancesystem.Model.MapStruct.LabRequestMapper;
import com.insurancesystem.Model.Dto.PrescriptionDTO;
import com.insurancesystem.Model.Dto.LabRequestDTO;
import com.insurancesystem.Model.Dto.RadiologyRequestDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.insurancesystem.Model.Entity.Enums.FamilyRelation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class HealthcareProviderClaimService {
    private final HealthcareProviderClaimRepository claimRepo;
    private final ClientRepository clientRepo;
    private final FamilyMemberRepository familyMemberRepo;
    private final PrescriptionRepository prescriptionRepo;
    private final LabRequestRepository labRequestRepo;
    private final RadiologistRepository radiologyRequestRepo;
    private final PrescriptionMapper prescriptionMapper;
    private final LabRequestMapper labRequestMapper;
    private final com.insurancesystem.Model.MapStruct.RadiologyRequestMapper radiologyRequestMapper;
    private final HealthcareProviderClaimMapper claimMapper;
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String UPLOAD_DIR = "uploads/healthcare-claims/";

    // Create claim by healthcare provider (doctor, pharmacist, lab tech, radiologist)
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

        // Check if this is a follow-up visit from roleSpecificData
        boolean isFollowUp = false;
        java.math.BigDecimal originalConsultationFee = null;
        if (dto.getRoleSpecificData() != null) {
            try {
                java.util.Map<String, Object> roleData = objectMapper.readValue(
                        dto.getRoleSpecificData(),
                        java.util.Map.class
                );
                Object isFollowUpObj = roleData.get("isFollowUp");
                if (isFollowUpObj instanceof Boolean) {
                    isFollowUp = (Boolean) isFollowUpObj;
                } else if (isFollowUpObj instanceof String) {
                    isFollowUp = Boolean.parseBoolean((String) isFollowUpObj);
                }
                
                // If follow-up, store original consultation fee and set amount to 0
                if (isFollowUp) {
                    // Get original consultation fee from roleData if available
                    Object originalFeeObj = roleData.get("originalConsultationFee");
                    if (originalFeeObj != null) {
                        if (originalFeeObj instanceof Number) {
                            double feeValue = ((Number) originalFeeObj).doubleValue();
                            if (feeValue > 0) {
                                originalConsultationFee = java.math.BigDecimal.valueOf(feeValue);
                            }
                        } else if (originalFeeObj instanceof String) {
                            try {
                                double feeValue = Double.parseDouble((String) originalFeeObj);
                                if (feeValue > 0) {
                                    originalConsultationFee = new java.math.BigDecimal((String) originalFeeObj);
                                }
                            } catch (Exception e) {
                                log.warn("Error parsing originalConsultationFee: {}", e.getMessage());
                            }
                        }
                    }

                    claim.setAmount(0.0); // Insurance doesn't pay for follow-up consultation
                    if (originalConsultationFee != null && originalConsultationFee.compareTo(java.math.BigDecimal.ZERO) > 0) {
                        claim.setOriginalConsultationFee(originalConsultationFee);
                    } else {
                        log.warn("Follow-up visit detected but originalConsultationFee is missing or zero. Claim ID: {}", claim.getId());
                    }
                }
            } catch (Exception e) {
                log.warn("Error parsing roleSpecificData for follow-up check: {}", e.getMessage());
            }
        }
        
        claim.setIsFollowUp(isFollowUp);

        // Handle patient info (can be Client or FamilyMember)
        Client patient = null;
        String patientName = null;
        
        if (claim.getClientId() != null) {
            Optional<FamilyMember> familyMemberOpt = familyMemberRepo.findById(claim.getClientId());
            if (familyMemberOpt.isPresent()) {
                FamilyMember familyMember = familyMemberOpt.get();
                claim.setClientName(familyMember.getFullName());
                patientName = familyMember.getFullName();
                patient = familyMember.getClient();
            } else {
                Optional<Client> clientOpt = clientRepo.findById(claim.getClientId());
                if (clientOpt.isPresent()) {
                    Client client = clientOpt.get();
                    
                    // Extract family member info from role-specific data (pharmacist, lab, radiology)
                    boolean isPharmacist = provider.getRoles().stream()
                            .anyMatch(r -> r.getName() == RoleName.PHARMACIST);
                    
                    boolean foundFamilyMember = false;
                    
                    if (isPharmacist && claim.getRoleSpecificData() != null) {
                        try {
                            java.util.Map<String, Object> roleData = objectMapper.readValue(
                                    claim.getRoleSpecificData(), 
                                    java.util.Map.class
                            );
                            String prescriptionIdStr = (String) roleData.get("prescriptionId");
                            
                            if (prescriptionIdStr != null) {
                                UUID prescriptionId = UUID.fromString(prescriptionIdStr);
                                Optional<Prescription> prescriptionOpt = prescriptionRepo.findById(prescriptionId);
                                
                                if (prescriptionOpt.isPresent()) {
                                    Prescription prescription = prescriptionOpt.get();
                                    PrescriptionDTO prescriptionDto = prescriptionMapper.toDto(prescription, familyMemberRepo);
                                    
                                    if (prescriptionDto.getIsFamilyMember() != null && prescriptionDto.getIsFamilyMember()) {
                                        String familyMemberName = prescriptionDto.getFamilyMemberName();
                                        String familyMemberRelationStr = prescriptionDto.getFamilyMemberRelation();
                                        
                                        if (familyMemberName != null && familyMemberRelationStr != null) {
                                            try {
                                                FamilyRelation relation = FamilyRelation.valueOf(familyMemberRelationStr.toUpperCase());
                                                
                                                Optional<FamilyMember> fmOpt = familyMemberRepo.findByClient_IdAndFullNameAndRelation(
                                                        client.getId(), 
                                                        familyMemberName, 
                                                        relation
                                                );
                                                
                                                if (fmOpt.isPresent()) {
                                                    FamilyMember fm = fmOpt.get();
                                                    claim.setClientId(fm.getId());
                                                    claim.setClientName(fm.getFullName());
                                                    patientName = fm.getFullName();
                                                    patient = client;
                                                    foundFamilyMember = true;
                                                }
                                            } catch (IllegalArgumentException e) {
                                            }
                                        }
                                    }
                                }
                            }
                        } catch (Exception e) {
                        }
                    }
                    
                    if (!foundFamilyMember && isPharmacist && claim.getTreatmentDetails() != null) {
                        try {
                            String treatmentDetails = claim.getTreatmentDetails();
                            String familyMemberPattern = "Family Member:\\s*([^-]+?)\\s*\\(([^)]+)\\)\\s*-\\s*Insurance:";
                            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(familyMemberPattern, java.util.regex.Pattern.CASE_INSENSITIVE);
                            java.util.regex.Matcher matcher = pattern.matcher(treatmentDetails);
                            
                            if (matcher.find()) {
                                String familyMemberName = matcher.group(1).trim();
                                String familyMemberRelationStr = matcher.group(2).trim();
                                
                                try {
                                    FamilyRelation relation = FamilyRelation.valueOf(familyMemberRelationStr.toUpperCase());
                                    
                                    Optional<FamilyMember> fmOpt = familyMemberRepo.findByClient_IdAndFullNameAndRelation(
                                            client.getId(), 
                                            familyMemberName, 
                                            relation
                                    );
                                    
                                    if (fmOpt.isPresent()) {
                                        FamilyMember fm = fmOpt.get();
                                        claim.setClientId(fm.getId());
                                        claim.setClientName(fm.getFullName());
                                        patientName = fm.getFullName();
                                        patient = client;
                                        foundFamilyMember = true;
                                    }
                                } catch (IllegalArgumentException e) {
                                }
                            }
                        } catch (Exception e) {
                        }
                    }
                    
                    boolean isLabTech = provider.getRoles().stream()
                            .anyMatch(r -> r.getName() == RoleName.LAB_TECH);
                    
                    if (!foundFamilyMember && isLabTech && claim.getRoleSpecificData() != null) {
                        try {
                            java.util.Map<String, Object> roleData = objectMapper.readValue(
                                    claim.getRoleSpecificData(), 
                                    java.util.Map.class
                            );
                            String testIdStr = (String) roleData.get("testId");
                            
                            if (testIdStr != null) {
                                UUID labRequestId = UUID.fromString(testIdStr);
                                Optional<LabRequest> labRequestOpt = labRequestRepo.findByIdWithMember(labRequestId);
                                
                                if (labRequestOpt.isPresent()) {
                                    LabRequest labRequest = labRequestOpt.get();
                                    LabRequestDTO labRequestDto = labRequestMapper.toDto(labRequest, familyMemberRepo);
                                    
                                    if ((labRequestDto.getIsFamilyMember() == null || !labRequestDto.getIsFamilyMember()) 
                                            && (labRequest.getNotes() != null || labRequest.getTreatment() != null)) {
                                        String textToSearch = labRequest.getNotes() != null ? labRequest.getNotes() : labRequest.getTreatment();
                                        if (textToSearch != null && textToSearch.toLowerCase().contains("family member:")) {
                                            String familyMemberPattern = "Family Member:\\s*([^-]+?)\\s*\\(([^)]+)\\)\\s*-\\s*Insurance:\\s*([^-]+?)(?:\\s*-\\s*Age:\\s*([^-]+?))?(?:\\s*-\\s*Gender:\\s*([^\\n\\r]+))?";
                                            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(familyMemberPattern, java.util.regex.Pattern.CASE_INSENSITIVE);
                                            java.util.regex.Matcher matcher = pattern.matcher(textToSearch);
                                            
                                            if (matcher.find()) {
                                                String familyMemberName = matcher.group(1).trim();
                                                String familyMemberRelationStr = matcher.group(2).trim();
                                                
                                                try {
                                                    FamilyRelation relation = FamilyRelation.valueOf(familyMemberRelationStr.toUpperCase());
                                                    
                                                    Optional<FamilyMember> fmOpt = familyMemberRepo.findByClient_IdAndFullNameAndRelation(
                                                            client.getId(), 
                                                            familyMemberName, 
                                                            relation
                                                    );
                                                    
                                                    if (fmOpt.isPresent()) {
                                                        FamilyMember fm = fmOpt.get();
                                                        claim.setClientId(fm.getId());
                                                        claim.setClientName(fm.getFullName());
                                                        patientName = fm.getFullName();
                                                        patient = client;
                                                        foundFamilyMember = true;
                                                    }
                                                } catch (IllegalArgumentException e) {
                                                    // Invalid relation value
                                                }
                                            }
                                        }
                                    }
                                    
                                    if (labRequestDto.getIsFamilyMember() != null && labRequestDto.getIsFamilyMember()) {
                                        if (labRequestDto.getFamilyMemberId() != null) {
                                            Optional<FamilyMember> fmOpt = familyMemberRepo.findById(labRequestDto.getFamilyMemberId());
                                            
                                            if (fmOpt.isPresent()) {
                                                FamilyMember fm = fmOpt.get();
                                                if (fm.getClient() != null && fm.getClient().getId().equals(client.getId())) {
                                                    claim.setClientId(fm.getId());
                                                    claim.setClientName(fm.getFullName());
                                                    patientName = fm.getFullName();
                                                    patient = client;
                                                    foundFamilyMember = true;
                                                }
                                            }
                                        }
                                        
                                        if (!foundFamilyMember) {
                                        String familyMemberName = labRequestDto.getFamilyMemberName();
                                        String familyMemberRelationStr = labRequestDto.getFamilyMemberRelation();
                                        
                                        if (familyMemberName != null && familyMemberRelationStr != null) {
                                            try {
                                                FamilyRelation relation = FamilyRelation.valueOf(familyMemberRelationStr.toUpperCase());
                                                
                                                Optional<FamilyMember> fmOpt = familyMemberRepo.findByClient_IdAndFullNameAndRelation(
                                                        client.getId(), 
                                                        familyMemberName, 
                                                        relation
                                                );
                                                
                                                if (fmOpt.isPresent()) {
                                                    FamilyMember fm = fmOpt.get();
                                                    claim.setClientId(fm.getId());
                                                    claim.setClientName(fm.getFullName());
                                                    patientName = fm.getFullName();
                                                        patient = client;
                                                    foundFamilyMember = true;
                                                }
                                            } catch (IllegalArgumentException e) {
                                                    // Invalid relation value
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } catch (Exception e) {
                        }
                    }
                    
                    if (!foundFamilyMember && isLabTech && claim.getTreatmentDetails() != null) {
                        try {
                            String treatmentDetails = claim.getTreatmentDetails();
                            String familyMemberPattern = "Family Member:\\s*([^-]+?)\\s*\\(([^)]+)\\)\\s*-\\s*Insurance:";
                            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(familyMemberPattern, java.util.regex.Pattern.CASE_INSENSITIVE);
                            java.util.regex.Matcher matcher = pattern.matcher(treatmentDetails);
                            
                            if (matcher.find()) {
                                String familyMemberName = matcher.group(1).trim();
                                String familyMemberRelationStr = matcher.group(2).trim();
                                
                                try {
                                    FamilyRelation relation = FamilyRelation.valueOf(familyMemberRelationStr.toUpperCase());
                                    
                                    Optional<FamilyMember> fmOpt = familyMemberRepo.findByClient_IdAndFullNameAndRelation(
                                            client.getId(), 
                                            familyMemberName, 
                                            relation
                                    );
                                    
                                    if (fmOpt.isPresent()) {
                                        FamilyMember fm = fmOpt.get();
                                        claim.setClientId(fm.getId());
                                        claim.setClientName(fm.getFullName());
                                        patientName = fm.getFullName();
                                        patient = client;
                                        foundFamilyMember = true;
                                    }
                                } catch (IllegalArgumentException e) {
                                }
                            }
                        } catch (Exception e) {
                        }
                    }
                    
                    boolean isRadiologist = provider.getRoles().stream()
                            .anyMatch(r -> r.getName() == RoleName.RADIOLOGIST);
                    
                    if (!foundFamilyMember && isRadiologist && claim.getRoleSpecificData() != null) {
                        try {
                            java.util.Map<String, Object> roleData = objectMapper.readValue(
                                    claim.getRoleSpecificData(), 
                                    java.util.Map.class
                            );
                            String testIdStr = (String) roleData.get("testId");
                            
                            if (testIdStr != null) {
                                UUID radiologyRequestId = UUID.fromString(testIdStr);
                                Optional<com.insurancesystem.Model.Entity.RadiologyRequest> radiologyRequestOpt = radiologyRequestRepo.findById(radiologyRequestId);
                                
                                if (radiologyRequestOpt.isPresent()) {
                                    com.insurancesystem.Model.Entity.RadiologyRequest radiologyRequest = radiologyRequestOpt.get();
                                    RadiologyRequestDTO radiologyRequestDto = radiologyRequestMapper.toDto(radiologyRequest, familyMemberRepo);
                                    
                                    if ((radiologyRequestDto.getIsFamilyMember() == null || !radiologyRequestDto.getIsFamilyMember()) 
                                            && (radiologyRequest.getNotes() != null || radiologyRequest.getTreatment() != null)) {
                                        String textToSearch = radiologyRequest.getNotes() != null ? radiologyRequest.getNotes() : radiologyRequest.getTreatment();
                                        if (textToSearch != null && textToSearch.toLowerCase().contains("family member:")) {
                                            String familyMemberPattern = "Family Member:\\s*([^-]+?)\\s*\\(([^)]+)\\)\\s*-\\s*Insurance:\\s*([^-]+?)(?:\\s*-\\s*Age:\\s*([^-]+?))?(?:\\s*-\\s*Gender:\\s*([^\\n\\r]+))?";
                                            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(familyMemberPattern, java.util.regex.Pattern.CASE_INSENSITIVE);
                                            java.util.regex.Matcher matcher = pattern.matcher(textToSearch);
                                            
                                            if (matcher.find()) {
                                                String familyMemberName = matcher.group(1).trim();
                                                String familyMemberRelationStr = matcher.group(2).trim();
                                
                                try {
                                    FamilyRelation relation = FamilyRelation.valueOf(familyMemberRelationStr.toUpperCase());
                                    
                                    Optional<FamilyMember> fmOpt = familyMemberRepo.findByClient_IdAndFullNameAndRelation(
                                            client.getId(), 
                                            familyMemberName, 
                                            relation
                                    );
                                    
                                    if (fmOpt.isPresent()) {
                                        FamilyMember fm = fmOpt.get();
                                        claim.setClientId(fm.getId());
                                        claim.setClientName(fm.getFullName());
                                        patientName = fm.getFullName();
                                                        patient = client;
                                        foundFamilyMember = true;
                                    }
                                } catch (IllegalArgumentException e) {
                                                    // Invalid relation value
                                                }
                                            }
                                        }
                                    }
                                    
                                    if (radiologyRequestDto.getIsFamilyMember() != null && radiologyRequestDto.getIsFamilyMember()) {
                                        if (radiologyRequestDto.getFamilyMemberId() != null) {
                                            Optional<FamilyMember> fmOpt = familyMemberRepo.findById(radiologyRequestDto.getFamilyMemberId());
                                            
                                            if (fmOpt.isPresent()) {
                                                FamilyMember fm = fmOpt.get();
                                                if (fm.getClient() != null && fm.getClient().getId().equals(client.getId())) {
                                                    claim.setClientId(fm.getId());
                                                    claim.setClientName(fm.getFullName());
                                                    patientName = fm.getFullName();
                                                    patient = client;
                                                    foundFamilyMember = true;
                                                }
                                            }
                                        }
                                        
                                        if (!foundFamilyMember) {
                                            String familyMemberName = radiologyRequestDto.getFamilyMemberName();
                                            String familyMemberRelationStr = radiologyRequestDto.getFamilyMemberRelation();
                                            
                                            if (familyMemberName != null && familyMemberRelationStr != null) {
                                                try {
                                                    FamilyRelation relation = FamilyRelation.valueOf(familyMemberRelationStr.toUpperCase());
                                                    
                                                    Optional<FamilyMember> fmOpt = familyMemberRepo.findByClient_IdAndFullNameAndRelation(
                                                            client.getId(), 
                                                            familyMemberName, 
                                                            relation
                                                    );
                                                    
                                                    if (fmOpt.isPresent()) {
                                                        FamilyMember fm = fmOpt.get();
                                                        claim.setClientId(fm.getId());
                                                        claim.setClientName(fm.getFullName());
                                                        patientName = fm.getFullName();
                                                        patient = client;
                                                        foundFamilyMember = true;
                                                    }
                                                } catch (IllegalArgumentException e) {
                                                    // Invalid relation value
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } catch (Exception e) {
                        }
                    }
                    
                    if (!foundFamilyMember && isRadiologist && claim.getTreatmentDetails() != null) {
                        try {
                            String treatmentDetails = claim.getTreatmentDetails();
                            String familyMemberPattern = "Family Member:\\s*([^-]+?)\\s*\\(([^)]+)\\)\\s*-\\s*Insurance:";
                            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(familyMemberPattern, java.util.regex.Pattern.CASE_INSENSITIVE);
                            java.util.regex.Matcher matcher = pattern.matcher(treatmentDetails);
                            
                            if (matcher.find()) {
                                String familyMemberName = matcher.group(1).trim();
                                String familyMemberRelationStr = matcher.group(2).trim();
                                
                                try {
                                    FamilyRelation relation = FamilyRelation.valueOf(familyMemberRelationStr.toUpperCase());
                                    
                                    Optional<FamilyMember> fmOpt = familyMemberRepo.findByClient_IdAndFullNameAndRelation(
                                            client.getId(), 
                                            familyMemberName, 
                                            relation
                                    );
                                    
                                    if (fmOpt.isPresent()) {
                                        FamilyMember fm = fmOpt.get();
                                        claim.setClientId(fm.getId());
                                        claim.setClientName(fm.getFullName());
                                        patientName = fm.getFullName();
                                        patient = client;
                                        foundFamilyMember = true;
                                    }
                                } catch (IllegalArgumentException e) {
                                }
                            }
                        } catch (Exception e) {
                        }
                    }
                    
                    if (patientName == null) {
                        claim.setClientName(client.getFullName());
                        patientName = client.getFullName();
                        patient = client;
                    }
                } else {
                    patientName = null;
                }
            }
        }

        if (invoiceImage != null && !invoiceImage.isEmpty()) {
            claim.setInvoiceImagePath(saveDocument(invoiceImage));
        }

        HealthcareProviderClaim savedClaim = claimRepo.save(claim);
        final Client finalPatient = patient;
        final String finalPatientName = patientName;

        // Send notification to medical admins
        clientRepo.findByRoles_Name(RoleName.MEDICAL_ADMIN)
                .forEach(medicalAdmin -> {
                    String notificationMessage;
                    // Check if follow-up: either isFollowUp flag is true OR amount is 0 for DOCTOR claims
                    boolean isFollowUpClaim = (claim.getIsFollowUp() != null && claim.getIsFollowUp()) ||
                            (provider.getRoles().stream().anyMatch(r -> r.getName() == RoleName.DOCTOR) && 
                             claim.getAmount() != null && claim.getAmount() == 0.0);
                    
                    if (isFollowUpClaim) {
                        // Follow-up visit notification - only mention it's follow-up with 0 amount, patient pays
                        notificationMessage = "⚠️ مطالبة زيارة متابعة (Follow-up Visit) من الدكتور " + provider.getFullName() +
                                (finalPatientName != null ? " للمريض " + finalPatientName : "") +
                                " - المبلغ للدكتور: 0 شيكل (التأمين لا يدفع - المريض يدفع المبلغ)";
                    } else {
                        // Normal visit notification
                        notificationMessage = "📋 مطالبة جديدة من " + provider.getFullName() +
                                (finalPatientName != null ? " للمريض " + finalPatientName : "") +
                                " - المبلغ: " + claim.getAmount() + " شيكل";
                    }
                    notificationService.sendToUser(medicalAdmin.getId(), notificationMessage);
                });

        // Check if follow-up for provider notification
        boolean isFollowUpForProvider = (claim.getIsFollowUp() != null && claim.getIsFollowUp()) ||
                (provider.getRoles().stream().anyMatch(r -> r.getName() == RoleName.DOCTOR) && 
                 claim.getAmount() != null && claim.getAmount() == 0.0);
        
        if (isFollowUpForProvider) {
            // Follow-up visit notification for doctor
            String consultationFee = claim.getOriginalConsultationFee() != null ? 
                    claim.getOriginalConsultationFee().toString() : "0";
            notificationService.sendToUser(
                    provider.getId(),
                    "✅ تم إرسال مطالبة زيارة متابعة بنجاح - المبلغ للدكتور: 0 شيكل (التأمين لا يدفع)" +
                            (finalPatientName != null ? " - المريض " + finalPatientName + " يجب أن يدفع سعر الكشفية: " + consultationFee + " شيكل" : 
                                    " - المريض يجب أن يدفع سعر الكشفية: " + consultationFee + " شيكل") +
                            " - في انتظار المراجعة الطبية"
            );
        } else {
            // Normal visit notification for provider
            notificationService.sendToUser(
                    provider.getId(),
                    "✅ تم إرسال مطالبتك بنجاح - المبلغ: " + claim.getAmount() + " شيكل" +
                            (finalPatientName != null ? " للمريض " + finalPatientName : "") +
                            " - في انتظار المراجعة الطبية"
            );
        }

        if (finalPatient != null) {
            // Check if follow-up for patient notification
            boolean isFollowUpForPatient = (claim.getIsFollowUp() != null && claim.getIsFollowUp()) ||
                    (provider.getRoles().stream().anyMatch(r -> r.getName() == RoleName.DOCTOR) && 
                     claim.getAmount() != null && claim.getAmount() == 0.0);
            
            if (isFollowUpForPatient) {
                // Follow-up visit notification for patient
                String consultationFee = claim.getOriginalConsultationFee() != null ? 
                        claim.getOriginalConsultationFee().toString() : "0";
                notificationService.sendToUser(
                        finalPatient.getId(),
                        "📋 تم إنشاء مطالبة طبية لك من " + provider.getFullName() +
                                " - نوع الزيارة: زيارة متابعة (Follow-up Visit)" +
                                " - المبلغ للدكتور: 0 شيكل (التأمين لا يدفع)" +
                                " - يجب عليك دفع سعر الكشفية: " + consultationFee + " شيكل" +
                                " - في انتظار المراجعة"
                );
            } else {
                // Normal visit notification for patient
                notificationService.sendToUser(
                        finalPatient.getId(),
                        "📋 تم إنشاء مطالبة طبية لك من " + provider.getFullName() +
                                " - المبلغ: " + claim.getAmount() + " شيكل" +
                                " - في انتظار المراجعة"
                );
            }
        }

        HealthcareProviderClaimDTO resultDto = claimMapper.toDto(savedClaim);
        resultDto.setProviderEmployeeId(provider.getEmployeeId());
        resultDto.setProviderNationalId(provider.getNationalId());
        
        populatePatientInfo(savedClaim, resultDto);
        return resultDto;
    }

    // Create self-service claim by client
    public HealthcareProviderClaimDTO createClientClaim(
            UUID clientId,
            CreateHealthcareProviderClaimDTO dto,
            MultipartFile invoiceImage
    ) {
        Client client = clientRepo.findById(clientId)
                .orElseThrow(() -> new NotFoundException("Client not found"));

        HealthcareProviderClaim claim = claimMapper.toEntity(dto);
        claim.setHealthcareProvider(client);
        claim.setStatus(ClaimStatus.PENDING_MEDICAL);

        // Handle beneficiary: can be client themselves or a family member
        UUID beneficiaryId = dto.getClientId();
        String beneficiaryName = null;
        FamilyMember familyMember = null;

        if (beneficiaryId != null) {
            // First, try to find as a family member
            Optional<FamilyMember> familyMemberOpt = familyMemberRepo.findById(beneficiaryId);
            
            if (familyMemberOpt.isPresent()) {
                familyMember = familyMemberOpt.get();
                // Verify that this family member belongs to the authenticated client
                if (!familyMember.getClient().getId().equals(clientId)) {
                    throw new BadRequestException("Family member does not belong to this client");
                }
                // Verify family member is approved
                if (familyMember.getStatus() != com.insurancesystem.Model.Entity.Enums.ProfileStatus.APPROVED) {
                    throw new BadRequestException("Family member is not approved");
                }
                claim.setClientId(beneficiaryId);
                claim.setClientName(familyMember.getFullName());
                beneficiaryName = familyMember.getFullName();
            } else {
                // Try to find as a client
                Optional<Client> beneficiaryClientOpt = clientRepo.findById(beneficiaryId);
                if (beneficiaryClientOpt.isPresent()) {
                    Client beneficiaryClient = beneficiaryClientOpt.get();
                    // Verify it's the authenticated client themselves
                    if (!beneficiaryClient.getId().equals(clientId)) {
                        throw new BadRequestException("Cannot create claim for another client");
                    }
                    claim.setClientId(clientId);
                    claim.setClientName(client.getFullName());
                    beneficiaryName = client.getFullName();
                } else {
                    throw new NotFoundException("Beneficiary not found");
                }
            }
        } else {
            // No beneficiary specified, use the authenticated client themselves
            claim.setClientId(clientId);
            claim.setClientName(client.getFullName());
            beneficiaryName = client.getFullName();
        }

        if (invoiceImage != null && !invoiceImage.isEmpty()) {
            claim.setInvoiceImagePath(saveDocument(invoiceImage));
        }

        HealthcareProviderClaim savedClaim = claimRepo.save(claim);

        // Notification message
        String notificationMessage = familyMember != null 
            ? "📋 مطالبة جديدة من العميل " + client.getFullName() + 
              " لعضو الأسرة " + beneficiaryName +
              " - المبلغ: " + claim.getAmount() + " شيكل"
            : "📋 مطالبة جديدة من العميل " + client.getFullName() +
              " - المبلغ: " + claim.getAmount() + " شيكل";

        clientRepo.findByRoles_Name(RoleName.MEDICAL_ADMIN)
                .forEach(medicalAdmin -> notificationService.sendToUser(
                        medicalAdmin.getId(),
                        notificationMessage
                ));

        String clientNotificationMessage = familyMember != null
            ? "✅ تم إرسال مطالبة لعضو الأسرة " + beneficiaryName + " بنجاح - المبلغ: " + claim.getAmount() + " شيكل" +
              " - في انتظار المراجعة الطبية"
            : "✅ تم إرسال مطالبتك بنجاح - المبلغ: " + claim.getAmount() + " شيكل" +
              " - في انتظار المراجعة الطبية";

        notificationService.sendToUser(
                client.getId(),
                clientNotificationMessage
        );

        HealthcareProviderClaimDTO resultDto = claimMapper.toDto(savedClaim);
        resultDto.setProviderEmployeeId(client.getEmployeeId());
        resultDto.setProviderNationalId(client.getNationalId());
        
        populatePatientInfo(savedClaim, resultDto);
        return resultDto;
    }

    // Get claims for provider or client (different logic for each)
    public List<HealthcareProviderClaimDTO> getProviderClaims(UUID userId) {
        Client user = clientRepo.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        boolean isClient = user.getRoles()
                .stream()
                .anyMatch(r -> r.getName() == RoleName.INSURANCE_CLIENT);

        List<HealthcareProviderClaim> claims;

        if (isClient) {
            // Client sees all claims they created (for themselves or family members)
            // This includes claims where they are the healthcare provider
            claims = claimRepo.findByHealthcareProviderId(user.getId());
        } else {
            // Provider sees only their own claims
            claims = claimRepo.findByHealthcareProviderId(user.getId());
        }

        List<HealthcareProviderClaimDTO> result = new ArrayList<>();
        for (HealthcareProviderClaim claim : claims) {
            try {
                if (claim.getStatus() == null) {
                    claim.setStatus(ClaimStatus.PENDING_MEDICAL);
                }
                
                HealthcareProviderClaimDTO dto = claimMapper.toDto(claim);
                dto.setMedicalReviewerName(claim.getMedicalReviewerName());
                dto.setMedicalReviewedAt(claim.getMedicalReviewedAt());
                populatePatientInfo(claim, dto);
                result.add(dto);
            } catch (IllegalArgumentException e) {
                continue;
            } catch (Exception e) {
                continue;
            }
        }
        return result;
    }

    private void populatePatientInfo(HealthcareProviderClaim claim, HealthcareProviderClaimDTO dto) {
        if (claim.getClientId() == null) {
            return;
        }

        Optional<FamilyMember> familyMemberOpt = familyMemberRepo.findById(claim.getClientId());
        
        if (familyMemberOpt.isPresent()) {
            FamilyMember familyMember = familyMemberOpt.get();
            
            dto.setFamilyMemberId(familyMember.getId());
            dto.setFamilyMemberName(familyMember.getFullName());
            dto.setFamilyMemberRelation(familyMember.getRelation() != null ? familyMember.getRelation().toString() : null);
            dto.setFamilyMemberAge(calculateAge(familyMember.getDateOfBirth()));
            dto.setFamilyMemberGender(familyMember.getGender() != null ? familyMember.getGender().toString() : null);
            dto.setFamilyMemberInsuranceNumber(familyMember.getInsuranceNumber());
            dto.setFamilyMemberNationalId(familyMember.getNationalId());
            
            Client mainClient = familyMember.getClient();
            if (mainClient != null) {
                dto.setClientId(mainClient.getId());
                dto.setClientName(mainClient.getFullName());
                Integer mainClientAge = calculateAge(mainClient.getDateOfBirth());
                dto.setClientAge(mainClientAge);
                dto.setClientGender(mainClient.getGender() != null ? mainClient.getGender().toString() : null);
                dto.setClientEmployeeId(mainClient.getEmployeeId());
                dto.setClientNationalId(mainClient.getNationalId());
                dto.setClientFaculty(mainClient.getFaculty());
                dto.setClientDepartment(mainClient.getDepartment());
            }
        } else {
            clientRepo.findById(claim.getClientId()).ifPresent(client -> {
                dto.setClientId(client.getId());
                dto.setClientName(client.getFullName());
                Integer clientAge = calculateAge(client.getDateOfBirth());
                dto.setClientAge(clientAge);
                dto.setClientGender(client.getGender() != null ? client.getGender().toString() : null);
                dto.setClientEmployeeId(client.getEmployeeId());
                dto.setClientNationalId(client.getNationalId());
                dto.setClientFaculty(client.getFaculty());
                dto.setClientDepartment(client.getDepartment());
            });
        }
    }

    private void populatePatientInfoForMedicalDTO(HealthcareProviderClaim claim, HealthcareProviderClaimMedicalDTO dto) {
        if (claim.getClientId() == null) {
            return;
        }

        Optional<FamilyMember> familyMemberOpt = familyMemberRepo.findById(claim.getClientId());
        
        if (familyMemberOpt.isPresent()) {
            FamilyMember familyMember = familyMemberOpt.get();
            dto.setFamilyMemberId(familyMember.getId());
            dto.setFamilyMemberName(familyMember.getFullName());
            dto.setFamilyMemberRelation(familyMember.getRelation() != null ? familyMember.getRelation().toString() : null);
            dto.setFamilyMemberAge(calculateAge(familyMember.getDateOfBirth()));
            dto.setFamilyMemberGender(familyMember.getGender() != null ? familyMember.getGender().toString() : null);
            dto.setFamilyMemberInsuranceNumber(familyMember.getInsuranceNumber());
            dto.setFamilyMemberNationalId(familyMember.getNationalId());
            
            Client mainClient = familyMember.getClient();
            if (mainClient != null) {
                dto.setClientId(mainClient.getId());
                dto.setClientName(mainClient.getFullName());
                dto.setClientAge(calculateAge(mainClient.getDateOfBirth()));
                dto.setClientGender(mainClient.getGender() != null ? mainClient.getGender().toString() : null);
                dto.setEmployeeId(mainClient.getEmployeeId());
                dto.setClientNationalId(mainClient.getNationalId());
                dto.setClientFaculty(mainClient.getFaculty());
                dto.setClientDepartment(mainClient.getDepartment());
            }
        } else {
            clientRepo.findById(claim.getClientId()).ifPresent(client -> {
                dto.setClientId(client.getId());
                dto.setClientName(client.getFullName());
                dto.setClientAge(calculateAge(client.getDateOfBirth()));
                dto.setClientGender(client.getGender() != null ? client.getGender().toString() : null);
                dto.setEmployeeId(client.getEmployeeId());
                dto.setClientNationalId(client.getNationalId());
                dto.setClientFaculty(client.getFaculty());
                dto.setClientDepartment(client.getDepartment());
            });
        }
    }

    private Integer calculateAge(java.time.LocalDate dateOfBirth) {
        if (dateOfBirth == null) {
            return null;
        }
        java.time.LocalDate today = java.time.LocalDate.now();
        int age = today.getYear() - dateOfBirth.getYear();
        if (today.getMonthValue() < dateOfBirth.getMonthValue() ||
                (today.getMonthValue() == dateOfBirth.getMonthValue() && today.getDayOfMonth() < dateOfBirth.getDayOfMonth())) {
            age--;
        }
        return age > 0 ? age : null;
    }

    // Helper: Get provider role (handles self-service client claims)
    private String getProviderRole(HealthcareProviderClaim claim) {
        if (claim.getClientId() != null &&
                claim.getHealthcareProvider().getId().equals(claim.getClientId())) {
            return "INSURANCE_CLIENT";
        }
        return claim.getHealthcareProvider()
                .getRoles()
                .stream()
                .findFirst()
                .map(r -> r.getName().name())
                .orElse("UNKNOWN");
    }

    public List<HealthcareProviderClaimDTO> getAllClaims() {
        return claimRepo.findAll()
                .stream()
                .map(claim -> {
                    HealthcareProviderClaimDTO dto = claimMapper.toDto(claim);
                    populatePatientInfo(claim, dto);
                    return dto;
                })
                .toList();
    }

    public HealthcareProviderClaimDTO getClaim(UUID id, UUID requesterId, boolean isManager) {
        HealthcareProviderClaim claim = claimRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Claim not found"));

        if (!isManager && !claim.getHealthcareProvider().getId().equals(requesterId))
            throw new NotFoundException("Claim not found for this provider");

        HealthcareProviderClaimDTO dto = claimMapper.toDto(claim);
        populatePatientInfo(claim, dto);
        return dto;
    }

    // Medical admin rejects claim
    public HealthcareProviderClaimDTO rejectMedical(UUID claimId, String reason, UUID reviewerId) {
        HealthcareProviderClaim claim = claimRepo.findById(claimId)
                .orElseThrow(() -> new NotFoundException("Claim not found"));

        if (claim.getStatus() != ClaimStatus.PENDING_MEDICAL &&
                claim.getStatus() != ClaimStatus.RETURNED_FOR_REVIEW) {
            throw new BadRequestException("Claim was already processed");
        }

        Client reviewer = clientRepo.findById(reviewerId)
                .orElseThrow(() -> new NotFoundException("Reviewer not found"));

        claim.setStatus(ClaimStatus.REJECTED_FINAL);
        claim.setRejectedAt(Instant.now());
        claim.setRejectionReason(reason);
        claim.setMedicalReviewerId(reviewerId);
        claim.setMedicalReviewerName(reviewer.getFullName());
        claim.setMedicalReviewedAt(Instant.now());

        HealthcareProviderClaim savedClaim = claimRepo.save(claim);

        notificationService.sendToUser(
                claim.getHealthcareProvider().getId(),
                "❌ تم رفض مطالبتك من المراجع الطبي " + reviewer.getFullName() +
                        " - المبلغ: " + claim.getAmount() + " شيكل" +
                        (reason != null && !reason.isEmpty() ? "\nالسبب: " + reason : "")
        );

        if (claim.getClientId() != null) {
            clientRepo.findById(claim.getClientId()).ifPresent(patient ->
                    notificationService.sendToUser(
                            patient.getId(),
                            "❌ تم رفض مطالبتك الطبية من " + claim.getHealthcareProvider().getFullName() +
                                    " - السبب: " + (reason != null && !reason.isEmpty() ? reason : "غير محدد")
                    )
            );
        }

        HealthcareProviderClaimDTO resultDto = claimMapper.toDto(savedClaim);
        populatePatientInfo(savedClaim, resultDto);
        return resultDto;
    }

    // Medical admin approves claim (moves to coordination review)
    public HealthcareProviderClaimDTO approveMedical(UUID claimId, UUID reviewerId) {
        HealthcareProviderClaim claim = claimRepo.findById(claimId)
                .orElseThrow(() -> new NotFoundException("Claim not found"));

        if (claim.getStatus() != ClaimStatus.PENDING_MEDICAL &&
                claim.getStatus() != ClaimStatus.RETURNED_FOR_REVIEW) {
            throw new BadRequestException("Claim already processed");
        }

        Client reviewer = clientRepo.findById(reviewerId)
                .orElseThrow(() -> new NotFoundException("Reviewer not found"));

        claim.setMedicalReviewerId(reviewerId);
        claim.setMedicalReviewerName(reviewer.getFullName());
        claim.setMedicalReviewedAt(Instant.now());
        claim.setStatus(ClaimStatus.AWAITING_COORDINATION_REVIEW);

        HealthcareProviderClaim savedClaim = claimRepo.save(claim);

        // 🔔 إشعار لمقدم الخدمة مع معلومات follow-up
        String notificationMessage = "✅ تمت الموافقة على مطالبتك من المراجع الطبي " + reviewer.getFullName();
        if (claim.getIsFollowUp() != null && claim.getIsFollowUp()) {
            notificationMessage += " - ⚠️ زيارة متابعة (Follow-up): المريض يجب أن يدفع سعر الكشفية (" + 
                    (claim.getOriginalConsultationFee() != null ? claim.getOriginalConsultationFee() : "0") + 
                    " دينار). التأمين لا يدفع الكشفية في زيارة المتابعة.";
        } else {
            notificationMessage += " - المبلغ: " + claim.getAmount() + " شيكل";
        }
        notificationMessage += " - الآن في انتظار مراجعة المنسق الإداري";
        
        notificationService.sendToUser(
                claim.getHealthcareProvider().getId(),
                notificationMessage
        );
        
        // 🔔 إشعار للمنسقين الإداريين
        clientRepo.findByRoles_Name(RoleName.COORDINATION_ADMIN)
                .forEach(coordinator ->
                        notificationService.sendToUser(
                                coordinator.getId(),
                                "🔔 مطالبة جديدة في انتظار المراجعة الإدارية\n" +
                                        "من: " + claim.getHealthcareProvider().getFullName() + "\n" +
                                        "المبلغ: " + claim.getAmount() + " شيكل" +
                                        (claim.getClientName() != null ? "\nللمريض: " + claim.getClientName() : "")
                        )
                );

        if (claim.getClientId() != null) {
            clientRepo.findById(claim.getClientId()).ifPresent(patient ->
                    notificationService.sendToUser(
                            patient.getId(),
                            "✅ تمت الموافقة الطبية على مطالبتك من " + claim.getHealthcareProvider().getFullName() +
                                    " - الآن في انتظار مراجعة المنسق الإداري"
                    )
            );
        }

        HealthcareProviderClaimDTO resultDto = claimMapper.toDto(savedClaim);
        populatePatientInfo(savedClaim, resultDto);
        return resultDto;
    }

    // Get claims pending medical review
    public List<HealthcareProviderClaimMedicalDTO> getClaimsForMedicalReview() {
        List<HealthcareProviderClaim> claims = claimRepo.findByStatusInWithProvider(
                List.of(ClaimStatus.PENDING_MEDICAL, ClaimStatus.RETURNED_FOR_REVIEW)
                );

        return claims.stream().map(claim -> {
            HealthcareProviderClaimMedicalDTO dto = claimMapper.toMedicalDto(claim);

            dto.setDiagnosis(claim.getDiagnosis());
            dto.setTreatmentDetails(cleanTreatmentDetails(claim.getTreatmentDetails()));
            populatePatientInfoForMedicalDTO(claim, dto);
            dto.setProviderRole(getProviderRole(claim));
            populateProviderInfo(claim, dto);
            dto.setDescription(claim.getDescription());
            dto.setRoleSpecificData(claim.getRoleSpecificData());
            dto.setIsFollowUp(claim.getIsFollowUp());
            dto.setOriginalConsultationFee(claim.getOriginalConsultationFee());

            return dto;
        }).toList();
    }

    // Get final decisions (approved/rejected by medical admin)
    public List<HealthcareProviderClaimMedicalDTO> getFinalDecisions() {
        List<ClaimStatus> statuses = List.of(
                ClaimStatus.APPROVED_FINAL,
                ClaimStatus.REJECTED_FINAL
        );

        List<HealthcareProviderClaim> claims = claimRepo.findByStatusInWithProvider(statuses);

        return claims.stream().map(claim -> {
            HealthcareProviderClaimMedicalDTO dto = claimMapper.toMedicalDto(claim);
            dto.setAmount(claim.getAmount());
            dto.setIsFollowUp(claim.getIsFollowUp());
            dto.setOriginalConsultationFee(claim.getOriginalConsultationFee());

            if (claim.getStatus() == ClaimStatus.RETURNED_FOR_REVIEW) {
                dto.setReturnedByCoordinator(true);
                dto.setCoordinatorNote(claim.getRejectionReason());
            } else {
                dto.setReturnedByCoordinator(false);
                dto.setCoordinatorNote(null);
            }
            
            populatePatientInfoForMedicalDTO(claim, dto);
            dto.setProviderRole(getProviderRole(claim));
            populateProviderInfo(claim, dto);
            dto.setDescription(claim.getDescription());
            dto.setRoleSpecificData(claim.getRoleSpecificData());
            dto.setIsFollowUp(claim.getIsFollowUp());
            dto.setOriginalConsultationFee(claim.getOriginalConsultationFee());

            return dto;
        }).toList();
    }

    private void populateProviderInfo(HealthcareProviderClaim claim, HealthcareProviderClaimMedicalDTO dto) {
        Client provider = claim.getHealthcareProvider();
        if (provider != null) {
            String employeeId = provider.getEmployeeId();
            String nationalId = provider.getNationalId();
            
            dto.setProviderEmployeeId(employeeId);
            dto.setProviderNationalId(nationalId);
            
            String role = dto.getProviderRole();
            
            // For client claims (outside network), extract provider and doctor name from roleSpecificData
            if (role != null && role.equals("INSURANCE_CLIENT") && claim.getRoleSpecificData() != null) {
                try {
                    java.util.Map<String, Object> roleData = objectMapper.readValue(
                            claim.getRoleSpecificData(), 
                            java.util.Map.class
                    );
                    String providerName = (String) roleData.get("providerName");
                    String doctorName = (String) roleData.get("doctorName");
                    
                    if (providerName != null && !providerName.trim().isEmpty()) {
                        dto.setProviderName(providerName);
                    } else {
                        dto.setProviderName(provider.getFullName());
                    }
                    
                    // Set doctor name if available
                    if (doctorName != null && !doctorName.trim().isEmpty()) {
                        dto.setDoctorName(doctorName);
                    }
                } catch (Exception e) {
                    // If parsing fails, use provider's name
                    dto.setProviderName(provider.getFullName());
                }
            } else {
                // For regular providers, use their name
                dto.setProviderName(provider.getFullName());
            }
            
            if (role != null) {
                if (role.equals("DOCTOR")) {
                    dto.setProviderSpecialization(provider.getSpecialization());
                } else if (role.equals("PHARMACIST")) {
                    dto.setProviderPharmacyCode(provider.getPharmacyCode() != null && !provider.getPharmacyCode().trim().isEmpty() 
                            ? provider.getPharmacyCode() : null);
                } else if (role.equals("LAB_TECH")) {
                    dto.setProviderLabCode(provider.getLabCode() != null && !provider.getLabCode().trim().isEmpty() 
                            ? provider.getLabCode() : null);
                } else if (role.equals("RADIOLOGIST")) {
                    dto.setProviderRadiologyCode(provider.getRadiologyCode() != null && !provider.getRadiologyCode().trim().isEmpty() 
                            ? provider.getRadiologyCode() : null);
                }
            }
        }
    }

    private String cleanTreatmentDetails(String treatmentDetails) {
        if (treatmentDetails == null || treatmentDetails.isEmpty()) {
            return treatmentDetails;
        }
        
        String cleaned = treatmentDetails.replaceAll("(?i)(\\r?\\n)?\\s*Family\\s+Member:.*?-\\s*Insurance:.*?-\\s*Age:.*?-\\s*Gender:.*?(?=\\r?\\n|$|\\z)", "");
        cleaned = cleaned.replaceAll("(?i)(\\r?\\n)?\\s*Family\\s+Member:.*?-\\s*Insurance:.*?-\\s*Age:.*?-\\s*Gender:.*$", "");
        cleaned = cleaned.replaceAll("(?i)^\\s*Family\\s+Member:.*?-\\s*Insurance:.*?-\\s*Age:.*?-\\s*Gender:.*?(?=\\r?\\n|$)", "");
        cleaned = cleaned.replaceAll("\\r?\\n\\r?\\n+", "\n");
        cleaned = cleaned.replaceAll("\\s+", " ");
        cleaned = cleaned.trim();
        
        return cleaned.isEmpty() ? null : cleaned;
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
<<<<<<< HEAD

    // Get claims for medical review (PENDING or RETURNED_FOR_REVIEW)
    public List<HealthcareProviderClaimDTO> getClaimsForMedicalReview() {
        List<ClaimStatus> statuses = List.of(
                ClaimStatus.PENDING,
                ClaimStatus.PENDING_MEDICAL,
                ClaimStatus.RETURNED_FOR_REVIEW
        );
        return claimRepo.findByStatusIn(statuses).stream()
                .map(claimMapper::toDto)
                .toList();
    }

    // Get claims for coordination review
    public List<HealthcareProviderClaimDTO> getClaimsForCoordinationReview() {
        return claimRepo.findClaimsForCoordinationReview().stream()
                .map(claimMapper::toDto)
                .toList();
    }

    // Get final decisions
    public List<HealthcareProviderClaimDTO> getFinalDecisions() {
        return claimRepo.findFinalDecisions().stream()
                .map(claimMapper::toDto)
                .toList();
    }

    // Medical approve claim
    public HealthcareProviderClaimDTO approveMedical(UUID claimId) {
        HealthcareProviderClaim claim = claimRepo.findById(claimId)
                .orElseThrow(() -> new NotFoundException("Claim not found"));

        claim.setStatus(ClaimStatus.APPROVED_MEDICAL);
        claim.setMedicalReviewedAt(Instant.now());
        claimRepo.save(claim);

        notificationService.sendToRole(
                RoleName.COORDINATION_ADMIN,
                "Medical approved claim from " + claim.getHealthcareProvider().getFullName()
        );

        return claimMapper.toDto(claim);
    }

    // Medical reject claim
    public HealthcareProviderClaimDTO rejectMedical(UUID claimId, String reason) {
        HealthcareProviderClaim claim = claimRepo.findById(claimId)
                .orElseThrow(() -> new NotFoundException("Claim not found"));

        claim.setStatus(ClaimStatus.REJECTED_MEDICAL);
        claim.setRejectedAt(Instant.now());
        claim.setRejectionReason(reason);
        claimRepo.save(claim);

        notificationService.sendToUser(
                claim.getHealthcareProvider().getId(),
                "Your claim was rejected by medical review. Reason: " + reason
        );

        return claimMapper.toDto(claim);
    }

    // Final approve claim (coordination admin)
    public HealthcareProviderClaimDTO approveFinal(UUID claimId) {
        HealthcareProviderClaim claim = claimRepo.findById(claimId)
                .orElseThrow(() -> new NotFoundException("Claim not found"));

        claim.setStatus(ClaimStatus.APPROVED_FINAL);
        claim.setApprovedAt(Instant.now());
        claimRepo.save(claim);

        notificationService.sendToUser(
                claim.getHealthcareProvider().getId(),
                "Your claim of " + claim.getAmount() + " has been fully approved!"
        );

        return claimMapper.toDto(claim);
    }

    // Final reject claim (coordination admin)
    public HealthcareProviderClaimDTO rejectFinal(UUID claimId, String reason) {
        HealthcareProviderClaim claim = claimRepo.findById(claimId)
                .orElseThrow(() -> new NotFoundException("Claim not found"));

        claim.setStatus(ClaimStatus.REJECTED_FINAL);
        claim.setRejectedAt(Instant.now());
        claim.setRejectionReason(reason);
        claimRepo.save(claim);

        notificationService.sendToUser(
                claim.getHealthcareProvider().getId(),
                "Your claim was rejected. Reason: " + reason
        );

        return claimMapper.toDto(claim);
    }

    // Return to medical for review
    public HealthcareProviderClaimDTO returnToMedical(UUID claimId, String reason) {
        HealthcareProviderClaim claim = claimRepo.findById(claimId)
                .orElseThrow(() -> new NotFoundException("Claim not found"));

        claim.setStatus(ClaimStatus.RETURNED_FOR_REVIEW);
        claim.setRejectionReason(reason);
        claimRepo.save(claim);

        notificationService.sendToRole(
                RoleName.MEDICAL_ADMIN,
                "Claim returned for medical review. Reason: " + reason
        );

        return claimMapper.toDto(claim);
    }
}
=======
    // 📤 Export Approved Claims as PDF
    public byte[] exportApprovedClaimsPdf() {
        List<HealthcareProviderClaim> claims = claimRepo.findAllApprovedClaims();
>>>>>>> 59fc73de7f549007a5658aab4146b5707a8a4bd8

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
                table.addCell(claim.getClientName() != null ? claim.getClientName() : "-");
                table.addCell(claim.getHealthcareProvider().getFullName());
                table.addCell(claim.getMedicalReviewerName() != null ? claim.getMedicalReviewerName() : "-");
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

    private byte[] generatePdf(ReportType reportType, List<HealthcareProviderClaim> claims) {
        boolean hideClientName = reportType != ReportType.CLIENT;

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(document, out);

            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            document.add(new Paragraph(reportType.name() + " Claims Report", titleFont));
            document.add(new Paragraph(" "));
            document.add(new Paragraph("Generated by Coordination Admin"));
            document.add(new Paragraph(" "));

            int columns = hideClientName ? 5 : 6;
            PdfPTable table = new PdfPTable(columns);
            table.setWidthPercentage(100);

            table.addCell("Claim ID");
            table.addCell("Provider Name");

            if (!hideClientName) {
                table.addCell("Client Name");
            }

            table.addCell("Amount");
            table.addCell("Status");
            table.addCell("Service Date");

            for (HealthcareProviderClaim claim : claims) {
                table.addCell(claim.getId().toString());
                table.addCell(claim.getHealthcareProvider().getFullName());

                if (!hideClientName) {
                    table.addCell(claim.getClientName() != null ? claim.getClientName() : "-");
                }

                table.addCell(claim.getAmount() + " NIS");
                table.addCell(claim.getStatus().name());
                table.addCell(claim.getServiceDate() != null ? claim.getServiceDate().toString() : "-");
            }

            document.add(table);
            document.close();

            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF report", e);
        }
    }

    public byte[] exportReportPdf(ReportType reportType, ClaimStatus status, LocalDate from, LocalDate to) {
        RoleName roleFilter = switch (reportType) {
            case DOCTOR -> RoleName.DOCTOR;
            case PHARMACY -> RoleName.PHARMACIST;
            case LAB -> RoleName.LAB_TECH;
            case RADIOLOGY -> RoleName.RADIOLOGIST;
            case CLIENT -> null; // clients handled separately
        };

        List<HealthcareProviderClaim> claims = claimRepo.filterClaims(status, from, to, roleFilter);

        return generatePdf(reportType, claims);
    }

    // Coordination admin creates claim on behalf of client
    public HealthcareProviderClaimDTO createClaimByCoordinationAdmin(
            UUID adminId,
            CreateHealthcareProviderClaimDTO dto,
            MultipartFile invoiceImage
    ) {
        Client admin = clientRepo.findById(adminId)
                .orElseThrow(() -> new NotFoundException("Coordination admin not found"));

        boolean isCoordinator = admin.getRoles().stream()
                .anyMatch(r -> r.getName() == RoleName.COORDINATION_ADMIN);

        if (!isCoordinator) {
            throw new BadRequestException("Only coordination admin can create claims this way");
        }

        if (dto.getClientId() == null) {
            throw new BadRequestException("Client ID is required for coordination admin claim");
        }

        // First, try to find as a family member
        Optional<FamilyMember> familyMemberOpt = familyMemberRepo.findById(dto.getClientId());
        Client client;
        FamilyMember familyMember = null;
        String beneficiaryName;
        
        if (familyMemberOpt.isPresent()) {
            // It's a family member
            familyMember = familyMemberOpt.get();
            // Verify family member is approved
            if (familyMember.getStatus() != com.insurancesystem.Model.Entity.Enums.ProfileStatus.APPROVED) {
                throw new BadRequestException("Family member is not approved");
            }
            client = familyMember.getClient();
            beneficiaryName = familyMember.getFullName();
        } else {
            // It's a regular client
            client = clientRepo.findById(dto.getClientId())
                    .orElseThrow(() -> new NotFoundException("Client not found"));
            beneficiaryName = client.getFullName();
        }

        HealthcareProviderClaim claim = claimMapper.toEntity(dto);
        // Set healthcare provider to the client (not admin) so it shows in client's claims
        claim.setHealthcareProvider(client);
        
        if (familyMember != null) {
            // Claim is for family member
            claim.setClientId(familyMember.getId());
            claim.setClientName(familyMember.getFullName());
        } else {
            // Claim is for client themselves
            claim.setClientId(client.getId());
            claim.setClientName(client.getFullName());
        }
        
        // Coordinator admin creates claim with APPROVED status directly
        claim.setStatus(ClaimStatus.APPROVED_FINAL);
        claim.setApprovedAt(Instant.now());
        claim.setMedicalReviewerId(admin.getId());
        claim.setMedicalReviewerName(admin.getFullName());
        claim.setMedicalReviewedAt(Instant.now());

        if (invoiceImage != null && !invoiceImage.isEmpty()) {
            claim.setInvoiceImagePath(saveDocument(invoiceImage));
        }

        HealthcareProviderClaim savedClaim = claimRepo.save(claim);
        
        // Notification message for client
        String clientNotificationMessage = familyMember != null 
            ? "✅ تم إنشاء واعتماد مطالبة من المنسق الإداري " + admin.getFullName() + 
              " لعضو الأسرة " + beneficiaryName +
              " - المبلغ: " + claim.getAmount() + " شيكل" +
              " - تمت الموافقة مباشرة"
            : "✅ تم إنشاء واعتماد مطالبة من المنسق الإداري " + admin.getFullName() +
              " للعميل " + beneficiaryName +
              " - المبلغ: " + claim.getAmount() + " شيكل" +
              " - تمت الموافقة مباشرة";

        // Notify the client
        notificationService.sendToUser(
                client.getId(),
                clientNotificationMessage
        );
        
        // Notify medical admins about the approved claim created by coordinator
        String medicalAdminNotificationMessage = familyMember != null
            ? "✅ تم إنشاء واعتماد مطالبة من المنسق الإداري " + admin.getFullName() +
              " لعضو الأسرة " + beneficiaryName + " (العميل: " + client.getFullName() + ")" +
              " - المبلغ: " + claim.getAmount() + " شيكل" +
              " - تمت الموافقة مباشرة من المنسق الإداري"
            : "✅ تم إنشاء واعتماد مطالبة من المنسق الإداري " + admin.getFullName() +
              " للعميل " + beneficiaryName +
              " - المبلغ: " + claim.getAmount() + " شيكل" +
              " - تمت الموافقة مباشرة من المنسق الإداري";
        
        clientRepo.findByRoles_Name(RoleName.MEDICAL_ADMIN)
                .forEach(medicalAdmin -> notificationService.sendToUser(
                        medicalAdmin.getId(),
                        medicalAdminNotificationMessage
                ));
        
        HealthcareProviderClaimDTO resultDto = claimMapper.toDto(savedClaim);
        
        if (claim.getHealthcareProvider() != null) {
            resultDto.setProviderEmployeeId(claim.getHealthcareProvider().getEmployeeId());
            resultDto.setProviderNationalId(claim.getHealthcareProvider().getNationalId());
        }
        
        populatePatientInfo(savedClaim, resultDto);
        return resultDto;
    }

    // Coordinator admin approves claim (sets to final approval)
    public HealthcareProviderClaimDTO approveAdmin(UUID claimId, UUID reviewerId) {
        HealthcareProviderClaim claim = claimRepo.findById(claimId)
                .orElseThrow(() -> new NotFoundException("Claim not found"));

        if (claim.getStatus() != ClaimStatus.AWAITING_COORDINATION_REVIEW) {
            throw new BadRequestException("Claim is not awaiting coordination review");
        }

        Client reviewer = clientRepo.findById(reviewerId)
                .orElseThrow(() -> new NotFoundException("Reviewer not found"));

        claim.setStatus(ClaimStatus.APPROVED_FINAL);
        claim.setApprovedAt(Instant.now());

        HealthcareProviderClaim savedClaim = claimRepo.save(claim);

        // 🔔 إشعار لمقدم الخدمة
        notificationService.sendToUser(
                claim.getHealthcareProvider().getId(),
                "✅ تمت الموافقة النهائية على مطالبتك من المنسق الإداري " + reviewer.getFullName() +
                        " - المبلغ: " + claim.getAmount() + " شيكل" +
                        (claim.getClientName() != null ? " للمريض " + claim.getClientName() : "") +
                        " - تمت الموافقة بنجاح!"
        );

        // 🔔 إشعار للمريض (إن وجد)
        if (claim.getClientId() != null) {
            clientRepo.findById(claim.getClientId()).ifPresent(patient ->
                    notificationService.sendToUser(
                            patient.getId(),
                            "✅ تمت الموافقة النهائية على مطالبتك الطبية من " + claim.getHealthcareProvider().getFullName() +
                                    " - تمت الموافقة بنجاح!"
                    )
            );
        }

        HealthcareProviderClaimDTO resultDto = claimMapper.toDto(savedClaim);
        populatePatientInfo(savedClaim, resultDto);
        return resultDto;
    }

    // Coordinator admin rejects claim
    public HealthcareProviderClaimDTO rejectAdmin(UUID claimId, String reason, UUID reviewerId) {
        HealthcareProviderClaim claim = claimRepo.findById(claimId)
                .orElseThrow(() -> new NotFoundException("Claim not found"));

        if (claim.getStatus() != ClaimStatus.AWAITING_COORDINATION_REVIEW) {
            throw new BadRequestException("Claim is not awaiting coordination review");
        }

        Client reviewer = clientRepo.findById(reviewerId)
                .orElseThrow(() -> new NotFoundException("Reviewer not found"));

        claim.setStatus(ClaimStatus.REJECTED_FINAL);
        claim.setRejectedAt(Instant.now());
        claim.setRejectionReason(reason);

        HealthcareProviderClaim savedClaim = claimRepo.save(claim);

        notificationService.sendToUser(
                claim.getHealthcareProvider().getId(),
                "❌ تم رفض مطالبتك من المنسق الإداري " + reviewer.getFullName() +
                        " - المبلغ: " + claim.getAmount() + " شيكل" +
                        (reason != null && !reason.isEmpty() ? "\nالسبب: " + reason : "")
        );

        if (claim.getClientId() != null) {
            clientRepo.findById(claim.getClientId()).ifPresent(patient ->
                    notificationService.sendToUser(
                            patient.getId(),
                            "❌ تم رفض مطالبتك الطبية من " + claim.getHealthcareProvider().getFullName() +
                                    " - السبب: " + (reason != null && !reason.isEmpty() ? reason : "غير محدد")
                    )
            );
        }

        HealthcareProviderClaimDTO resultDto = claimMapper.toDto(savedClaim);
        populatePatientInfo(savedClaim, resultDto);
        return resultDto;
    }

    public HealthcareProviderClaimDTO returnToMedical(UUID claimId, String reason, UUID coordinatorId) {
        HealthcareProviderClaim claim = claimRepo.findById(claimId)
                .orElseThrow(() -> new NotFoundException("Claim not found"));

        if (claim.getStatus() != ClaimStatus.AWAITING_COORDINATION_REVIEW) {
            throw new BadRequestException("Only claims awaiting coordination review can be returned");
        }

        claim.setStatus(ClaimStatus.RETURNED_FOR_REVIEW);
        claim.setRejectionReason(reason);
        claim.setRejectedAt(Instant.now());

        HealthcareProviderClaim saved = claimRepo.save(claim);

        clientRepo.findByRoles_Name(RoleName.MEDICAL_ADMIN)
                .forEach(admin ->
                        notificationService.sendToUser(
                                admin.getId(),
                                "🚨 مراجعة طبية عاجلة\n" +
                                        "تم إرجاع مطالبة من المنسق الإداري.\n\n" +
                                        "📝 ملاحظة:\n" + reason
                        )
                );

        notificationService.sendToUser(
                claim.getHealthcareProvider().getId(),
                "⚠️ تمت إعادة مطالبتك للمراجعة الطبية بسبب ملاحظة إدارية:\n" + reason
        );

        HealthcareProviderClaimDTO resultDto = claimMapper.toDto(saved);
        populatePatientInfo(saved, resultDto);
        return resultDto;
    }

    // Get claims approved by medical admin for coordination review
    public List<HealthcareProviderClaimDTO> getClaimsForCoordinationReview() {
        List<HealthcareProviderClaim> claims = claimRepo.findByStatus(ClaimStatus.AWAITING_COORDINATION_REVIEW);

        return claims.stream().map(claim -> {
            HealthcareProviderClaimDTO dto = claimMapper.toDto(claim);

            populatePatientInfo(claim, dto);
            dto.setProviderRole(getProviderRole(claim));
            

            dto.setInvoiceImagePath(claim.getInvoiceImagePath());

            // ✅ تحديد دور مقدم الخدمة
            String role;
            if (claim.getClientId() != null &&
                    claim.getHealthcareProvider().getId().equals(claim.getClientId())) {

                // Self-service client claim
                role = RoleName.INSURANCE_CLIENT.name();

            } else {
                // Provider claim
                role = claim.getHealthcareProvider()
                        .getRoles()
                        .stream()
                        .findFirst()
                        .map(r -> r.getName().name())
                        .orElse("UNKNOWN");
            }

            if (claim.getClientId() != null) {
                clientRepo.findById(claim.getClientId())
                        .ifPresent(c -> dto.setEmployeeId(c.getEmployeeId()));
            }

            return dto;
        }).toList();
    }
}
