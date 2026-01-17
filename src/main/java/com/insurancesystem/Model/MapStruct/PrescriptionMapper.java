package com.insurancesystem.Model.MapStruct;

import com.insurancesystem.Model.Dto.PrescriptionDTO;
import com.insurancesystem.Model.Entity.Prescription;
import com.insurancesystem.Model.Entity.FamilyMember;
import com.insurancesystem.Repository.FamilyMemberRepository;
import org.mapstruct.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.UUID;
import java.util.Optional;

@Mapper(componentModel = "spring", uses = {PrescriptionItemMapper.class})
public interface PrescriptionMapper {
    
    Logger log = LoggerFactory.getLogger(PrescriptionMapper.class);

    @Mapping(target = "pharmacistId",
            expression = "java(entity.getPharmacist() != null ? entity.getPharmacist().getId() : null)")
    @Mapping(target = "pharmacistName",
            expression = "java(entity.getPharmacist() != null ? entity.getPharmacist().getFullName() : null)")

    @Mapping(target = "doctorName",
            expression = "java(entity.getDoctor() != null ? entity.getDoctor().getFullName() : null)")

    @Mapping(target = "memberId",
            expression = "java(entity.getMember() != null ? entity.getMember().getId() : null)")
    @Mapping(target = "memberName",
            expression = "java(entity.getMember() != null ? entity.getMember().getFullName() : null)")

    @Mapping(target = "employeeId",
            expression = "java(entity.getMember() != null ? entity.getMember().getEmployeeId() : null)")

    @Mapping(
            target = "universityCardImages",
            expression = "java(entity.getMember()!=null ? entity.getMember().getUniversityCardImages() : null)"
    )

    @Mapping(source = "items", target = "items")
    @Mapping(source = "totalPrice", target = "totalPrice")
    @Mapping(source = "diagnosis", target = "diagnosis")
    @Mapping(source = "treatment", target = "treatment")
    @Mapping(source = "isChronic", target = "isChronic")
    PrescriptionDTO toDto(Prescription entity, @Context FamilyMemberRepository familyMemberRepo);

