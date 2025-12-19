package com.insurancesystem.Model.MapStruct;

import com.insurancesystem.Model.Dto.LabRequestDTO;
import com.insurancesystem.Model.Dto.PrescriptionDTO;
import com.insurancesystem.Model.Entity.LabRequest;
import com.insurancesystem.Model.Entity.Prescription;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface LabRequestMapper {

    // ----------------------------
    // 🔹 ENTITY → DTO
    // ----------------------------
    @Mapping(source = "doctor.id", target = "doctorId")
    @Mapping(source = "doctor.fullName", target = "doctorName")

    @Mapping(source = "member.id", target = "memberId")
    @Mapping(source = "member.fullName", target = "memberName")

    @Mapping(source = "labTech.id", target = "labTechId")
    @Mapping(source = "labTech.fullName", target = "labTechName")

    @Mapping(source = "member.universityCardImages", target = "universityCardImages")


    @Mapping(source = "test.id", target = "testId")
    @Mapping(source = "test.serviceName", target = "serviceName")
    @Mapping(source = "test.price", target = "unionPrice")
    @Mapping(source = "member.employeeId", target = "employeeId")
    LabRequestDTO toDto(LabRequest request);

    // ----------------------------
    // 🔹 DTO → ENTITY
    // ----------------------------
    // ملاحظة: سيتم حقن doctor, member, test في Service يدويًا
    @Mapping(target = "doctor", ignore = true)
    @Mapping(target = "member", ignore = true)
    @Mapping(target = "labTech", ignore = true)
    @Mapping(target = "test", ignore = true)

    // سيتم ملؤها في الـ service
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    // 🔥 NEW — diagnosis + treatment
    @Mapping(source = "diagnosis", target = "diagnosis")
    @Mapping(source = "treatment", target = "treatment")
    @AfterMapping
    default void extractFamilyMemberInfo(@MappingTarget PrescriptionDTO dto, Prescription entity) {
        // Extract main client age and gender
        if (entity.getMember() != null) {
            try {
                com.insurancesystem.Model.Entity.Client member = entity.getMember();
                String memberName = member.getFullName();
                System.out.println("🔍 [MAPPER] Processing prescription " + entity.getId() + " for member: " + memberName);
                System.out.println("🔍 [MAPPER] Member object class: " + member.getClass().getName());

                // Get dateOfBirth directly
                java.time.LocalDate birthDate = member.getDateOfBirth();
                System.out.println("📅 [MAPPER] Member dateOfBirth: " + birthDate + " (null? " + (birthDate == null) + ")");

                if (birthDate != null) {
                    java.time.LocalDate today = java.time.LocalDate.now();
                    int age = today.getYear() - birthDate.getYear();
                    if (today.getMonthValue() < birthDate.getMonthValue() ||
                            (today.getMonthValue() == birthDate.getMonthValue() && today.getDayOfMonth() < birthDate.getDayOfMonth())) {
                        age--;
                    }
                    String ageStr = age > 0 ? age + " years" : null;
                    dto.setMemberAge(ageStr);
                    System.out.println("✅ [MAPPER] Set memberAge: " + ageStr + " for prescription " + entity.getId());
                } else {
                    dto.setMemberAge(null);
                    System.out.println("⚠️ [MAPPER] Member dateOfBirth is NULL for " + memberName);
                }

                // Get gender directly
                String gender = member.getGender();
                System.out.println("👤 [MAPPER] Member gender: " + gender + " (null? " + (gender == null) + ")");

                if (gender != null && !gender.trim().isEmpty()) {
                    dto.setMemberGender(gender);
                    System.out.println("✅ [MAPPER] Set memberGender: " + gender + " for prescription " + entity.getId());
                } else {
                    dto.setMemberGender(null);
                    System.out.println("⚠️ [MAPPER] Member gender is NULL or empty for " + memberName);
                }
            } catch (org.hibernate.LazyInitializationException e) {
                System.err.println("❌ [MAPPER] LazyInitializationException for prescription " + entity.getId() + ": " + e.getMessage());
                dto.setMemberAge(null);
                dto.setMemberGender(null);
            } catch (Exception e) {
                System.err.println("❌ [MAPPER] Error extracting member age/gender for prescription " + entity.getId() + ": " + e.getMessage());
                e.printStackTrace();
                dto.setMemberAge(null);
                dto.setMemberGender(null);
            }
        } else {
            System.out.println("⚠️ [MAPPER] Member is null for prescription " + entity.getId());
        }

        // Parse family member information from treatment field
        if (dto.getTreatment() == null || dto.getTreatment().isEmpty()) {
            dto.setIsFamilyMember(false);
            return;
        }

        // Pattern: "Family Member: [Name] ([Relation]) - Insurance: [Insurance Number] - Age: [Age] - Gender: [Gender]"
        String treatment = dto.getTreatment();
        String familyMemberPattern = "Family Member:\\s*([^-]+?)\\s*\\(([^)]+)\\)\\s*-\\s*Insurance:\\s*([^-]+?)(?:\\s*-\\s*Age:\\s*([^-]+?))?(?:\\s*-\\s*Gender:\\s*([^\\n\\r]+))?";

        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(familyMemberPattern, java.util.regex.Pattern.CASE_INSENSITIVE);
        java.util.regex.Matcher matcher = pattern.matcher(treatment);

        if (matcher.find()) {
            dto.setIsFamilyMember(true);
            dto.setFamilyMemberName(matcher.group(1).trim());
            dto.setFamilyMemberRelation(matcher.group(2).trim());
            dto.setFamilyMemberInsuranceNumber(matcher.group(3).trim());

            if (matcher.groupCount() >= 4 && matcher.group(4) != null) {
                dto.setFamilyMemberAge(matcher.group(4).trim());
            } else {
                dto.setFamilyMemberAge(null);
            }

            if (matcher.groupCount() >= 5 && matcher.group(5) != null) {
                dto.setFamilyMemberGender(matcher.group(5).trim());
            } else {
                dto.setFamilyMemberGender(null);
            }
        } else {
            dto.setIsFamilyMember(false);
        }
    }
    LabRequest toEntity(LabRequestDTO dto);
}
