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

    // Entity -> DTO
    @Mapping(target = "roles", expression = "java(mapRoleNames(entity.getRoles()))")
    // 🔽 ما في داعي تكتب source = "universityCardImage" لأن الاسم نفسه، MapStruct بياخده تلقائي
    ClientDto toDTO(Client entity);

    // Create DTO -> Entity
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    // 🔽 نخلي السيرفس يحدد الصورة عند الرفع
    @Mapping(target = "universityCardImage", ignore = true)
    Client toEntity(CreateClientDto dto);

    // Update DTO -> Entity
    @BeanMapping(ignoreByDefault = false)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "roles", ignore = true)
    // 🔽 برضه ما نسمح بالمسح التلقائي للصورة
    @Mapping(target = "universityCardImage", ignore = true)
    void updateEntityFromDTO(UpdateUserDTO dto, @MappingTarget Client entity);

    // ===== helpers =====
    default Set<RoleName> mapRoleNames(Set<Role> roles) {
        return roles == null ? java.util.Set.of()
                : roles.stream().map(Role::getName).collect(Collectors.toSet());
    }
}
