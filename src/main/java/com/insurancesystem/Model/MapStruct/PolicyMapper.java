package com.insurancesystem.Model.MapStruct;

import com.insurancesystem.Model.Dto.CreatePolicyDTO;
import com.insurancesystem.Model.Dto.PolicyDTO;
import com.insurancesystem.Model.Dto.UpdatePolicyDTO;
import com.insurancesystem.Model.Entity.Policy;
import org.mapstruct.*;
@Mapper(config = MapStructConfig.class, uses = {CoverageMapper.class})
public interface PolicyMapper {

    @Mapping(target = "coverages", source = "coverages")
    PolicyDTO toDTO(Policy policy);

    @Mapping(target = "coverages", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Policy toEntity(CreatePolicyDTO dto);

    @BeanMapping(ignoreByDefault = false)
    @Mapping(target = "coverages", ignore = true)
    void updateEntityFromDTO(UpdatePolicyDTO dto, @MappingTarget Policy entity);
}
