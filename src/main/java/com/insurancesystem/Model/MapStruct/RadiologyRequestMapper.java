package com.insurancesystem.Model.MapStruct;

import com.insurancesystem.Model.Dto.RadiologyRequestDTO;
import com.insurancesystem.Model.Entity.RadiologyRequest;
import com.insurancesystem.Model.Entity.FamilyMember;
import com.insurancesystem.Repository.FamilyMemberRepository;
import org.mapstruct.*;
import java.util.Optional;

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

    @Mapping(target = "approvedPrice",
            expression = "java(request.getApprovedPrice() != null ? request.getApprovedPrice() : (request.getTest() != null ? request.getTest().getPrice() : null))")

    @Mapping(target = "employeeId",
            expression = "java(request.getMember() != null ? request.getMember().getEmployeeId() : null)")

    RadiologyRequestDTO toDto(RadiologyRequest request, @Context FamilyMemberRepository familyMemberRepo);

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
    default void extractFamilyMemberInfo(RadiologyRequest entity, @MappingTarget RadiologyRequestDTO.RadiologyRequestDTOBuilder dto, @Context FamilyMemberRepository familyMemberRepo) {
        if (entity == null || entity.getMember() == null) {
            return;
        }
        
        try {
            com.insurancesystem.Model.Entity.Client member = entity.getMember();
            String memberName = member.getFullName();

            // Extract university card image (first image from list)
            if (member.getUniversityCardImages() != null && !member.getUniversityCardImages().isEmpty()) {
                String firstImage = member.getUniversityCardImages().get(0);
                dto.universityCardImage(firstImage);
            }

            // Get dateOfBirth and calculate age
            java.time.LocalDate birthDate = member.getDateOfBirth();
            if (birthDate != null) {
                java.time.LocalDate today = java.time.LocalDate.now();
                int age = today.getYear() - birthDate.getYear();
                if (today.getMonthValue() < birthDate.getMonthValue() ||
                        (today.getMonthValue() == birthDate.getMonthValue() && today.getDayOfMonth() < birthDate.getDayOfMonth())) {
                    age--;
                }
                String ageStr = age > 0 ? age + " years" : null;
                dto.memberAge(ageStr);
            }

            // Get gender
            String gender = member.getGender();
            if (gender != null && !gender.trim().isEmpty()) {
                dto.memberGender(gender);
            }

            // Extract National ID from member
            String nationalId = member.getNationalId();
            if (nationalId != null && !nationalId.trim().isEmpty()) {
                dto.memberNationalId(nationalId);
            }
        } catch (org.hibernate.LazyInitializationException e) {
            // Silently handle - data will be null
        } catch (Exception e) {
            // Silently handle - data will be null
        }

        // Parse family member information from notes/treatment field
        dto.isFamilyMember(false);
        String notes = entity.getNotes();
        if (notes == null || notes.isEmpty()) {
            notes = entity.getTreatment();
        }
        
        if (notes == null || notes.isEmpty()) {
            return;
        }

        // Normalize: replace newlines with spaces and clean up multiple spaces
        String normalized = notes.replaceAll("\\r?\\n", " ").replaceAll("\\s+", " ").trim();
        
        // Find "Family Member:" (case insensitive)
        String lowerNormalized = normalized.toLowerCase();
        int idx = lowerNormalized.indexOf("family member:");
        if (idx < 0) {
            return;
        }
        
        // Extract section starting from "Family Member:"
        String section = normalized.substring(idx);
        
        // Extract Name: between "Family Member:" and "("
        int nameStart = "Family Member:".length();
        int parenStart = section.indexOf('(', nameStart);
        if (parenStart < 0) {
            return;
        }
        
        String name = section.substring(nameStart, parenStart).trim();
        
        // Extract Relation: between "(" and ")"
        int parenEnd = section.indexOf(')', parenStart);
        if (parenEnd < 0) {
            return;
        }
        
        String relation = section.substring(parenStart + 1, parenEnd).trim();
        
        // Try to find the FamilyMember in database to get accurate data
        try {
            if (entity.getMember() != null && familyMemberRepo != null) {
                Optional<FamilyMember> familyMemberOpt = familyMemberRepo.findByClient_IdAndFullNameAndRelation(
                        entity.getMember().getId(),
                        name,
                        com.insurancesystem.Model.Entity.Enums.FamilyRelation.valueOf(relation.toUpperCase())
                );
                
                if (familyMemberOpt.isPresent()) {
                    FamilyMember familyMember = familyMemberOpt.get();
                    
                    String insuranceNumber = familyMember.getInsuranceNumber();
                    
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
                    
                    String genderStr = null;
                    if (familyMember.getGender() != null) {
                        genderStr = familyMember.getGender().toString();
                    }
                    
                    String familyMemberNationalId = familyMember.getNationalId();
                    
                    dto.isFamilyMember(true);
                    dto.familyMemberName(name);
                    dto.familyMemberRelation(relation);
                    dto.familyMemberInsuranceNumber(insuranceNumber);
                    dto.familyMemberAge(ageStr);
                    dto.familyMemberGender(genderStr);
                    dto.familyMemberNationalId(familyMemberNationalId);
                    
                    return;
                }
            }
        } catch (Exception e) {
            // Fallback to parsing
        }
        
        // Fallback: Parse from notes/treatment field if database lookup fails
        int insuranceIdx = section.toLowerCase().indexOf("insurance:", parenEnd);
        if (insuranceIdx < 0) {
            return;
        }
        
        int insuranceStart = insuranceIdx + "insurance:".length();
        String afterInsurance = section.substring(insuranceStart).trim();
        
        String insurance = afterInsurance;
        int dashIdx = insurance.indexOf(" - ");
        int familyIdx = insurance.toLowerCase().indexOf("family member:");
        int endIdx = Math.min(dashIdx > 0 ? dashIdx : Integer.MAX_VALUE, 
                             familyIdx > 0 ? familyIdx : Integer.MAX_VALUE);
        if (endIdx < Integer.MAX_VALUE) {
            insurance = insurance.substring(0, endIdx).trim();
        }
        
        String age = null;
        int ageIdx = section.toLowerCase().indexOf("age:", insuranceIdx);
        if (ageIdx > 0) {
            int ageStart = ageIdx + "age:".length();
            String afterAge = section.substring(ageStart).trim();
            int ageEnd = afterAge.indexOf(" - ");
            if (ageEnd > 0) {
                age = afterAge.substring(0, ageEnd).trim();
            } else {
                age = afterAge.trim();
            }
        }
        
        String gender = null;
        int genderIdx = section.toLowerCase().indexOf("gender:");
        if (genderIdx > 0) {
            int genderStart = genderIdx + "gender:".length();
            String afterGender = section.substring(genderStart).trim();
            int genderEnd = afterGender.toLowerCase().indexOf("family member:");
            if (genderEnd > 0) {
                gender = afterGender.substring(0, genderEnd).trim();
            } else {
                gender = afterGender.trim();
            }
        }
        
        dto.isFamilyMember(true);
        dto.familyMemberName(name);
        dto.familyMemberRelation(relation);
        dto.familyMemberInsuranceNumber(insurance);
        dto.familyMemberAge(age);
        dto.familyMemberGender(gender != null ? gender.toUpperCase() : null);
    }
    RadiologyRequest toEntity(RadiologyRequestDTO dto);
}
