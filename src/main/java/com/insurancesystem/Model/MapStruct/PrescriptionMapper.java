package com.insurancesystem.Model.MapStruct;

import com.insurancesystem.Model.Dto.PrescriptionDTO;
import com.insurancesystem.Model.Entity.Prescription;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = {PrescriptionItemMapper.class})
public interface PrescriptionMapper {

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
    PrescriptionDTO toDto(Prescription entity);

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

