package com.insurancesystem.Model.MapStruct;

import com.insurancesystem.Model.Dto.EmergencyRequestDTO;
import com.insurancesystem.Model.Entity.Client;
import com.insurancesystem.Model.Entity.EmergencyRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {})
public interface EmergencyRequestMapper {

    @Mapping(target = "memberId", source = "member.id")
    @Mapping(target = "memberName", source = "member.fullName")
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
    @Mapping(source = "member.universityCardImage", target = "universityCardImage")
    EmergencyRequestDTO toDto(EmergencyRequest entity);

    @Mapping(target = "member", ignore = true)
    @Mapping(target = "doctorId", ignore = true)
    EmergencyRequest toEntity(EmergencyRequestDTO dto);
}
