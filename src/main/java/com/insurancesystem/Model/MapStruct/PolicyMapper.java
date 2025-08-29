package com.insurancesystem.Model.MapStruct;

import com.insurancesystem.Model.Dto.CreatePolicyDTO;
import com.insurancesystem.Model.Dto.PolicyDTO;
import com.insurancesystem.Model.Dto.UpdatePolicyDTO;
import com.insurancesystem.Model.Entity.Policy;
import org.mapstruct.*;

@Mapper(config = MapStructConfig.class)
public interface PolicyMapper {

    PolicyDTO toDTO(Policy policy);

    // Create DTO -> Entity (الـ coverages/timestamps تُدار بالخدمة/الـ JPA)
    @Mapping(target = "coverages", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Policy toEntity(CreatePolicyDTO dto);

    // Update جزئي
    @BeanMapping(ignoreByDefault = false)
    @Mapping(target = "coverages", ignore = true)
    void updateEntityFromDTO(UpdatePolicyDTO dto, @MappingTarget Policy entity);
}
