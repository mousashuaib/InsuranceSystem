package com.insurancesystem.Model.MapStruct;

import com.insurancesystem.Model.Dto.ClaimDTO;
import com.insurancesystem.Model.Dto.CreateClaimDTO;
import com.insurancesystem.Model.Entity.Claim;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ClaimMapper {

    // Entity → DTO
    @Mapping(source = "member.id", target = "memberId")
    @Mapping(source = "member.fullName", target = "memberName")
    @Mapping(source = "policy.id", target = "policyId")
    @Mapping(source = "policy.name", target = "policyName")
    ClaimDTO toDto(Claim entity);

    // CreateClaimDTO → Entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "member", ignore = true)
    @Mapping(target = "policy", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "submittedAt", ignore = true)
    @Mapping(target = "approvedAt", ignore = true)
    @Mapping(target = "rejectedAt", ignore = true)
    @Mapping(target = "rejectionReason", ignore = true)
    @Mapping(target = "invoiceImagePath", ignore = true)
    Claim toEntity(CreateClaimDTO dto);
}
