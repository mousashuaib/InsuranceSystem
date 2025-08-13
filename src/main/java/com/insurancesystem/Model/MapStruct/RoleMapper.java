package com.insurancesystem.Model.MapStruct;

import com.insurancesystem.Model.Dto.RoleDTO;
import com.insurancesystem.Model.Entity.Role;
import org.mapstruct.Mapper;

@Mapper(config = MapStructConfig.class)
public interface RoleMapper {
    RoleDTO toDTO(Role role);
}
