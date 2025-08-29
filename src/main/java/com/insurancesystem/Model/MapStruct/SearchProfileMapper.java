package com.insurancesystem.Model.MapStruct;

import com.insurancesystem.Model.Dto.SearchProfileDto;
import com.insurancesystem.Model.Entity.SearchProfile;
import org.mapstruct.*;
@Mapper(componentModel = "spring")
public interface SearchProfileMapper {

    @Mapping(source = "owner.fullName", target = "ownerName")
    SearchProfileDto toDto(SearchProfile searchProfile);

    @InheritInverseConfiguration(name = "toDto")
    @Mapping(target = "owner", ignore = true)
    SearchProfile toEntity(SearchProfileDto dto);
}
