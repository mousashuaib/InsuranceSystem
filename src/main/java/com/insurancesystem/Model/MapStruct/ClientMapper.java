package com.insurancesystem.Model.MapStruct;

import com.insurancesystem.Model.Dto.ClientDto;
import com.insurancesystem.Model.Dto.CreateClientDto;
import com.insurancesystem.Model.Dto.UpdateUserDTO;
import com.insurancesystem.Model.Entity.Client;
import com.insurancesystem.Model.Entity.Role;
import com.insurancesystem.Model.Entity.Enums.RoleName;
import org.mapstruct.*;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(config = MapStructConfig.class)
public interface ClientMapper {

    @Mapping(target = "roles", expression = "java(mapRoleNames(entity.getRoles()))")
    ClientDto toDTO(Client entity);

    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "universityCardImage", ignore = true)
    Client toEntity(CreateClientDto dto);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "fullName", source = "fullName")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "phone", source = "phone")
    @Mapping(target = "employeeId", source = "employeeId")
    @Mapping(target = "department", source = "department")
    @Mapping(target = "faculty", source = "faculty")
    @Mapping(target = "specialization", source = "specialization")
    @Mapping(target = "clinicLocation", source = "clinicLocation")
    @Mapping(target = "pharmacyCode", source = "pharmacyCode")
    @Mapping(target = "pharmacyName", source = "pharmacyName")
    @Mapping(target = "pharmacyLocation", source = "pharmacyLocation")
    @Mapping(target = "labCode", source = "labCode")
    @Mapping(target = "labName", source = "labName")
    @Mapping(target = "labLocation", source = "labLocation")
    @Mapping(target = "radiologyCode", source = "labCode")
    @Mapping(target = "radiologyName", source = "labName")
    @Mapping(target = "radiologyLocation", source = "labLocation")

    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "universityCardImage", ignore = true)
    void updateEntityFromDTO(UpdateUserDTO dto, @MappingTarget Client entity);

    default Set<RoleName> mapRoleNames(Set<Role> roles) {
        return roles == null ? Set.of() :
                roles.stream().map(Role::getName).collect(Collectors.toSet());
    }
}
