package com.insurancesystem.Model.MapStruct;

import com.insurancesystem.Model.Dto.PrescriptionDTO;
import com.insurancesystem.Model.Dto.RadiologyRequestDTO;
import com.insurancesystem.Model.Entity.Prescription;
import com.insurancesystem.Model.Entity.RadiologyRequest;
import org.mapstruct.*;
@Mapper(componentModel = "spring")
public interface RadiologyRequestMapper {

    // Entity → DTO
    @Mapping(target = "doctorId",
            expression = "java(request.getDoctor() != null ? request.getDoctor().getId() : null)")
    @Mapping(target = "doctorName",
            expression = "java(request.getDoctor() != null ? request.getDoctor().getFullName() : null)")

    @Mapping(target = "memberId",
            expression = "java(request.getMember() != null ? request.getMember().getId() : null)")
    @Mapping(target = "memberName",
            expression = "java(request.getMember() != null ? request.getMember().getFullName() : null)")

    @Mapping(target = "radiologistId",
            expression = "java(request.getRadiologist() != null ? request.getRadiologist().getId() : null)")
    @Mapping(target = "radiologistName",
            expression = "java(request.getRadiologist() != null ? request.getRadiologist().getFullName() : null)")

    @Mapping(
            target = "universityCardImages",
            expression = "java(request.getMember()!=null ? request.getMember().getUniversityCardImages() : null)"
    )

    @Mapping(target = "testId",
            expression = "java(request.getTest() != null ? request.getTest().getId() : null)")
    @Mapping(target = "testName",
            expression = "java(request.getTest() != null ? request.getTest().getServiceName() : null)")

    // انتبه: هاي بتعبي price تبع الاتحاد حتى لو فيه approvedPrice فعلي بالـ entity
    @Mapping(target = "approvedPrice",
            expression = "java(request.getApprovedPrice() != null ? request.getApprovedPrice() : (request.getTest() != null ? request.getTest().getPrice() : null))")

    @Mapping(target = "employeeId",
            expression = "java(request.getMember() != null ? request.getMember().getEmployeeId() : null)")

    RadiologyRequestDTO toDto(RadiologyRequest request);

    // DTO → Entity
    @Mapping(source = "doctorId", target = "doctor.id")
    @Mapping(source = "memberId", target = "member.id")
    @Mapping(source = "radiologistId", target = "radiologist.id")
    @Mapping(source = "testId", target = "test.id")

    @Mapping(target = "doctor", ignore = true)
    @Mapping(target = "member", ignore = true)
    @Mapping(target = "radiologist", ignore = true)
    @Mapping(target = "test", ignore = true)
    @AfterMapping
    default void extractFamilyMemberInfo(@MappingTarget PrescriptionDTO dto, Prescription entity) {
        // ✅ Extract main client age and gender (ADD THIS PART)
        if (entity.getMember() != null) {
            // Calculate age from date of birth
            if (entity.getMember().getDateOfBirth() != null) {
                java.time.LocalDate today = java.time.LocalDate.now();
                java.time.LocalDate birthDate = entity.getMember().getDateOfBirth();
                int age = today.getYear() - birthDate.getYear();
                if (today.getMonthValue() < birthDate.getMonthValue() ||
                        (today.getMonthValue() == birthDate.getMonthValue() && today.getDayOfMonth() < birthDate.getDayOfMonth())) {
                    age--;
                }
                dto.setMemberAge(age > 0 ? age + " years" : null);
            } else {
                dto.setMemberAge(null);
            }

            // Get gender
            if (entity.getMember().getGender() != null) {
                dto.setMemberGender(entity.getMember().getGender().toString());
            } else {
                dto.setMemberGender(null);
            }
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
    RadiologyRequest toEntity(RadiologyRequestDTO dto);
}
