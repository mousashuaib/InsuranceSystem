package com.insurancesystem.Model.MapStruct;

import com.insurancesystem.Model.Dto.LabRequestDTO;
import com.insurancesystem.Model.Entity.LabRequest;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface LabRequestMapper {

    @Mapping(source = "doctor.id", target = "doctorId")
    @Mapping(source = "member.id", target = "memberId")
    LabRequestDTO toDto(LabRequest request);

    @Mapping(source = "memberId", target = "member.id")
    @Mapping(target = "doctor", ignore = true) // يضاف من Service
    LabRequest toEntity(LabRequestDTO dto);
}
