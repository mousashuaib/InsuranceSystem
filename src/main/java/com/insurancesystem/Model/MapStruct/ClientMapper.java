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

    // ✅ Entity -> DTO
    @Mapping(target = "roles", expression = "java(mapRoleNames(entity.getRoles()))")
    ClientDto toDTO(Client entity);

    // ✅ Create DTO -> Entity
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "universityCardImage", ignore = true) // نخلي السيرفس يحددها
    Client toEntity(CreateClientDto dto);

    // ✅ Update DTO -> Entity
    @BeanMapping(ignoreByDefault = true) // نخلي MapStruct يحدث بس اللي مبعوت
    @Mapping(target = "fullName", source = "fullName", ignore = false)
    @Mapping(target = "email", source = "email", ignore = false)
    @Mapping(target = "phone", source = "phone", ignore = false)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "universityCardImage", ignore = true)
    void updateEntityFromDTO(UpdateUserDTO dto, @MappingTarget Client entity);

    // ===== Helpers =====
    default Set<RoleName> mapRoleNames(Set<Role> roles) {
        return roles == null ? Set.of()
                : roles.stream().map(Role::getName).collect(Collectors.toSet());
    }
}
