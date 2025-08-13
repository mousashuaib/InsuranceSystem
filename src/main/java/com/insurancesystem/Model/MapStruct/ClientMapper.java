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
public interface ClientMapper  {

    // Entity -> DTO  (نحوّل Set<Role> إلى Set<RoleName>)
    @Mapping(target = "roles", expression = "java( mapRoleNames(entity.getRoles()) )")
    @Mapping(target = "universityCardImage", source = "universityCardImage")

    ClientDto toDTO(Client entity);

    // Create DTO -> Entity (نترك passwordHash/roles/timestamps للخدمة)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Client toEntity(CreateClientDto dto);

    // Update جزئي (لا نلمس الحقول الحساسة)
    @BeanMapping(ignoreByDefault = false)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "roles", ignore = true)
    void updateEntityFromDTO(UpdateUserDTO dto, @MappingTarget Client entity);

    // ===== helpers =====
    default Set<RoleName> mapRoleNames(Set<Role> roles){
        return roles == null ? java.util.Set.of()
                : roles.stream().map(Role::getName).collect(Collectors.toSet());
    }
}
