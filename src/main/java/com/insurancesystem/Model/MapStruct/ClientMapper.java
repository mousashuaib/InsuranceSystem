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

    // =========================
    // Entity ➜ DTO
    // =========================
    @Mapping(target = "roles", expression = "java(mapRoleNames(entity.getRoles()))")

    // ✅ استنتاج هل عنده أمراض مزمنة
    @Mapping(
            target = "hasChronicDiseases",
            expression = "java(entity.getChronicDiseases() != null && !entity.getChronicDiseases().isEmpty())"
    )

    // ✅ اسم البوليصة
    @Mapping(
            target = "policyName",
            expression = "java(entity.getPolicy() != null ? entity.getPolicy().getName() : null)"
    )

    @Mapping(target = "universityCardImages", source = "universityCardImages")
    @Mapping(target = "chronicDocumentPaths", source = "chronicDocumentPaths")
    @Mapping(target = "gender", source = "gender")
    ClientDto toDTO(Client entity);

    // =========================
    // DTO ➜ Entity (Create)
    // =========================
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "universityCardImages", ignore = true)
    @Mapping(target = "gender", source = "gender")
    @Mapping(target = "chronicDiseases", ignore = true) // ❗ لا تُنشأ هنا
    Client toEntity(CreateClientDto dto);

    // =========================
    // DTO ➜ Entity (Update)
    // =========================
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "fullName", source = "fullName")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "phone", source = "phone")

    // ❌ ممنوع تعديل بيانات حساسة
    @Mapping(target = "employeeId", ignore = true)
    @Mapping(target = "department", ignore = true)
    @Mapping(target = "faculty", ignore = true)
    @Mapping(target = "specialization", ignore = true)
    @Mapping(target = "clinicLocation", ignore = true)
    @Mapping(target = "pharmacyCode", ignore = true)
    @Mapping(target = "pharmacyName", ignore = true)
    @Mapping(target = "pharmacyLocation", ignore = true)
    @Mapping(target = "nationalId", ignore = true)
    @Mapping(target = "dateOfBirth", ignore = true)
    @Mapping(target = "chronicDiseases", ignore = true)
    @Mapping(target = "gender", source = "gender")
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "universityCardImages", ignore = true)
    void updateEntityFromDTO(UpdateUserDTO dto, @MappingTarget Client entity);

    // =========================
    // Helpers
    // =========================
    default Set<RoleName> mapRoleNames(Set<Role> roles) {
        return roles == null
                ? Set.of()
                : roles.stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
    }
}
