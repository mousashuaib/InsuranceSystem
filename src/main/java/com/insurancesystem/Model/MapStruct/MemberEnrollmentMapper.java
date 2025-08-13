package com.insurancesystem.Model.MapStruct;

import com.insurancesystem.Model.Dto.CreateMemberEnrollmentDTO;
import com.insurancesystem.Model.Dto.MemberEnrollmentDTO;
import com.insurancesystem.Model.Dto.UpdateMemberEnrollmentDTO;
import com.insurancesystem.Model.Entity.MemberEnrollment;
import org.mapstruct.*;

@Mapper(config = MapStructConfig.class)
public interface MemberEnrollmentMapper {

    // Entity -> DTO (إرجاع المعرّفات)
    @Mapping(target = "userId", source = "member.id")
    @Mapping(target = "policyId", source = "policy.id")
    MemberEnrollmentDTO toDTO(MemberEnrollment entity);

    // Create DTO -> Entity (member/policy تُحمّل في الخدمة)
    @Mapping(target = "member", ignore = true)
    @Mapping(target = "policy", ignore = true)
    MemberEnrollment toEntity(CreateMemberEnrollmentDTO dto);

    // Update جزئي
    @BeanMapping(ignoreByDefault = false)
    @Mapping(target = "member", ignore = true)
    @Mapping(target = "policy", ignore = true)
    void updateEntityFromDTO(UpdateMemberEnrollmentDTO dto,
                             @MappingTarget MemberEnrollment entity);
}
