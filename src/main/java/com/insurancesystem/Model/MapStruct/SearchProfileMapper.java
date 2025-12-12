package com.insurancesystem.Model.MapStruct;

import com.insurancesystem.Model.Dto.SearchProfileDto;
import com.insurancesystem.Model.Entity.SearchProfile;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface SearchProfileMapper {

    // ✅ تحويل Entity → DTO
    @Mapping(source = "owner.fullName", target = "ownerName")
    @Mapping(source = "medicalLicense", target = "medicalLicense")
    @Mapping(source = "universityDegree", target = "universityDegree")
    @Mapping(source = "clinicRegistration", target = "clinicRegistration")
    @Mapping(source = "idOrPassportCopy", target = "idOrPassportCopy")
    SearchProfileDto toDto(SearchProfile searchProfile);

    // ✅ تحويل DTO → Entity
    @InheritInverseConfiguration(name = "toDto")
    @Mapping(target = "owner", ignore = true) // لأنك تجيب الـ owner من الـ SecurityContext
    SearchProfile toEntity(SearchProfileDto dto);
}
