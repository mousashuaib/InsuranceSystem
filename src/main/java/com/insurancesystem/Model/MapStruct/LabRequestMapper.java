package com.insurancesystem.Model.MapStruct;

import com.insurancesystem.Model.Dto.LabRequestDTO;
import com.insurancesystem.Model.Entity.LabRequest;
import com.insurancesystem.Model.Entity.FamilyMember;
import com.insurancesystem.Repository.FamilyMemberRepository;
import org.mapstruct.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Optional;

@Mapper(componentModel = "spring")
public interface LabRequestMapper {
    
    Logger log = LoggerFactory.getLogger(LabRequestMapper.class);

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
    LabRequestDTO toDto(LabRequest request, @Context FamilyMemberRepository familyMemberRepo);

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
    default void extractFamilyMemberInfo(LabRequest entity, @MappingTarget LabRequestDTO.LabRequestDTOBuilder dto, @Context FamilyMemberRepository familyMemberRepo) {
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
        
        log.debug("🔍 [LabRequestMapper] Extracting family member info from entity. Notes: {}", notes != null ? (notes.length() > 200 ? notes.substring(0, 200) : notes) : "null");
        
        if (notes == null || notes.isEmpty()) {
            log.debug("⚠️ [LabRequestMapper] Notes and treatment are both null/empty, setting isFamilyMember=false");
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
                // Find family member by name, relation, and client
                Optional<FamilyMember> familyMemberOpt = familyMemberRepo.findByClient_IdAndFullNameAndRelation(
                        entity.getMember().getId(),
                        name,
                        com.insurancesystem.Model.Entity.Enums.FamilyRelation.valueOf(relation.toUpperCase())
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
            log.warn("⚠️ [MAPPER] Could not fetch family member from DB, falling back to parsing: {}", e.getMessage());
        }
        
        // Fallback: Parse from notes/treatment field if database lookup fails
        // Extract Insurance: after "Insurance:"
        int insuranceIdx = section.toLowerCase().indexOf("insurance:", parenEnd);
        if (insuranceIdx < 0) {
            return;
        }
        
        int insuranceStart = insuranceIdx + "insurance:".length();
        String afterInsurance = section.substring(insuranceStart).trim();
        
        // Insurance number is until next " - " or "Family Member:" or end
        String insurance = afterInsurance;
        int dashIdx = insurance.indexOf(" - ");
        int familyIdx = insurance.toLowerCase().indexOf("family member:");
        int endIdx = Math.min(dashIdx > 0 ? dashIdx : Integer.MAX_VALUE, 
                             familyIdx > 0 ? familyIdx : Integer.MAX_VALUE);
        if (endIdx < Integer.MAX_VALUE) {
            insurance = insurance.substring(0, endIdx).trim();
        }
        
        // Extract Age: after "Age:"
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
        
        // Extract Gender: after "Gender:"
        String gender = null;
        int genderIdx = section.toLowerCase().indexOf("gender:");
        if (genderIdx > 0) {
            int genderStart = genderIdx + "gender:".length();
            String afterGender = section.substring(genderStart).trim();
            // Gender is usually at the end or before new "Family Member:"
            int genderEnd = afterGender.toLowerCase().indexOf("family member:");
            if (genderEnd > 0) {
                gender = afterGender.substring(0, genderEnd).trim();
            } else {
                gender = afterGender.trim();
            }
        }
        
        // Set DTO fields
        dto.isFamilyMember(true);
        dto.familyMemberName(name);
        dto.familyMemberRelation(relation);
        dto.familyMemberInsuranceNumber(insurance);
        dto.familyMemberAge(age);
        dto.familyMemberGender(gender != null ? gender.toUpperCase() : null);
    }
    LabRequest toEntity(LabRequestDTO dto);
}
