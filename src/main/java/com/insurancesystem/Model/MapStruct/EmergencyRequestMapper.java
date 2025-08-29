package com.insurancesystem.Model.MapStruct;

import com.insurancesystem.Model.Dto.EmergencyRequestDTO;
import com.insurancesystem.Model.Entity.EmergencyRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EmergencyRequestMapper {

    @Mapping(source = "member.id", target = "memberId")
    @Mapping(source = "member.fullName", target = "memberName")
    EmergencyRequestDTO toDto(EmergencyRequest emergencyRequest);
}
