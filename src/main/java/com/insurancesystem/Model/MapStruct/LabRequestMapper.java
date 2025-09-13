package com.insurancesystem.Model.MapStruct;

import com.insurancesystem.Model.Dto.LabRequestDTO;
import com.insurancesystem.Model.Entity.LabRequest;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface LabRequestMapper {

    // ✅ Entity → DTO
    @Mapping(source = "doctor.id", target = "doctorId")
    @Mapping(source = "doctor.fullName", target = "doctorName")
    @Mapping(source = "member.id", target = "memberId")
    @Mapping(source = "member.fullName", target = "memberName")
    @Mapping(source = "labTech.id", target = "labTechId")          // 🟢 أضف هذا
    @Mapping(source = "labTech.fullName", target = "labTechName")  // 🟢 وأضف هذا
    LabRequestDTO toDto(LabRequest request);

    // ✅ DTO → Entity
    @Mapping(source = "memberId", target = "member.id")
    @Mapping(target = "doctor", ignore = true)   // يضاف من Service
    @Mapping(target = "labTech", ignore = true)  // يضاف من Service
    @Mapping(target = "member", ignore = true)   // يضاف من Service
    LabRequest toEntity(LabRequestDTO dto);
}

