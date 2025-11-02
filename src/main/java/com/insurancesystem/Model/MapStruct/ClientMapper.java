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
    @Mapping(target = "fullName", source = "fullName", ignore = false)
    @Mapping(target = "email", source = "email", ignore = false)
    @Mapping(target = "phone", source = "phone", ignore = false)
    @Mapping(target = "employeeId", source = "employeeId", ignore = false)
    @Mapping(target = "department", source = "department", ignore = false)
    @Mapping(target = "faculty", source = "faculty", ignore = false)
    @Mapping(target = "specialization", source = "specialization", ignore = false)
    @Mapping(target = "clinicLocation", source = "clinicLocation", ignore = false)
    @Mapping(target = "pharmacyCode", source = "pharmacyCode", ignore = false)
    @Mapping(target = "pharmacyName", source = "pharmacyName", ignore = false)
    @Mapping(target = "pharmacyLocation", source = "pharmacyLocation", ignore = false)
    @Mapping(target = "labCode", source = "labCode", ignore = false)
    @Mapping(target = "labName", source = "labName", ignore = false)
    @Mapping(target = "labLocation", source = "labLocation", ignore = false)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "universityCardImage", ignore = true)
    void updateEntityFromDTO(UpdateUserDTO dto, @MappingTarget Client entity);

    default Set<RoleName> mapRoleNames(Set<Role> roles) {
        return roles == null ? Set.of() :
                roles.stream().map(Role::getName).collect(Collectors.toSet());
    }
}