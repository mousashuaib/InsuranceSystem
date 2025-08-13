package com.insurancesystem.Model.MapStruct;

import com.insurancesystem.Model.Dto.CoverageDTO;
import com.insurancesystem.Model.Dto.CreateCoverageDTO;
import com.insurancesystem.Model.Dto.UpdateCoverageDTO;
import com.insurancesystem.Model.Entity.Coverage;
import org.mapstruct.*;

@Mapper(config = MapStructConfig.class)
public interface CoverageMapper {

    // Entity -> DTO (policyId بدل كائن Policy)
    @Mapping(target = "policyId", source = "policy.id")
    CoverageDTO toDTO(Coverage coverage);

    // Create DTO -> Entity (policy يُضبط في الخدمة)
    @Mapping(target = "policy", ignore = true)
    Coverage toEntity(CreateCoverageDTO dto);

    // Update جزئي
    @BeanMapping(ignoreByDefault = false)
    @Mapping(target = "policy", ignore = true)
    void updateEntityFromDTO(UpdateCoverageDTO dto, @MappingTarget Coverage entity);
}
