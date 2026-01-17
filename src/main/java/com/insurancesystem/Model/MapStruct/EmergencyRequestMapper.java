package com.insurancesystem.Model.MapStruct;

import com.insurancesystem.Model.Dto.EmergencyRequestDTO;
import com.insurancesystem.Model.Entity.EmergencyRequest;
import com.insurancesystem.Model.Entity.FamilyMember;
import com.insurancesystem.Repository.FamilyMemberRepository;
import org.mapstruct.*;
import java.util.Optional;

@Mapper(componentModel = "spring")
public interface EmergencyRequestMapper {

    @Mapping(target = "memberId", source = "member.id")
    @Mapping(target = "memberName", source = "member.fullName")
    @Mapping(target = "employeeId", source = "member.employeeId")
    @Mapping(target = "universityCardImages", source = "member.universityCardImages")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "location", source = "location")
    @Mapping(target = "contactPhone", source = "contactPhone")
    @Mapping(target = "incidentDate", source = "incidentDate")
    @Mapping(target = "notes", source = "notes")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "submittedAt", source = "submittedAt")
    @Mapping(target = "approvedAt", source = "approvedAt")
    @Mapping(target = "rejectedAt", source = "rejectedAt")
    @Mapping(target = "rejectionReason", source = "rejectionReason")
    EmergencyRequestDTO toDto(EmergencyRequest entity, @Context FamilyMemberRepository familyMemberRepo);

    @AfterMapping
    default void extractFamilyMemberInfo(EmergencyRequest entity, @MappingTarget EmergencyRequestDTO.EmergencyRequestDTOBuilder dto, @Context FamilyMemberRepository familyMemberRepo) {
        if (entity == null || entity.getMember() == null) {
            return;
        }
        
        try {
            com.insurancesystem.Model.Entity.Client member = entity.getMember();

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

        // Extract family member information using familyMemberId from entity (if exists)
        dto.isFamilyMember(false);
        
        // Use familyMemberId directly from entity (no need to parse notes)
        if (entity.getFamilyMemberId() != null && familyMemberRepo != null) {
            try {
                Optional<FamilyMember> familyMemberOpt = familyMemberRepo.findById(entity.getFamilyMemberId());
                
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
                    String relationStr = familyMember.getRelation() != null ? familyMember.getRelation().toString() : null;
                    
                    dto.isFamilyMember(true);
                    dto.familyMemberId(familyMember.getId());
                    dto.familyMemberName(familyMember.getFullName());
                    dto.familyMemberRelation(relationStr);
                    dto.familyMemberInsuranceNumber(insuranceNumber);
                    dto.familyMemberAge(ageStr);
                    dto.familyMemberGender(genderStr);
                    dto.familyMemberNationalId(familyMemberNationalId);
                }
            } catch (Exception e) {
                // Silently handle - data will be null
            }
        }
    }

    @Mapping(target = "member", ignore = true)
    @Mapping(target = "doctorId", ignore = true)
    EmergencyRequest toEntity(EmergencyRequestDTO dto);
}