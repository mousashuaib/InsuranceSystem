package com.insurancesystem.Model.MapStruct;

import com.insurancesystem.Model.Dto.FamilyMemberDTO;
import com.insurancesystem.Model.Entity.FamilyMember;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FamilyMemberMapper {
    @Mapping(source = "status", target = "status")
    @Mapping(target = "clientFullName", source = "client.fullName")
    @Mapping(target = "clientId", source = "client.id")
    @Mapping(target = "documentImages", source = "documentImages")
    @Mapping(target = "clientNationalId", source = "client.nationalId")
    @Mapping(target = "clientStatus", source = "client.status")
    FamilyMemberDTO toDto(FamilyMember entity);
}
