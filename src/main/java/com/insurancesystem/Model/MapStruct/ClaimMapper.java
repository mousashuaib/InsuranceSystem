package com.insurancesystem.Model.MapStruct;

import com.insurancesystem.Model.Dto.ClaimDTO;
import com.insurancesystem.Model.Dto.CreateClaimDTO;
import com.insurancesystem.Model.Entity.Claim;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
@Mapper(componentModel = "spring")
public interface ClaimMapper {

    @Mapping(source = "member.id", target = "memberId")
    @Mapping(source = "member.fullName", target = "memberName")
    @Mapping(source = "policy.id", target = "policyId")
    @Mapping(source = "policy.name", target = "policyName")
    @Mapping(source = "medicalReviewer.fullName", target = "medicalReviewerName")
    @Mapping(source = "adminReviewer.fullName", target = "adminReviewerName")
    @Mapping(target = "emergency", source = "emergency")
    ClaimDTO toDto(Claim claim);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "member", ignore = true)
    @Mapping(target = "policy", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "invoiceImagePath", ignore = true)
    @Mapping(target = "submittedAt", ignore = true)
    @Mapping(target = "approvedAt", ignore = true)
    @Mapping(target = "rejectedAt", ignore = true)
    @Mapping(target = "medicalReviewedAt", ignore = true)
    @Mapping(target = "adminReviewedAt", ignore = true)
    @Mapping(target = "medicalReviewer", ignore = true)
    @Mapping(target = "adminReviewer", ignore = true)
    @Mapping(target = "emergency", source = "emergency")
    @Mapping(target = "isCovered", ignore = true)
    @Mapping(target = "coverageMessage", ignore = true)
    @Mapping(target = "insuranceCoveredAmount", ignore = true)
    @Mapping(target = "clientPayAmount", ignore = true)
    @Mapping(target = "coveragePercentUsed", ignore = true)
    @Mapping(target = "maxCoverageUsed", ignore = true)
    Claim toEntity(CreateClaimDTO dto);
}