    @AfterMapping
    default void extractFamilyMemberInfo(Prescription entity, @MappingTarget PrescriptionDTO.PrescriptionDTOBuilder dto, @Context FamilyMemberRepository familyMemberRepo) {
        // Extract university card image (first image from list)
        if (entity.getMember() != null && entity.getMember().getUniversityCardImages() != null && !entity.getMember().getUniversityCardImages().isEmpty()) {
            String firstImage = entity.getMember().getUniversityCardImages().get(0);
            dto.universityCardImage(firstImage);
            log.info("✅ [MAPPER] Set universityCardImage: {} for prescription {}", firstImage, entity.getId());
        } else {
            dto.universityCardImage(null);
        }
        
        // Extract main client age and gender
        if (entity.getMember() != null) {
            try {
                com.insurancesystem.Model.Entity.Client member = entity.getMember();

                String memberName = member.getFullName();
                java.time.LocalDate birthDate = member.getDateOfBirth();
                String gender = member.getGender();

                if (birthDate != null) {
                    java.time.LocalDate today = java.time.LocalDate.now();
                    int age = today.getYear() - birthDate.getYear();
                    if (today.getMonthValue() < birthDate.getMonthValue() ||
                            (today.getMonthValue() == birthDate.getMonthValue() && today.getDayOfMonth() < birthDate.getDayOfMonth())) {
                        age--;
                    }
                    String ageStr = age > 0 ? age + " years" : null;
                    dto.memberAge(ageStr);
                    log.info("✅ [MAPPER] Set memberAge: {} for prescription {}", ageStr, entity.getId());
                } else {
                    dto.memberAge(null);
                    log.warn("⚠️ [MAPPER] Member dateOfBirth is NULL for {} in prescription {}", memberName, entity.getId());
                }

                if (gender != null && !gender.trim().isEmpty()) {
                    dto.memberGender(gender);
                    log.info("✅ [MAPPER] Set memberGender: {} for prescription {}", gender, entity.getId());
                } else {
                    dto.memberGender(null);
                    log.warn("⚠️ [MAPPER] Member gender is NULL or empty for {} in prescription {}", memberName, entity.getId());
                }

                // Extract National ID from member
                String nationalId = member.getNationalId();
                if (nationalId != null && !nationalId.trim().isEmpty()) {
                    dto.memberNationalId(nationalId);
                    log.info("✅ [MAPPER] Set memberNationalId: {} for prescription {}", nationalId, entity.getId());
                } else {
                    dto.memberNationalId(null);
                    log.warn("⚠️ [MAPPER] Member nationalId is NULL or empty for {} in prescription {}", memberName, entity.getId());
                }
            } catch (Exception e) {
                log.error("❌ [MAPPER] Error extracting member age/gender for prescription {}: {}", entity.getId(), e.getMessage(), e);
                dto.memberAge(null);
                dto.memberGender(null);
                dto.memberNationalId(null);
            }
        } else {
            log.warn("⚠️ [MAPPER] Member is null for prescription {}", entity.getId());
            dto.memberAge(null);
            dto.memberGender(null);
        }

        // Parse family member information from treatment field
        dto.isFamilyMember(false);
        String treatment = entity.getTreatment();
        if (treatment == null || treatment.isEmpty()) {
            return;
        }

        // First, try to parse family member info from treatment field to get name and relation
        String familyMemberPattern = "(?:\\n|^)\\s*Family Member:\\s*([^-]+?)\\s*\\(([^)]+)\\)\\s*-\\s*Insurance:\\s*([^-]+?)(?:\\s*-\\s*Age:\\s*([^-]+?))?(?:\\s*-\\s*Gender:\\s*([^\\n\\r]+))?";
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(familyMemberPattern, java.util.regex.Pattern.CASE_INSENSITIVE | java.util.regex.Pattern.MULTILINE);
        java.util.regex.Matcher matcher = pattern.matcher(treatment);

        if (matcher.find()) {
            dto.isFamilyMember(true);
            String familyMemberName = matcher.group(1).trim();
            String familyMemberRelation = matcher.group(2).trim();
            
            // Try to find the FamilyMember in database to get accurate data
            try {
                if (entity.getMember() != null && familyMemberRepo != null) {
                    // Find family member by name, relation, and client
                    Optional<FamilyMember> familyMemberOpt = familyMemberRepo.findByClient_IdAndFullNameAndRelation(
                            entity.getMember().getId(),
                            familyMemberName,
                            com.insurancesystem.Model.Entity.Enums.FamilyRelation.valueOf(familyMemberRelation.toUpperCase())
                    );
                    
                    if (familyMemberOpt.isPresent()) {
                        FamilyMember familyMember = familyMemberOpt.get();
                        
                        // Get insurance number directly from database
                        String insuranceNumber = familyMember.getInsuranceNumber();
                        
                        // Calculate age from date of birth
                        String ageStr = null;
                        if (familyMember.getDateOfBirth() != null) {
                            java.time.LocalDate today = java.time.LocalDate.now();
                            java.time.LocalDate birthDate = familyMember.getDateOfBirth();
                            int age = today.getYear() - birthDate.getYear();
                            if (today.getMonthValue() < birthDate.getMonthValue() ||
                                    (today.getMonthValue() == birthDate.getMonthValue() && today.getDayOfMonth() < birthDate.getDayOfMonth())) {
                                age--;
                            }
                            if (age > 0) {
                                ageStr = age + " years";
                            }
                        }
                        
                        // Get gender directly from database
                        String genderStr = null;
                        if (familyMember.getGender() != null) {
                            genderStr = familyMember.getGender().toString();
                        }
                        
                        // Get National ID from family member
                        String familyMemberNationalId = familyMember.getNationalId();
                        
                        dto.familyMemberName(familyMemberName);
                        dto.familyMemberRelation(familyMemberRelation);
                        dto.familyMemberInsuranceNumber(insuranceNumber);
                        dto.familyMemberAge(ageStr);
                        dto.familyMemberGender(genderStr);
                        dto.familyMemberNationalId(familyMemberNationalId);
                        
                        log.info("✅ [MAPPER] Extracted family member info from DB - Name: {}, Relation: {}, Insurance: {}, Age: {}, Gender: {}, NationalId: {}",
                                familyMemberName, familyMemberRelation, insuranceNumber, ageStr, genderStr, familyMemberNationalId);
                        return;
                    }
                }
            } catch (Exception e) {
                log.warn("⚠️ [MAPPER] Could not fetch family member from DB, falling back to parsing: {}", e.getMessage());
            }
            
            // Fallback: Parse from treatment field if database lookup fails
            String insuranceNumber = matcher.group(3).trim();
            String ageStr = null;
            String genderStr = null;

            // Extract age (group 4)
            if (matcher.groupCount() >= 4 && matcher.group(4) != null && !matcher.group(4).trim().isEmpty()) {
                String ageValue = matcher.group(4).trim();
                if (!ageValue.equalsIgnoreCase("N/A") && !ageValue.equalsIgnoreCase("null")) {
                    ageStr = ageValue;
                }
            }

            // Extract gender (group 5)
            if (matcher.groupCount() >= 5 && matcher.group(5) != null && !matcher.group(5).trim().isEmpty()) {
                String genderValue = matcher.group(5).trim();
                if (!genderValue.equalsIgnoreCase("N/A") && !genderValue.equalsIgnoreCase("null")) {
                    genderStr = genderValue;
                }
            }

            dto.familyMemberName(familyMemberName);
            dto.familyMemberRelation(familyMemberRelation);
            dto.familyMemberInsuranceNumber(insuranceNumber);
            dto.familyMemberAge(ageStr);
            dto.familyMemberGender(genderStr);

            log.info("✅ [MAPPER] Extracted family member info from treatment - Name: {}, Relation: {}, Insurance: {}, Age: {}, Gender: {}",
                    familyMemberName, familyMemberRelation, insuranceNumber, ageStr, genderStr);
        } else {
            log.warn("⚠️ [MAPPER] Family member pattern not found in treatment field for prescription {}", entity.getId());
        }
    }

    @Mapping(target = "pharmacist", ignore = true)
    @Mapping(target = "doctor", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "items", ignore = true)
    @Mapping(target = "totalPrice", ignore = true)
    @Mapping(source = "memberId", target = "member.id")
    @Mapping(source = "diagnosis", target = "diagnosis")
    @Mapping(source = "treatment", target = "treatment")
    Prescription toEntity(PrescriptionDTO dto);
}

