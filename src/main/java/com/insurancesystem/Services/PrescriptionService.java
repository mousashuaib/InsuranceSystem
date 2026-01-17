package com.insurancesystem.Services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.insurancesystem.Exception.NotFoundException;
import com.insurancesystem.Model.Dto.ClientDto;
import com.insurancesystem.Model.Dto.PrescriptionDTO;
import com.insurancesystem.Model.Dto.PrescriptionItemDTO;
import com.insurancesystem.Model.Dto.UpdateUserDTO;
import com.insurancesystem.Model.Entity.Client;
import com.insurancesystem.Model.Entity.Enums.PrescriptionStatus;
import com.insurancesystem.Model.Entity.Enums.RoleName;
import com.insurancesystem.Model.Entity.FamilyMember;
import com.insurancesystem.Model.Entity.Prescription;
import com.insurancesystem.Model.Entity.PrescriptionItem;
import com.insurancesystem.Model.Entity.PriceList;
import com.insurancesystem.Model.MapStruct.ClientMapper;
import com.insurancesystem.Model.MapStruct.PrescriptionMapper;
import com.insurancesystem.Repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepo;
    private final PrescriptionItemRepository prescriptionItemRepo;
    private final ClientRepository clientRepo;
    private final PriceListRepository priceListRepo;
    private final FamilyMemberRepository familyMemberRepo;
    private final PrescriptionMapper prescriptionMapper;
    private final ClientMapper clientMapper;
    private final NotificationService notificationService;
    private final PrescriptionQuantityCalculator quantityCalculator;
    private final ObjectMapper json = new ObjectMapper();

    private int extractQuantity(String jsonStr) {
        try {
            if (jsonStr == null) return 1;
            Map<String, Object> data = json.readValue(jsonStr, Map.class);
            return data.get("quantity") != null ? (int) data.get("quantity") : 1;
        } catch (Exception e) {
            return 1;
        }
    }

    // ➕ Doctor creates prescription
    @Transactional
    public PrescriptionDTO create(PrescriptionDTO dto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String doctorUsername = auth.getName();

        Client doctor = clientRepo.findByEmail(doctorUsername.toLowerCase())
                .orElseThrow(() -> new NotFoundException("DOCTOR_NOT_FOUND"));
        
        return createPrescriptionWithDoctor(dto, doctor);
    }
    
    // ✅ إنشاء وصفة طبية من Medical Admin (للجداول التلقائية)
    @Transactional
    public PrescriptionDTO createPrescriptionWithDoctor(PrescriptionDTO dto, Client doctor) {
        Client member;
        FamilyMember familyMember = null;
        String familyMemberInfo = "";

        if (dto.getMemberId() != null) {
            // First try to find as a Client
            Optional<Client> clientOpt = clientRepo.findById(dto.getMemberId());

            if (clientOpt.isPresent()) {
                member = clientOpt.get();
                
                // Force initialization of dateOfBirth and gender to ensure they're loaded
                try {
                    UUID memberId = member.getId();
                    String memberName = member.getFullName();
                    java.time.LocalDate dob = member.getDateOfBirth();
                    String gender = member.getGender();
                    
                    log.info("🔍 [PRESCRIPTION CREATE] Member found by ID: {} (ID: {}) | DOB: {} | Gender: {}", 
                        memberName, memberId, dob, gender);
                    
                    // If dateOfBirth or gender are null, reload the member to ensure we have the latest data
                    if (dob == null || gender == null || gender.trim().isEmpty()) {
                        log.warn("⚠️ [PRESCRIPTION CREATE] Member {} missing DOB/Gender, reloading from DB", memberName);
                        Client reloadedMember = clientRepo.findById(memberId).orElse(null);
                        if (reloadedMember != null) {
                            member = reloadedMember;
                            log.info("✅ [PRESCRIPTION CREATE] Reloaded member - DOB: {} | Gender: {}", 
                                reloadedMember.getDateOfBirth(), reloadedMember.getGender());
                        }
                    }
                } catch (Exception e) {
                    log.error("❌ [PRESCRIPTION CREATE] Error accessing member fields: {}", e.getMessage());
                    // Continue with the member as is - the mapper will handle it
                }
            } else {
                // If not found as Client, try to find as FamilyMember
                Optional<FamilyMember> familyMemberOpt = familyMemberRepo.findById(dto.getMemberId());

                if (familyMemberOpt.isPresent()) {
                    familyMember = familyMemberOpt.get();
                    // Get the main client (the family member's client)
                    member = familyMember.getClient();

                    // Calculate age from date of birth
                    int age = -1;
                    if (familyMember.getDateOfBirth() != null) {
                        java.time.LocalDate today = java.time.LocalDate.now();
                        java.time.LocalDate birthDate = familyMember.getDateOfBirth();
                        age = today.getYear() - birthDate.getYear();
                        if (today.getMonthValue() < birthDate.getMonthValue() ||
                                (today.getMonthValue() == birthDate.getMonthValue() && today.getDayOfMonth() < birthDate.getDayOfMonth())) {
                            age--;
                        }
                    }

                    // Store family member info for notes with age and gender
                    String ageStr = age > 0 ? age + " years" : "N/A";
                    String genderStr = familyMember.getGender() != null ? familyMember.getGender().toString() : "N/A";

                    familyMemberInfo = String.format(
                            "\nFamily Member: %s (%s) - Insurance: %s - Age: %s - Gender: %s",
                            familyMember.getFullName(),
                            familyMember.getRelation(),
                            familyMember.getInsuranceNumber(),
                            ageStr,
                            genderStr
                    );
                } else {
                    throw new NotFoundException("MEMBER_NOT_FOUND");
                }
            }
        } else {
            // Find member by full name and ensure all fields are loaded
            member = clientRepo.findByFullName(dto.getMemberName())
                    .orElseThrow(() -> new NotFoundException("MEMBER_NOT_FOUND"));
            
            // Force initialization of dateOfBirth and gender to ensure they're loaded
            // This prevents LazyInitializationException later
            try {
                UUID memberId = member.getId();
                String memberName = member.getFullName();
                java.time.LocalDate dob = member.getDateOfBirth();
                String gender = member.getGender();
                
                log.info("🔍 [PRESCRIPTION CREATE] Member found: {} (ID: {}) | DOB: {} | Gender: {}", 
                    memberName, memberId, dob, gender);
                
                // If dateOfBirth or gender are null, reload the member to ensure we have the latest data
                if (dob == null || gender == null || gender.trim().isEmpty()) {
                    log.warn("⚠️ [PRESCRIPTION CREATE] Member {} missing DOB/Gender, reloading from DB", memberName);
                    Client reloadedMember = clientRepo.findById(memberId).orElse(null);
                    if (reloadedMember != null) {
                        member = reloadedMember;
                        log.info("✅ [PRESCRIPTION CREATE] Reloaded member - DOB: {} | Gender: {}", 
                            reloadedMember.getDateOfBirth(), reloadedMember.getGender());
                    }
                }
            } catch (Exception e) {
                log.error("❌ [PRESCRIPTION CREATE] Error accessing member fields: {}", e.getMessage());
                // Continue with the member as is - the mapper will handle it
            }
        }

        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            throw new IllegalArgumentException("PRESCRIPTION_MUST_HAVE_MEDICINES");
        }

        // Validate that no medicine is blocked (already dispensed within duration period)
        // If this is a family member prescription, check using family member's name, otherwise use main client's name
        String memberNameToCheck = (familyMember != null) ? familyMember.getFullName() : member.getFullName();
        log.info("🔍 [PRESCRIPTION CREATE] Checking prescriptions for: {} (Family Member: {})", 
            memberNameToCheck, familyMember != null ? "Yes" : "No");
        
        for (PrescriptionItemDTO itemDto : dto.getItems()) {
            Map<String, Object> checkResult = checkActivePrescription(memberNameToCheck, itemDto.getMedicineId());
            
            // Log for debugging
            log.info("🔍 [PRESCRIPTION CREATE] Checking medicine {} for member {} - Active: {}", 
                itemDto.getMedicineId(), memberNameToCheck, checkResult.get("active"));
            
            if (checkResult.get("active") != null && (Boolean) checkResult.get("active")) {
                String status = (String) checkResult.get("status");
                String memberType = (String) checkResult.getOrDefault("memberType", "CLIENT");
                String blockedMemberName = (String) checkResult.getOrDefault("memberName", memberNameToCheck);
                String relation = (String) checkResult.get("relation");
                
                String errorMessage;
                if ("BILLED".equals(status)) {
                    // BILLED - ممنوع صرف الدواء حتى تنتهي المدة (duration)
                    // يجب فحص duration وإظهار المدة المتبقية في الرسالة
                    Long remainingDays = checkResult.get("remainingDays") != null ? 
                        ((Number) checkResult.get("remainingDays")).longValue() : null;
                    Instant allowedDate = (Instant) checkResult.get("allowedDate");
                    Instant expiryDate = (Instant) checkResult.get("expiryDate");
                    Integer duration = checkResult.get("duration") != null ? 
                        ((Number) checkResult.get("duration")).intValue() : null;
                    
                    if (memberType != null && "FAMILY_MEMBER".equals(memberType)) {
                        if (remainingDays != null && duration != null && allowedDate != null) {
                            String allowedDateStr = allowedDate.toString().substring(0, 10); // Format: YYYY-MM-DD
                            errorMessage = String.format(
                                "❌ الدواء محظور! تم صرفه مؤخراً لعضو العائلة %s (%s) ولم تنتهِ مدة العلاج بعد.\n\n" +
                                "📅 المدة المحددة: %d يوم\n" +
                                "⏳ الأيام المتبقية: %d يوم\n" +
                                "📆 يمكن صرفه مرة أخرى بعد: %s\n\n" +
                                "⚠️ القاعدة: ممنوع صرف نفس الدواء إلا بعد انتهاء مدة العلاج (%d يوم).",
                                blockedMemberName, relation, duration, remainingDays, allowedDateStr, duration
                            );
                        } else if (expiryDate != null && remainingDays != null) {
                            String expiryDateStr = expiryDate.toString().substring(0, 10);
                            int calculatedDuration = duration != null ? duration : remainingDays.intValue();
                            errorMessage = String.format(
                                "❌ الدواء محظور! تم صرفه مؤخراً لعضو العائلة %s (%s) ولم تنتهِ صلاحيته بعد.\n\n" +
                                "📅 المدة المحددة: %d يوم\n" +
                                "⏳ الأيام المتبقية: %d يوم\n" +
                                "📆 يمكن صرفه مرة أخرى بعد: %s\n\n" +
                                "⚠️ القاعدة: ممنوع صرف نفس الدواء إلا بعد انتهاء مدة العلاج.",
                                blockedMemberName, relation, calculatedDuration, remainingDays, expiryDateStr
                            );
                        } else {
                            int defaultDuration = duration != null ? duration : 30;
                            errorMessage = String.format(
                                "❌ الدواء محظور! تم صرفه مؤخراً لعضو العائلة %s (%s) ولم تنتهِ مدة العلاج بعد.\n\n" +
                                "📅 المدة المحددة: %d يوم\n\n" +
                                "⚠️ القاعدة: ممنوع صرف نفس الدواء إلا بعد انتهاء مدة العلاج (%d يوم).",
                                blockedMemberName, relation, defaultDuration, defaultDuration
                            );
                        }
                    } else {
                        if (remainingDays != null && duration != null && allowedDate != null) {
                            String allowedDateStr = allowedDate.toString().substring(0, 10);
                            errorMessage = String.format(
                                "❌ الدواء محظور! تم صرفه مؤخراً للمريض ولم تنتهِ مدة العلاج بعد.\n\n" +
                                "📅 المدة المحددة: %d يوم\n" +
                                "⏳ الأيام المتبقية: %d يوم\n" +
                                "📆 يمكن صرفه مرة أخرى بعد: %s\n\n" +
                                "⚠️ القاعدة: ممنوع صرف نفس الدواء إلا بعد انتهاء مدة العلاج (%d يوم).",
                                duration, remainingDays, allowedDateStr, duration
                            );
                        } else if (expiryDate != null && remainingDays != null) {
                            String expiryDateStr = expiryDate.toString().substring(0, 10);
                            int calculatedDuration = duration != null ? duration : remainingDays.intValue();
                            errorMessage = String.format(
                                "❌ الدواء محظور! تم صرفه مؤخراً للمريض ولم تنتهِ صلاحيته بعد.\n\n" +
                                "📅 المدة المحددة: %d يوم\n" +
                                "⏳ الأيام المتبقية: %d يوم\n" +
                                "📆 يمكن صرفه مرة أخرى بعد: %s\n\n" +
                                "⚠️ القاعدة: ممنوع صرف نفس الدواء إلا بعد انتهاء مدة العلاج.",
                                calculatedDuration, remainingDays, expiryDateStr
                            );
                        } else {
                            int defaultDuration = duration != null ? duration : 30;
                            errorMessage = String.format(
                                "❌ الدواء محظور! تم صرفه مؤخراً للمريض ولم تنتهِ مدة العلاج بعد.\n\n" +
                                "📅 المدة المحددة: %d يوم\n\n" +
                                "⚠️ القاعدة: ممنوع صرف نفس الدواء إلا بعد انتهاء مدة العلاج (%d يوم).",
                                defaultDuration, defaultDuration
                            );
                        }
                    }
                } else if ("PENDING".equals(status)) {
                    if (memberType != null && "FAMILY_MEMBER".equals(memberType)) {
                        errorMessage = String.format(
                            "❌ الدواء محظور! يوجد وصفة طبية قيد الانتظار لعضو العائلة %s (%s) تحتوي على نفس الدواء.",
                            blockedMemberName, relation
                        );
                    } else {
                        errorMessage = "❌ الدواء محظور! يوجد وصفة طبية قيد الانتظار تحتوي على نفس الدواء.";
                    }
                } else {
                    // VERIFIED status
                    if (memberType != null && "FAMILY_MEMBER".equals(memberType)) {
                        errorMessage = String.format(
                            "❌ الدواء محظور! يوجد وصفة طبية موافق عليها لعضو العائلة %s (%s) تحتوي على نفس الدواء ولم تنتهِ صلاحيتها بعد.",
                            blockedMemberName, relation
                        );
                    } else {
                        errorMessage = "❌ الدواء محظور! يوجد وصفة طبية موافق عليها تحتوي على نفس الدواء ولم تنتهِ صلاحيتها بعد.";
                    }
                }
                
                throw new IllegalStateException(errorMessage);
            }
        }

        // Add family member info to treatment notes if applicable
        String treatmentNotes = dto.getTreatment();
        if (familyMember != null && !familyMemberInfo.isEmpty()) {
            treatmentNotes = dto.getTreatment() + familyMemberInfo;
        }

        Prescription prescription = Prescription.builder()
                .doctor(doctor)
                .member(member) // Always link to main client
                .status(PrescriptionStatus.PENDING)
                .diagnosis(dto.getDiagnosis())
                .treatment(treatmentNotes)
                .totalPrice(0.0)
                .isChronic(dto.getIsChronic() != null ? dto.getIsChronic() : false) // ✅ حفظ علامة Chronic Disease
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        prescriptionRepo.save(prescription);
        
        // ✅ Force initialization of member fields BEFORE mapping to ensure they're available
        // This must happen within the transaction to avoid LazyInitializationException
        if (prescription.getMember() != null) {
            try {
                Client savedMember = prescription.getMember();
                // Force access to all fields to ensure they're loaded
                UUID memberId = savedMember.getId();
                String memberName = savedMember.getFullName();
                java.time.LocalDate dob = savedMember.getDateOfBirth();
                String gender = savedMember.getGender();
                
                log.info("🔍 [PRESCRIPTION CREATE] After save - Member: {} (ID: {}) | DOB: {} | Gender: {}", 
                    memberName, memberId, dob, gender);
                
                // If data is still null, reload the member
                if (dob == null || gender == null || gender.trim().isEmpty()) {
                    log.warn("⚠️ [PRESCRIPTION CREATE] Member data still null after save, reloading...");
                    Client reloadedMember = clientRepo.findById(memberId).orElse(null);
                    if (reloadedMember != null) {
                        prescription.setMember(reloadedMember);
                        log.info("✅ [PRESCRIPTION CREATE] Reloaded member - DOB: {} | Gender: {}", 
                            reloadedMember.getDateOfBirth(), reloadedMember.getGender());
                    }
                }
            } catch (Exception e) {
                log.error("❌ [PRESCRIPTION CREATE] Error accessing member fields after save: {}", e.getMessage());
            }
        }
        
        log.info("💾 Saved prescription - ID: {}, Member ID: {}, Member Name: {}, Doctor: {}, IsChronic: {}", 
                prescription.getId(), 
                prescription.getMember().getId(), 
                prescription.getMember().getFullName(),
                prescription.getDoctor().getFullName(),
                prescription.getIsChronic());

        List<PrescriptionItem> savedItems = new ArrayList<>();

        for (PrescriptionItemDTO itemDto : dto.getItems()) {
            PriceList med = priceListRepo.findById(itemDto.getMedicineId())
                    .orElseThrow(() -> new NotFoundException("MEDICINE_NOT_FOUND_IN_PRICE_LIST"));

            // Extract drug form and package quantity
            String drugForm = quantityCalculator.extractDrugForm(med);
            Integer packageQuantity = quantityCalculator.extractPackageQuantity(med);

            // Validate prescription parameters
            Integer dosage = itemDto.getDosage();
            Integer timesPerDay = itemDto.getTimesPerDay();
            Integer duration = itemDto.getDuration();

            if (duration == null || duration <= 0) {
                throw new IllegalArgumentException("Duration must be provided and greater than 0");
            }

            // Calculate required quantity based on prescription parameters
            // ✅ إذا كانت الكمية محددة مسبقاً في DTO (من المدير الطبي)، استخدمها مباشرة
            Integer calculatedQuantity = itemDto.getCalculatedQuantity();
            if (calculatedQuantity == null || calculatedQuantity <= 0) {
                // إذا لم تكن محددة، احسبها تلقائياً
                calculatedQuantity = quantityCalculator.calculateRequiredQuantity(
                        dosage, timesPerDay, duration, drugForm, packageQuantity
                );
            }

            // Calculate expiry date from duration
            Instant expiry = Instant.now().plus(duration, ChronoUnit.DAYS);

            // Calculate union price per unit based on drug form (for display/storage only, actual comparison happens in verify())
            // حساب سعر الوحدة من النقابة (للعرض/الحفظ فقط، المقارنة الفعلية تحدث في verify())
            Double unionPricePerUnit;
            String formCreate = drugForm != null ? drugForm.toUpperCase() : "";
            if ("SYRUP".equals(formCreate) || "DROPS".equals(formCreate) || "CREAM".equals(formCreate) || "OINTMENT".equals(formCreate)) {
                // للسائل/الكريم/القطرة: سعر الوحدة = سعر العلبة الواحدة (للعرض فقط)
                // For liquid/cream/drops: unit price = price per package (for display only)
                unionPricePerUnit = med.getPrice();
            } else {
                // للحبوب/الحقن: سعر الوحدة = سعر الحبة/الحقنة الواحدة
                // For tablets/injections: unit price = price per tablet/injection
                unionPricePerUnit = quantityCalculator.calculateUnitPrice(med.getPrice(), packageQuantity);
            }

            PrescriptionItem item = PrescriptionItem.builder()
                    .prescription(prescription)
                    .priceList(med)
                    .dosage(dosage)
                    .timesPerDay(timesPerDay)
                    .duration(duration)
                    .calculatedQuantity(calculatedQuantity)
                    .drugForm(drugForm)
                    .unionPricePerUnit(unionPricePerUnit)
                    .expiryDate(expiry)
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build();

            savedItems.add(prescriptionItemRepo.save(item));
        }

        prescription.setItems(savedItems);
        prescriptionRepo.save(prescription);

        // 🔔 إشعار للصيادلة (جميع الصيادلة)
        String chronicIndicatorForPharmacist = prescription.getIsChronic() != null && prescription.getIsChronic() 
                ? " - ⚠️ CHRONIC DISEASE" : "";
        String pharmacistMessage = familyMember != null
                ? String.format(
                "📋 New prescription from Dr. %s for family member %s (%s) - Client: %s%s",
                doctor.getFullName(),
                familyMember.getFullName(),
                familyMember.getRelation(),
                member.getFullName(),
                chronicIndicatorForPharmacist
        )
                : "📋 New prescription from Dr. " + doctor.getFullName() +
                " for patient " + member.getFullName() + chronicIndicatorForPharmacist;

        clientRepo.findByRoles_Name(RoleName.PHARMACIST)
                .forEach(pharmacist -> notificationService.sendToUser(
                        pharmacist.getId(),
                        pharmacistMessage
                ));

        // 🔔 إشعار للمريض (main client)
        String chronicIndicatorForPatient = prescription.getIsChronic() != null && prescription.getIsChronic() 
                ? " (Chronic Disease)" : "";
        String notificationMessage = familyMember != null
                ? String.format(
                "💊 New prescription created by Dr. %s for family member %s (%s)%s",
                doctor.getFullName(),
                familyMember.getFullName(),
                familyMember.getRelation(),
                chronicIndicatorForPatient
        )
                : "💊 New prescription created for you by Dr. " + doctor.getFullName() + chronicIndicatorForPatient;

        notificationService.sendToUser(
                member.getId(),
                notificationMessage
        );

        // ✅ Force initialization of member fields BEFORE mapping to ensure they're available
        // This must happen within the transaction to avoid LazyInitializationException
        if (prescription.getMember() != null) {
            try {
                Client memberForMapping = prescription.getMember();
                // Force access to all fields to ensure they're loaded before mapping
                UUID memberId = memberForMapping.getId();
                String memberName = memberForMapping.getFullName();
                java.time.LocalDate dob = memberForMapping.getDateOfBirth();
                String gender = memberForMapping.getGender();
                
                log.info("🔍 [PRESCRIPTION CREATE] Before mapping - Member: {} (ID: {}) | DOB: {} | Gender: {}", 
                    memberName, memberId, dob, gender);
                
                // If data is still null, reload the member
                if (dob == null || gender == null || gender.trim().isEmpty()) {
                    log.warn("⚠️ [PRESCRIPTION CREATE] Member data still null before mapping, reloading...");
                    Client reloadedMember = clientRepo.findById(memberId).orElse(null);
                    if (reloadedMember != null) {
                        prescription.setMember(reloadedMember);
                        log.info("✅ [PRESCRIPTION CREATE] Reloaded member before mapping - DOB: {} | Gender: {}", 
                            reloadedMember.getDateOfBirth(), reloadedMember.getGender());
                    }
                }
            } catch (Exception e) {
                log.error("❌ [PRESCRIPTION CREATE] Error accessing member fields before mapping: {}", e.getMessage());
            }
        }
        
        // Now map to DTO - member should be fully loaded
        PrescriptionDTO result = prescriptionMapper.toDto(prescription, familyMemberRepo);
        
        // Log the result to verify
        log.info("📦 [PRESCRIPTION CREATE] DTO result - memberAge: {} | memberGender: {} | memberName: {}", 
            result.getMemberAge(), result.getMemberGender(), result.getMemberName());
        
        return result;
    }

    // Member sees prescriptions
    @Transactional(readOnly = true)
    public List<PrescriptionDTO> getMyPrescriptions() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Client member = clientRepo.findByEmail(auth.getName().toLowerCase())
                .orElseThrow(() -> new NotFoundException("Member not found"));

        log.info("🔍 Fetching prescriptions for member: {} (ID: {})", member.getFullName(), member.getId());
        
        // Use custom query that eagerly fetches member with dateOfBirth and gender
        List<Prescription> prescriptions = prescriptionRepo.findByMemberIdWithMember(member.getId());
        
        log.info("✅ Found {} prescriptions for member: {}", prescriptions.size(), member.getFullName());
        for (Prescription p : prescriptions) {
            log.info("  - Prescription ID: {}, Doctor: {}, Status: {}, IsChronic: {}", 
                    p.getId(), 
                    p.getDoctor() != null ? p.getDoctor().getFullName() : "N/A",
                    p.getStatus(),
                    p.getIsChronic());
        }
        
        return prescriptions.stream()
                .map(p -> prescriptionMapper.toDto(p, familyMemberRepo))
                .collect(Collectors.toList());
    }

    // Pending prescriptions for pharmacists
    @Transactional(readOnly = true)
    public List<PrescriptionDTO> getPending() {
        // Use custom query that eagerly fetches member with dateOfBirth and gender
        List<Prescription> prescriptions = prescriptionRepo.findByStatusWithMember(PrescriptionStatus.PENDING);

        log.info("📋 [SERVICE] Found {} pending prescriptions", prescriptions.size());

        // Force initialization of member fields to ensure they're loaded
        for (Prescription p : prescriptions) {
            if (p.getMember() != null) {
                Client member = p.getMember();
                // Force access to fields to trigger loading
                String name = member.getFullName();
                java.time.LocalDate dob = member.getDateOfBirth();
                String gender = member.getGender();
                log.info("👤 [SERVICE] Prescription {} - Member: {} | DOB: {} | Gender: {}",
                        p.getId(), name, dob, gender);
            } else {
                log.warn("⚠️ [SERVICE] Prescription {} has null member", p.getId());
            }
        }

        List<PrescriptionDTO> dtos = prescriptions.stream()
                .map(p -> prescriptionMapper.toDto(p, familyMemberRepo))
                .collect(Collectors.toList());

        // Verify DTOs have the data
        for (PrescriptionDTO dto : dtos) {
            log.info("📦 [SERVICE] DTO for {} - memberAge: {} | memberGender: {}",
                    dto.getId(), dto.getMemberAge(), dto.getMemberGender());
        }

        return dtos;
    }

    /**
     * Pharmacist verifies items and enters prices
     * 
     * Business Rules:
     * 1. Prevent re-dispensing the same drug before treatment period ends
     * 2. Calculate quantity automatically based on prescription parameters
     * 3. Allow partial dispensing if required quantity < package
     * 4. Allow multiple packages if required quantity > package, but insurance covers only calculated quantity
     * 5. Claim calculation: min(union_price_per_unit, pharmacy_price_per_unit) × covered_quantity
     */
    @Transactional
    public PrescriptionDTO verify(UUID id, List<PrescriptionItemDTO> itemsWithPrices) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Client pharmacist = clientRepo.findByEmail(auth.getName().toLowerCase())
                .orElseThrow(() -> new NotFoundException("PHARMACIST_NOT_FOUND"));

        Prescription prescription = prescriptionRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("PRESCRIPTION_NOT_FOUND"));

        // القيود والتحقق من الوصفات النشطة تكون عند الدكتور فقط
        // الصيدلي فقط يدخل السعر ويتم الحساب في الـ backend
        // Restrictions and active prescription checks are done at doctor side only
        // Pharmacist only enters price and calculations are done in backend

        double total = 0.0;

        for (PrescriptionItemDTO dto : itemsWithPrices) {
            PrescriptionItem item = prescriptionItemRepo.findById(dto.getId())
                    .orElseThrow(() -> new NotFoundException("ITEM_NOT_FOUND"));

            PriceList med = item.getPriceList();
            String drugForm = item.getDrugForm();

            // الصيدلي يدخل السعر فقط للكمية المحددة (20 حبة مثلاً أو علبة كاملة)
            // Pharmacist enters price for the specified quantity (20 pills for example or full package)
            Double pharmacistPrice = dto.getPharmacistPrice();
            if (pharmacistPrice == null || pharmacistPrice <= 0) {
                throw new IllegalArgumentException("Pharmacist price must be provided and greater than 0");
            }

            // service price = سعر النقابة للعلبة الكاملة (med.getPrice())
            // Service price = Union price for full package
            Double servicePrice = med.getPrice();
            if (servicePrice == null || servicePrice <= 0) {
                throw new IllegalArgumentException("Service price (union price) must be valid and greater than 0");
            }

            // Update item with all calculated values
            Integer calculatedQuantity = item.getCalculatedQuantity(); // الكمية المحسوبة (20 حبة مثلاً أو 1 علبة)
            
            if (calculatedQuantity == null || calculatedQuantity <= 0) {
                throw new IllegalArgumentException("Calculated quantity must be set before verification");
            }
            
            // حساب سعر النقابة للكمية المحسوبة نفسها
            // Calculate union price for the calculated quantity
            Double unionPriceForCalculatedQuantity;
            String form = drugForm != null ? drugForm.toUpperCase() : "";
            
            log.info("🔍 [VERIFY] Medicine: {}, Form: {}, Calculated Qty: {}, Service Price (package): {}", 
                    med.getServiceName(), form, calculatedQuantity, servicePrice);
            
            if ("SYRUP".equals(form) || "DROPS".equals(form) || "CREAM".equals(form) || "OINTMENT".equals(form)) {
                // للسائل/الكريم/القطرة: الكمية = عدد العبوات، إذن سعر النقابة = servicePrice × عدد العبوات
                // For liquid/cream/drops: quantity = number of packages, so union price = servicePrice × number of packages
                // calculatedQuantity = عدد العبوات (مثلاً 2 علب)
                // calculatedQuantity = number of packages (e.g., 2 bottles)
                unionPriceForCalculatedQuantity = servicePrice * calculatedQuantity;
                log.info("💧 [VERIFY] Liquid/Cream/Drops detected - Number of packages: {}, Service Price per package: {}, Union price total: {} = {} × {}", 
                        calculatedQuantity, servicePrice, unionPriceForCalculatedQuantity, servicePrice, calculatedQuantity);
            } else {
                // للحبوب/الحقن: نحسب سعر النقابة للكمية المحسوبة
                // For tablets/injections: calculate union price for calculated quantity
                Integer packageQuantity = quantityCalculator.extractPackageQuantity(med);
                log.info("💊 [VERIFY] Tablet/Injection - Package Qty: {}", packageQuantity);
                
                if (packageQuantity != null && packageQuantity > 0 && calculatedQuantity > 0) {
                    // سعر الوحدة الواحدة = servicePrice / packageQuantity
                    // Price per unit = servicePrice / packageQuantity
                    Double unitPrice = servicePrice / packageQuantity;
                    // سعر الكمية المحسوبة = unitPrice × calculatedQuantity
                    // Price for calculated quantity = unitPrice × calculatedQuantity
                    unionPriceForCalculatedQuantity = unitPrice * calculatedQuantity;
                    log.info("💊 [VERIFY] Unit Price: {} = {} / {}, Union Price for {} units: {} = {} × {}", 
                            unitPrice, servicePrice, packageQuantity, calculatedQuantity, unionPriceForCalculatedQuantity, unitPrice, calculatedQuantity);
                } else {
                    // إذا لم توجد كمية، نستخدم servicePrice مباشرة
                    // If no quantity, use servicePrice directly
                    unionPriceForCalculatedQuantity = servicePrice;
                    log.warn("⚠️ [VERIFY] No package quantity found, using servicePrice directly: {}", servicePrice);
                }
            }

            // المقارنة: نقارن السعر الذي أدخله الصيدلي (للكمية المحسوبة) مع سعر النقابة (للكمية المحسوبة) ونأخذ الأقل
            // Comparison: Compare pharmacist price (for calculated quantity) with union price (for calculated quantity) and take the minimum
            // المبلغ النهائي = أقل سعر بين سعر الصيدلي وسعر النقابة للكمية المحسوبة نفسها
            // Final amount = minimum of pharmacist price and union price for the same calculated quantity
            
            // التحقق من القيم قبل المقارنة
            // Verify values before comparison
            if (unionPriceForCalculatedQuantity == null || unionPriceForCalculatedQuantity <= 0) {
                log.error("❌ [VERIFY] ERROR: unionPriceForCalculatedQuantity is invalid: {}", unionPriceForCalculatedQuantity);
                throw new IllegalStateException("Union price for calculated quantity is invalid");
            }
            
            Double finalPrice = Math.min(pharmacistPrice, unionPriceForCalculatedQuantity);
            
            log.info("💰 [VERIFY] Price comparison - Pharmacist Price: {}, Union Price (for qty): {}, Final Price (min): {}", 
                    pharmacistPrice, unionPriceForCalculatedQuantity, finalPrice);
            
            // التحقق من أن finalPrice هو فعلاً الأقل
            // Verify that finalPrice is indeed the minimum
            if (finalPrice > unionPriceForCalculatedQuantity) {
                log.error("❌ [VERIFY] ERROR: finalPrice ({}) is greater than unionPriceForCalculatedQuantity ({})! This should never happen!", 
                        finalPrice, unionPriceForCalculatedQuantity);
                throw new IllegalStateException("Final price calculation error: finalPrice > unionPriceForCalculatedQuantity");
            }
            
            if (finalPrice > pharmacistPrice) {
                log.error("❌ [VERIFY] ERROR: finalPrice ({}) is greater than pharmacistPrice ({})! This should never happen!", 
                        finalPrice, pharmacistPrice);
                throw new IllegalStateException("Final price calculation error: finalPrice > pharmacistPrice");
            }

            item.setDispensedQuantity(calculatedQuantity);
            item.setCoveredQuantity(calculatedQuantity);
            item.setPharmacistPrice(pharmacistPrice);
            
            // حفظ سعر الوحدة الواحدة من النقابة (للحفظ فقط)
            // Save union price per unit (for storage only)
            Integer packageQuantity = quantityCalculator.extractPackageQuantity(med);
            Double unionPricePerUnit = quantityCalculator.calculateUnitPrice(servicePrice, packageQuantity);
            item.setUnionPricePerUnit(unionPricePerUnit);
            
            // حساب سعر الوحدة من الصيدلي (للحفظ فقط، لا يعرض للصيدلي)
            // Calculate pharmacist price per unit (for storage only, not shown to pharmacist)
            Double pharmacistPricePerUnit = calculatedQuantity != null && calculatedQuantity > 0 
                ? pharmacistPrice / calculatedQuantity 
                : 0.0;
            item.setPharmacistPricePerUnit(pharmacistPricePerUnit);
            
            item.setFinalPrice(finalPrice);
            item.setUpdatedAt(Instant.now());
            
            // التحقق من أن finalPrice تم حفظه بشكل صحيح
            // Verify that finalPrice is saved correctly
            PrescriptionItem savedItem = prescriptionItemRepo.save(item);
            
            // التحقق من القيم المحفوظة
            // Verify saved values
            log.info("🔍 [VERIFY] Saved values - FinalPrice: {}, PharmacistPrice: {}, UnionPriceForQty: {}", 
                    savedItem.getFinalPrice(), savedItem.getPharmacistPrice(), unionPriceForCalculatedQuantity);
            
            if (savedItem.getFinalPrice() == null) {
                log.error("❌ [VERIFY] ERROR: FinalPrice is NULL after save!");
                throw new IllegalStateException("FinalPrice is NULL after save");
            }
            
            // استخدام Double.compare للتأكد من المقارنة الصحيحة (لتجنب مشاكل floating point)
            // Use Double.compare for accurate comparison (to avoid floating point issues)
            if (Double.compare(savedItem.getFinalPrice(), finalPrice) != 0) {
                log.error("❌ [VERIFY] ERROR: FinalPrice was not saved correctly! Expected: {}, Actual: {}", 
                        finalPrice, savedItem.getFinalPrice());
                throw new IllegalStateException("Failed to save finalPrice correctly. Expected: " + finalPrice + ", Actual: " + savedItem.getFinalPrice());
            }
            
            // التحقق النهائي: finalPrice يجب أن يكون الأقل
            // Final verification: finalPrice must be the minimum
            if (Double.compare(savedItem.getFinalPrice(), unionPriceForCalculatedQuantity) > 0) {
                log.error("❌ [VERIFY] CRITICAL ERROR: Saved finalPrice ({}) is greater than unionPriceForCalculatedQuantity ({})!", 
                        savedItem.getFinalPrice(), unionPriceForCalculatedQuantity);
                throw new IllegalStateException("CRITICAL: Saved finalPrice is greater than union price!");
            }
            
            log.info("✅ [VERIFY] FinalPrice saved successfully: {} (should be min of {} and {})", 
                    savedItem.getFinalPrice(), pharmacistPrice, unionPriceForCalculatedQuantity);

            total += finalPrice;
        }

        prescription.setPharmacist(pharmacist);
        prescription.setStatus(PrescriptionStatus.VERIFIED);
        prescription.setTotalPrice(total);
        prescription.setUpdatedAt(Instant.now());
        prescriptionRepo.save(prescription);

        // 🔔 إشعار للمريض
        notificationService.sendToUser(
                prescription.getMember().getId(),
                "✅ تمت الموافقة على وصفتك من الصيدلي " + pharmacist.getFullName() +
                        " - المجموع: " + total + " ₪"
        );

        // 🔔 إشعار للطبيب
        notificationService.sendToUser(
                prescription.getDoctor().getId(),
                "✅ تمت الموافقة على وصفتك للمريض " + prescription.getMember().getFullName() +
                        " من الصيدلي " + pharmacist.getFullName()
        );

        return prescriptionMapper.toDto(prescription, familyMemberRepo);
    }

    // Pharmacist reject
    @Transactional
    public PrescriptionDTO reject(UUID id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Client pharmacist = clientRepo.findByEmail(auth.getName().toLowerCase())
                .orElseThrow(() -> new NotFoundException("PHARMACIST_NOT_FOUND"));

        Prescription prescription = prescriptionRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("PRESCRIPTION_NOT_FOUND"));

        prescription.setStatus(PrescriptionStatus.REJECTED);
        prescription.setPharmacist(pharmacist);
        prescription.setUpdatedAt(Instant.now());
        prescriptionRepo.save(prescription);

        // 🔔 إشعار للمريض
        notificationService.sendToUser(
                prescription.getMember().getId(),
                "❌ تم رفض وصفتك من الصيدلي " + pharmacist.getFullName()
        );

        // 🔔 إشعار للطبيب
        notificationService.sendToUser(
                prescription.getDoctor().getId(),
                "❌ تم رفض وصفتك للمريض " + prescription.getMember().getFullName() +
                        " من الصيدلي " + pharmacist.getFullName()
        );

        return prescriptionMapper.toDto(prescription, familyMemberRepo);
    }

    // Doctor updates prescription
    @Transactional
    public PrescriptionDTO update(UUID id, PrescriptionDTO dto) {
        Prescription prescription = prescriptionRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("PRESCRIPTION_NOT_FOUND"));

        if (prescription.getStatus() != PrescriptionStatus.PENDING) {
            throw new IllegalStateException("CANNOT_UPDATE_NON_PENDING");
        }

        prescription.setDiagnosis(dto.getDiagnosis());
        prescription.setTreatment(dto.getTreatment());
        prescription.setUpdatedAt(Instant.now());

        prescriptionItemRepo.deleteAll(prescription.getItems());
        prescription.getItems().clear();

        List<PrescriptionItem> newItems = new ArrayList<>();

        for (PrescriptionItemDTO itemDto : dto.getItems()) {
            PriceList med = priceListRepo.findById(itemDto.getMedicineId())
                    .orElseThrow(() -> new NotFoundException("MEDICINE_NOT_FOUND_IN_PRICE_LIST"));

            // Extract drug form and package quantity
            String drugForm = quantityCalculator.extractDrugForm(med);
            Integer packageQuantity = quantityCalculator.extractPackageQuantity(med);

            // Validate prescription parameters
            Integer dosage = itemDto.getDosage();
            Integer timesPerDay = itemDto.getTimesPerDay();
            Integer duration = itemDto.getDuration();

            if (duration == null || duration <= 0) {
                throw new IllegalArgumentException("Duration must be provided and greater than 0");
            }

            // Calculate required quantity based on prescription parameters
            // ✅ إذا كانت الكمية محددة مسبقاً في DTO (من المدير الطبي)، استخدمها مباشرة
            Integer calculatedQuantity = itemDto.getCalculatedQuantity();
            if (calculatedQuantity == null || calculatedQuantity <= 0) {
                // إذا لم تكن محددة، احسبها تلقائياً
                calculatedQuantity = quantityCalculator.calculateRequiredQuantity(
                        dosage, timesPerDay, duration, drugForm, packageQuantity
                );
            }

            // Calculate expiry date from duration
            Instant expiry = Instant.now().plus(duration, ChronoUnit.DAYS);

            // Calculate union price per unit based on drug form (for display/storage only, actual comparison happens in verify())
            // حساب سعر الوحدة من النقابة (للعرض/الحفظ فقط، المقارنة الفعلية تحدث في verify())
            Double unionPricePerUnit;
            String formUpdate = drugForm != null ? drugForm.toUpperCase() : "";
            if ("SYRUP".equals(formUpdate) || "DROPS".equals(formUpdate) || "CREAM".equals(formUpdate) || "OINTMENT".equals(formUpdate)) {
                // للسائل/الكريم/القطرة: سعر الوحدة = سعر العلبة الواحدة (للعرض فقط)
                // For liquid/cream/drops: unit price = price per package (for display only)
                unionPricePerUnit = med.getPrice();
            } else {
                // للحبوب/الحقن: سعر الوحدة = سعر الحبة/الحقنة الواحدة
                // For tablets/injections: unit price = price per tablet/injection
                unionPricePerUnit = quantityCalculator.calculateUnitPrice(med.getPrice(), packageQuantity);
            }

            PrescriptionItem item = PrescriptionItem.builder()
                    .prescription(prescription)
                    .priceList(med)
                    .dosage(dosage)
                    .timesPerDay(timesPerDay)
                    .duration(duration)
                    .calculatedQuantity(calculatedQuantity)
                    .drugForm(drugForm)
                    .unionPricePerUnit(unionPricePerUnit)
                    .expiryDate(expiry)
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build();

            newItems.add(prescriptionItemRepo.save(item));
        }

        prescription.setItems(newItems);
        prescriptionRepo.save(prescription);

        // 🔔 إشعار للمريض
        notificationService.sendToUser(
                prescription.getMember().getId(),
                "✏️ تم تحديث وصفتك الطبية من الدكتور " + prescription.getDoctor().getFullName()
        );

        // 🔔 إشعار للصيادلة (في حالة وجود صيدلي مرتبط)
        if (prescription.getPharmacist() != null) {
            notificationService.sendToUser(
                    prescription.getPharmacist().getId(),
                    "✏️ تم تحديث الوصفة الطبية من الدكتور " + prescription.getDoctor().getFullName() +
                            " للمريض " + prescription.getMember().getFullName()
            );
        }

        return prescriptionMapper.toDto(prescription, familyMemberRepo);
    }

    // Doctor deletes prescription
    @Transactional
    public void delete(UUID id) {
        Prescription prescription = prescriptionRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("PRESCRIPTION_NOT_FOUND"));

        if (prescription.getStatus() != PrescriptionStatus.PENDING) {
            throw new IllegalStateException("CANNOT_DELETE_NON_PENDING");
        }

        // 🔔 إشعار للمريض
        notificationService.sendToUser(
                prescription.getMember().getId(),
                "🗑️ تم حذف وصفتك الطبية من الدكتور " + prescription.getDoctor().getFullName()
        );

        prescriptionRepo.delete(prescription);
    }

    // Doctor stats
    public PrescriptionDTO getDoctorStats() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Client doctor = clientRepo.findByEmail(auth.getName().toLowerCase())
                .orElseThrow(() -> new NotFoundException("DOCTOR_NOT_FOUND"));

        return PrescriptionDTO.builder()
                .total(prescriptionRepo.countByDoctorId(doctor.getId()))
                .pending(prescriptionRepo.countByDoctorIdAndStatus(doctor.getId(), PrescriptionStatus.PENDING))
                .verified(prescriptionRepo.countByDoctorIdAndStatus(doctor.getId(), PrescriptionStatus.VERIFIED))
                .rejected(prescriptionRepo.countByDoctorIdAndStatus(doctor.getId(), PrescriptionStatus.REJECTED))
                .build();
    }

    // Pharmacist stats
    public PrescriptionDTO getPharmacistStats() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Client pharmacist = clientRepo.findByEmail(auth.getName().toLowerCase())
                .orElseThrow(() -> new NotFoundException("PHARMACIST_NOT_FOUND"));

        return PrescriptionDTO.builder()
                .pending(prescriptionRepo.countByStatus(PrescriptionStatus.PENDING))
                .verified(prescriptionRepo.countByPharmacistIdAndStatus(pharmacist.getId(), PrescriptionStatus.VERIFIED))
                .rejected(prescriptionRepo.countByPharmacistIdAndStatus(pharmacist.getId(), PrescriptionStatus.REJECTED))
                .total(
                        prescriptionRepo.countByPharmacistIdAndStatus(pharmacist.getId(), PrescriptionStatus.PENDING)
                                + prescriptionRepo.countByPharmacistIdAndStatus(pharmacist.getId(), PrescriptionStatus.VERIFIED)
                                + prescriptionRepo.countByPharmacistIdAndStatus(pharmacist.getId(), PrescriptionStatus.REJECTED)
                )
                .build();
    }

    // Pharmacist update profile
    @Transactional
    public ClientDto updatePharmacistProfile(String username, UpdateUserDTO dto, MultipartFile[] universityCard){
        Client pharmacist = clientRepo.findByEmail(username.toLowerCase())
                .orElseThrow(() -> new NotFoundException("PHARMACIST_NOT_FOUND"));

        if (dto.getFullName() != null) pharmacist.setFullName(dto.getFullName());
        if (dto.getEmail() != null) pharmacist.setEmail(dto.getEmail());
        if (dto.getPhone() != null) pharmacist.setPhone(dto.getPhone());

        if (universityCard != null && universityCard.length > 0) {
            try {
                String uploadDir = "uploads/pharmacists";
                Files.createDirectories(Paths.get(uploadDir));

                if (pharmacist.getUniversityCardImages() == null) {
                    pharmacist.setUniversityCardImages(new ArrayList<>());
                }

                for (MultipartFile file : universityCard) {
                    if (file == null || file.isEmpty()) continue;

                    String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
                    Path filePath = Paths.get(uploadDir, fileName);

                    Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                    pharmacist.getUniversityCardImages()
                            .add("/" + uploadDir + "/" + fileName);
                }

            } catch (IOException e) {
                throw new RuntimeException("IMAGE_UPLOAD_FAILED", e);
            }
        }

        pharmacist.setUpdatedAt(Instant.now());
        Client saved = clientRepo.save(pharmacist);
        return clientMapper.toDTO(saved);
    }

    // Doctor sees his prescriptions
    @Transactional(readOnly = true)
    public List<PrescriptionDTO> getByDoctor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Client doctor = clientRepo.findByEmail(auth.getName().toLowerCase())
                .orElseThrow(() -> new NotFoundException("DOCTOR_NOT_FOUND"));

        // Use custom query that eagerly fetches member with dateOfBirth and gender
        return prescriptionRepo.findByDoctorIdWithMember(doctor.getId())
                .stream()
                .map(p -> prescriptionMapper.toDto(p, familyMemberRepo))
                .collect(Collectors.toList());
    }

    // Pharmacist sees all his prescriptions
    @Transactional(readOnly = true)
    public List<PrescriptionDTO> getAllForCurrentPharmacist() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Client pharmacist = clientRepo.findByEmail(auth.getName().toLowerCase())
                .orElseThrow(() -> new NotFoundException("PHARMACIST_NOT_FOUND"));

        // Use custom query that eagerly fetches member with dateOfBirth and gender
        List<Prescription> prescriptions = prescriptionRepo.findByPharmacistIdWithMember(pharmacist.getId());
        
        log.info("📋 [SERVICE] Found {} prescriptions for pharmacist {}", prescriptions.size(), pharmacist.getFullName());
        
        // Force initialization of member fields to ensure they're loaded BEFORE mapping
        // This must happen within the transaction to avoid LazyInitializationException
        // Use Hibernate.initialize() to force loading of lazy fields
        for (Prescription p : prescriptions) {
            if (p.getMember() != null) {
                try {
                    Client member = p.getMember();
                    UUID memberId = member.getId();
                    
                    // Force Hibernate to initialize the member entity and all its fields
                    // This ensures dateOfBirth and gender are loaded before mapping
                    org.hibernate.Hibernate.initialize(member);
                    
                    // Now access the fields to ensure they're loaded
                    String memberName = member.getFullName();
                    java.time.LocalDate dob = member.getDateOfBirth();
                    String gender = member.getGender();
                    
                    log.debug("✅ [SERVICE] Prescription {} - Member: {} (ID: {}) | DOB: {} | Gender: {}", 
                        p.getId(), memberName, memberId, dob, gender);
                    
                    // If data is still missing after initialization, reload from database
                    if (dob == null || gender == null || gender.trim().isEmpty()) {
                        log.warn("⚠️ [SERVICE] Prescription {} - Member {} missing DOB/Gender after initialize, reloading from DB", 
                            p.getId(), memberName);
                        
                        // Reload member from database to ensure all fields are loaded
                        Client reloadedMember = clientRepo.findById(memberId).orElse(null);
                        if (reloadedMember != null) {
                            p.setMember(reloadedMember);
                            log.info("✅ [SERVICE] Reloaded member {} - DOB: {} | Gender: {}", 
                                reloadedMember.getFullName(), reloadedMember.getDateOfBirth(), reloadedMember.getGender());
                        } else {
                            log.error("❌ [SERVICE] Could not reload member {} from database", memberId);
                        }
                    }
                } catch (org.hibernate.LazyInitializationException e) {
                    log.error("❌ [SERVICE] LazyInitializationException for prescription {}: {}", 
                        p.getId(), e.getMessage());
                    // Try to reload the member from database
                    try {
                        UUID memberId = p.getMember().getId();
                        Client reloadedMember = clientRepo.findById(memberId).orElse(null);
                        if (reloadedMember != null) {
                            p.setMember(reloadedMember);
                            log.info("✅ [SERVICE] Reloaded member for prescription {} after LazyInitializationException", p.getId());
                        }
                    } catch (Exception reloadErr) {
                        log.error("❌ [SERVICE] Failed to reload member: {}", reloadErr.getMessage());
                    }
                } catch (Exception e) {
                    log.error("❌ [SERVICE] Error accessing member fields for prescription {}: {}", 
                        p.getId(), e.getMessage());
                }
            } else {
                log.warn("⚠️ [SERVICE] Prescription {} has null member", p.getId());
            }
        }
        
        // Map to DTOs - this should now have all data loaded
        // The mapper will extract age and gender from the loaded member entity
        // IMPORTANT: Mapping must happen within the transaction to avoid LazyInitializationException
        List<PrescriptionDTO> dtos = new java.util.ArrayList<>();
        for (Prescription p : prescriptions) {
            // Before mapping, ensure member is fully initialized
            if (p.getMember() != null) {
                try {
                    // Force initialization one more time right before mapping
                    org.hibernate.Hibernate.initialize(p.getMember());
                    
                    // Access fields to ensure they're loaded
                    Client member = p.getMember();
                    java.time.LocalDate dob = member.getDateOfBirth();
                    String gender = member.getGender();
                    
                    log.debug("🔍 [SERVICE] Before mapping prescription {} - Member: {} | DOB: {} | Gender: {}", 
                        p.getId(), member.getFullName(), dob, gender);
                } catch (Exception e) {
                    log.error("❌ [SERVICE] Error initializing member before mapping prescription {}: {}", 
                        p.getId(), e.getMessage());
                }
            }
            
            // Map to DTO - this happens within the transaction
            PrescriptionDTO dto = prescriptionMapper.toDto(p, familyMemberRepo);
            dtos.add(dto);
            
            // Log the result immediately after mapping
            log.info("📦 [SERVICE] DTO for {} - memberAge: {} | memberGender: {}", 
                p.getId(), dto.getMemberAge(), dto.getMemberGender());
        }
        
        // Verify DTOs have the data and log any missing
        int missingCount = 0;
        for (PrescriptionDTO dto : dtos) {
            if (dto.getMemberAge() == null || dto.getMemberGender() == null) {
                missingCount++;
                log.warn("⚠️ [SERVICE] Prescription {} missing age/gender - Age: {} | Gender: {} | Member: {}", 
                    dto.getId(), dto.getMemberAge(), dto.getMemberGender(), dto.getMemberName());
            }
        }
        
        if (missingCount > 0) {
            log.warn("⚠️ [SERVICE] {} out of {} prescriptions missing age/gender data", missingCount, dtos.size());
        } else {
            log.info("✅ [SERVICE] All {} prescriptions have age/gender data", dtos.size());
        }
        
        return dtos;
    }

    // Get all pharmacists
    public List<ClientDto> getAllPharmacists() {
        return clientRepo.findByRoles_Name(RoleName.PHARMACIST)
                .stream()
                .map(clientMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Check if patient has an active prescription for a specific medicine
     * 
     * Business Rules:
     * - Prevent re-dispensing the same drug before the prescribed treatment period ends
     * - Allow re-dispensing only with a new or updated prescription
     * 
     * @param memberName - Patient name
     * @param medicineId - Medicine ID to check
     * @return Map with active status, prescription status, and expiry date if applicable
     */
    public Map<String, Object> checkActivePrescription(String memberName, UUID medicineId) {
        log.info("🔍 [CHECK ACTIVE] Checking medicine {} for member {}", medicineId, memberName);
        Map<String, Object> response = new HashMap<>();
        response.put("active", false);

        // First, try to find as a FamilyMember
        FamilyMember familyMember = familyMemberRepo.findByFullName(memberName).orElse(null);
        Client mainClient = null;
        
        if (familyMember != null) {
            // This is a family member - get the main client
            mainClient = familyMember.getClient();
            log.info("✅ [CHECK ACTIVE] Found as FamilyMember: {} (ID: {}), Main Client: {} (ID: {})", 
                familyMember.getFullName(), familyMember.getId(), 
                mainClient.getFullName(), mainClient.getId());
        } else {
            // Try to find as a Client (main client)
            Client member = clientRepo.findByFullName(memberName).orElse(null);
            if (member != null) {
                mainClient = member;
                log.info("✅ [CHECK ACTIVE] Found as Main Client: {} (ID: {})", mainClient.getFullName(), mainClient.getId());
            } else {
                log.warn("⚠️ [CHECK ACTIVE] Member not found: {}", memberName);
                return response;
            }
        }

        Instant now = Instant.now();

        // Get all prescriptions for the main client (all prescriptions are linked to main client)
        List<Prescription> clientPrescriptions = prescriptionRepo.findByMemberId(mainClient.getId());
        log.info("📋 [CHECK ACTIVE] Found {} prescriptions for main client {}", clientPrescriptions.size(), mainClient.getFullName());
        
        for (Prescription p : clientPrescriptions) {
            log.info("📋 [CHECK ACTIVE] Checking prescription {} with status {}", p.getId(), p.getStatus());
            
            // Check if this prescription is for a family member
            String treatment = p.getTreatment();
            boolean isFamilyMemberPrescription = treatment != null && treatment.contains("Family Member:");
            
            // Only check CLIENT prescriptions if memberName matches the main client
            // Skip if this is a family member prescription (we'll check those separately)
            if (isFamilyMemberPrescription) {
                log.info("⏭️ [CHECK ACTIVE] Skipping family member prescription - will check separately");
                continue;
            }
            
            // This is a CLIENT prescription - check it only if we're checking for the main client (not a family member)
            if (familyMember != null) {
                // We're checking for a family member, so skip main client prescriptions
                log.info("⏭️ [CHECK ACTIVE] Skipping - checking for family member, not main client");
                continue;
            }
            
            // Verify memberName matches the main client
            if (!mainClient.getFullName().equals(memberName)) {
                log.info("⏭️ [CHECK ACTIVE] Skipping - memberName '{}' does not match main client '{}'", memberName, mainClient.getFullName());
                continue;
            }
            
            for (PrescriptionItem item : p.getItems()) {
                if (!item.getPriceList().getId().equals(medicineId)) continue;

                log.info("✅ [CHECK ACTIVE] Found matching medicine in prescription {} with status {}", p.getId(), p.getStatus());

                // Check PENDING status - ممنوع تماماً
                if (p.getStatus() == PrescriptionStatus.PENDING) {
                    log.info("🚫 [CHECK ACTIVE] Medicine is PENDING - blocking");
                    response.put("active", true);
                    response.put("status", "PENDING");
                    response.put("memberType", "CLIENT");
                    response.put("memberName", mainClient.getFullName());
                    log.info("📤 [CHECK ACTIVE] Returning response: {}", response);
                    return response;
                }

                // Check VERIFIED status (not yet dispensed)
                if (p.getStatus() == PrescriptionStatus.VERIFIED &&
                        item.getExpiryDate() != null &&
                        item.getExpiryDate().isAfter(now)) {
                    response.put("active", true);
                    response.put("status", "VERIFIED");
                    response.put("expiryDate", item.getExpiryDate());
                    response.put("memberType", "CLIENT");
                    response.put("memberName", mainClient.getFullName());
                    return response;
                }

                // Check BILLED status (dispensed) - based on duration
                // If medicine is BILLED, check duration and block if duration hasn't passed
                if (p.getStatus() == PrescriptionStatus.BILLED) {
                    log.info("💰 [CHECK ACTIVE] Medicine is BILLED - checking duration");
                    Integer duration = item.getDuration();
                    Instant billDate = p.getUpdatedAt();
                    
                    log.info("📅 [CHECK ACTIVE] Duration: {}, BillDate: {}", duration, billDate);
                    
                    if (duration != null && duration > 0) {
                        // Calculate when the medicine can be dispensed again
                        Instant allowedDate = billDate.plus(duration, ChronoUnit.DAYS);
                        
                        log.info("📅 [CHECK ACTIVE] AllowedDate: {}, Now: {}, IsAfter: {}", allowedDate, now, allowedDate.isAfter(now));
                        
                        if (allowedDate.isAfter(now)) {
                            // Still within the duration period - medicine is blocked
                            long remainingDays = ChronoUnit.DAYS.between(now, allowedDate);
                            log.info("🚫 [CHECK ACTIVE] Medicine is blocked - {} days remaining", remainingDays);
                            response.put("active", true);
                            response.put("status", "BILLED");
                            response.put("memberType", "CLIENT");
                            response.put("memberName", mainClient.getFullName());
                            response.put("billDate", billDate);
                            response.put("allowedDate", allowedDate);
                            response.put("duration", duration);
                            response.put("remainingDays", remainingDays);
                            log.info("📤 [CHECK ACTIVE] Returning BILLED response: {}", response);
                            return response;
                        }
                        // If duration has passed, allow (don't block)
                        log.info("✅ [CHECK ACTIVE] Duration has passed - allowing");
                    } else {
                        // If no duration specified, use expiryDate
                        if (item.getExpiryDate() != null && item.getExpiryDate().isAfter(now)) {
                            long remainingDays = ChronoUnit.DAYS.between(now, item.getExpiryDate());
                            response.put("active", true);
                            response.put("status", "BILLED");
                            response.put("memberType", "CLIENT");
                            response.put("memberName", mainClient.getFullName());
                            response.put("expiryDate", item.getExpiryDate());
                            response.put("remainingDays", remainingDays);
                            // Calculate duration from expiryDate for display
                            long daysFromBillToExpiry = ChronoUnit.DAYS.between(billDate, item.getExpiryDate());
                            response.put("duration", (int) daysFromBillToExpiry);
                            return response;
                        } else if (item.getExpiryDate() == null) {
                            // If no expiryDate and no duration, block for at least 30 days as safety measure
                            Instant allowedDate = billDate.plus(30, ChronoUnit.DAYS);
                            if (allowedDate.isAfter(now)) {
                                long remainingDays = ChronoUnit.DAYS.between(now, allowedDate);
                                response.put("active", true);
                                response.put("status", "BILLED");
                                response.put("memberType", "CLIENT");
                                response.put("memberName", mainClient.getFullName());
                                response.put("billDate", billDate);
                                response.put("allowedDate", allowedDate);
                                response.put("duration", 30);
                                response.put("remainingDays", remainingDays);
                                return response;
                            }
                            // If 30 days passed, allow (don't block)
                        }
                    }
                }
            }
        }

        // 2. Check prescriptions for family members of this client
        // Only check if memberName matches a family member name
        // All prescriptions (including family member ones) are linked to the main client
        // We check all prescriptions and identify family member ones by parsing treatment notes
        for (Prescription p : clientPrescriptions) {
            String treatment = p.getTreatment();
            boolean isFamilyMemberPrescription = treatment != null && treatment.contains("Family Member:");
            
            // Skip if this is not a family member prescription
            if (!isFamilyMemberPrescription) {
                continue;
            }
            
            // Extract family member name from treatment if it's a family member prescription
            String familyMemberName = null;
            String relation = null;
            // Parse family member info from treatment notes
            // Format: "Family Member: Name (Relation) - Insurance: ..."
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
                "Family Member:\\s*([^-]+?)\\s*\\(([^)]+)\\)"
            );
            java.util.regex.Matcher matcher = pattern.matcher(treatment);
            if (matcher.find()) {
                familyMemberName = matcher.group(1).trim();
                relation = matcher.group(2).trim();
            }
            
            // Only check if memberName matches this family member
            if (familyMemberName == null || !familyMemberName.equals(memberName)) {
                log.info("⏭️ [CHECK ACTIVE] Skipping - memberName '{}' does not match family member '{}'", memberName, familyMemberName);
                continue;
            }
            
            for (PrescriptionItem item : p.getItems()) {
                if (!item.getPriceList().getId().equals(medicineId)) continue;

                // This is a FAMILY_MEMBER prescription for the specified member - check it
                if (familyMemberName != null) {
                    // Check PENDING status - ممنوع تماماً
                    if (p.getStatus() == PrescriptionStatus.PENDING) {
                        response.put("active", true);
                        response.put("status", "PENDING");
                        response.put("memberType", "FAMILY_MEMBER");
                        response.put("memberName", familyMemberName);
                        response.put("relation", relation);
                        return response;
                    }

                    // Check VERIFIED status
                    if (p.getStatus() == PrescriptionStatus.VERIFIED &&
                            item.getExpiryDate() != null &&
                            item.getExpiryDate().isAfter(now)) {
                        response.put("active", true);
                        response.put("status", "VERIFIED");
                        response.put("expiryDate", item.getExpiryDate());
                        response.put("memberType", "FAMILY_MEMBER");
                        response.put("memberName", familyMemberName);
                        response.put("relation", relation);
                        return response;
                    }

                    // Check BILLED status - based on duration
                    // If medicine is BILLED for family member, check duration and block if duration hasn't passed
                    if (p.getStatus() == PrescriptionStatus.BILLED) {
                        Integer duration = item.getDuration();
                        Instant billDate = p.getUpdatedAt();
                        
                        if (duration != null && duration > 0) {
                            Instant allowedDate = billDate.plus(duration, ChronoUnit.DAYS);
                            
                            if (allowedDate.isAfter(now)) {
                                long remainingDays = ChronoUnit.DAYS.between(now, allowedDate);
                                response.put("active", true);
                                response.put("status", "BILLED");
                                response.put("memberType", "FAMILY_MEMBER");
                                response.put("memberName", familyMemberName);
                                response.put("relation", relation);
                                response.put("billDate", billDate);
                                response.put("allowedDate", allowedDate);
                                response.put("duration", duration);
                                response.put("remainingDays", remainingDays);
                                return response;
                            }
                            // If duration has passed, allow (don't block)
                        } else {
                            // If no duration specified, use expiryDate
                            if (item.getExpiryDate() != null && item.getExpiryDate().isAfter(now)) {
                                long remainingDays = ChronoUnit.DAYS.between(now, item.getExpiryDate());
                                response.put("active", true);
                                response.put("status", "BILLED");
                                response.put("memberType", "FAMILY_MEMBER");
                                response.put("memberName", familyMemberName);
                                response.put("relation", relation);
                                response.put("expiryDate", item.getExpiryDate());
                                response.put("remainingDays", remainingDays);
                                // Calculate duration from expiryDate for display
                                long daysFromBillToExpiry = ChronoUnit.DAYS.between(billDate, item.getExpiryDate());
                                response.put("duration", (int) daysFromBillToExpiry);
                                return response;
                            } else if (item.getExpiryDate() == null) {
                                // If no expiryDate and no duration, block for at least 30 days as safety measure
                                Instant allowedDate = billDate.plus(30, ChronoUnit.DAYS);
                                if (allowedDate.isAfter(now)) {
                                    long remainingDays = ChronoUnit.DAYS.between(now, allowedDate);
                                    response.put("active", true);
                                    response.put("status", "BILLED");
                                    response.put("memberType", "FAMILY_MEMBER");
                                    response.put("memberName", familyMemberName);
                                    response.put("relation", relation);
                                    response.put("billDate", billDate);
                                    response.put("allowedDate", allowedDate);
                                    response.put("duration", 30);
                                    response.put("remainingDays", remainingDays);
                                    return response;
                                }
                                // If 30 days passed, allow (don't block)
                            }
                        }
                    }
                }
            }
        }

        log.info("✅ [CHECK ACTIVE] No active prescription found - returning inactive response");
        log.info("📤 [CHECK ACTIVE] Final response: {}", response);
        return response;
    }

    // Bill prescription
    @Transactional
    public PrescriptionDTO bill(UUID id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Client pharmacist = clientRepo.findByEmail(auth.getName().toLowerCase())
                .orElseThrow(() -> new NotFoundException("PHARMACIST_NOT_FOUND"));

        Prescription prescription = prescriptionRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("PRESCRIPTION_NOT_FOUND"));

        // التحقق من أن الوصفة في حالة VERIFIED فقط
        if (prescription.getStatus() != PrescriptionStatus.VERIFIED) {
            throw new IllegalStateException("CAN_ONLY_BILL_VERIFIED_PRESCRIPTIONS");
        }

        // التحقق من أن الصيدلي هو نفسه الذي قام بالتحقق
        if (prescription.getPharmacist() == null ||
                !prescription.getPharmacist().getId().equals(pharmacist.getId())) {
            throw new IllegalStateException("ONLY_VERIFYING_PHARMACIST_CAN_BILL");
        }

        prescription.setStatus(PrescriptionStatus.BILLED);
        prescription.setUpdatedAt(Instant.now());
        prescriptionRepo.save(prescription);

        // 🔔 إشعار للمريض
        notificationService.sendToUser(
                prescription.getMember().getId(),
                "💊 تم صرف الأدوية من وصفتك الطبية بواسطة الصيدلي " + pharmacist.getFullName() +
                        " - المجموع: " + prescription.getTotalPrice() + " ₪. شكراً لاستخدامك خدماتنا."
        );

        // 🔔 إشعار للطبيب
        notificationService.sendToUser(
                prescription.getDoctor().getId(),
                "💊 تم صرف الأدوية من وصفتك للمريض " + prescription.getMember().getFullName() +
                        " بواسطة الصيدلي " + pharmacist.getFullName()
        );

        return prescriptionMapper.toDto(prescription, familyMemberRepo);
    }
}

