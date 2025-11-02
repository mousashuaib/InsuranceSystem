package com.insurancesystem.Model.MapStruct;

import com.insurancesystem.Model.Dto.RadiologyRequestDTO;
import com.insurancesystem.Model.Entity.RadiologyRequest;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface RadiologyRequestMapper {

    // ✅ Entity → DTO
    @Mapping(source = "doctor.id", target = "doctorId")
    @Mapping(source = "doctor.fullName", target = "doctorName")
    @Mapping(source = "member.id", target = "memberId")
    @Mapping(source = "member.fullName", target = "memberName")
    @Mapping(source = "radiologist.id", target = "radiologistId")  // 🟢 Added for radiologist
    @Mapping(source = "radiologist.fullName", target = "radiologistName")  // 🟢 Added for radiologist
    RadiologyRequestDTO toDto(RadiologyRequest radiologyRequest);

    // ✅ DTO → Entity
    @Mapping(source = "memberId", target = "member.id")
    @Mapping(source = "doctorId", target = "doctor.id")
    @Mapping(source = "radiologistId", target = "radiologist.id")  // 🟢 Added for radiologist
    @Mapping(target = "doctor", ignore = true)  // The doctor will be added in the service layer
    @Mapping(target = "radiologist", ignore = true)  // The radiologist will be added in the service layer
    @Mapping(target = "member", ignore = true)  // The member will be added in the service layer
    RadiologyRequest toEntity(RadiologyRequestDTO radiologyRequestDTO);
}
