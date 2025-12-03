package com.insurancesystem.Model.MapStruct;

import com.insurancesystem.Model.Dto.CoverageDTO;
import com.insurancesystem.Model.Dto.CreateCoverageDTO;
import com.insurancesystem.Model.Dto.UpdateCoverageDTO;
import com.insurancesystem.Model.Entity.Coverage;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface CoverageMapper {

    @Mapping(source = "policyId", target = "policy.id")
    Coverage toEntity(CreateCoverageDTO dto);

    @Mapping(source = "policy.id", target = "policyId")
    CoverageDTO toDTO(Coverage entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDTO(UpdateCoverageDTO dto, @MappingTarget Coverage entity);
}
