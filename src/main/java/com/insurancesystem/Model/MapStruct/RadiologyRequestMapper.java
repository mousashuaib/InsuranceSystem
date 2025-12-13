package com.insurancesystem.Model.MapStruct;

import com.insurancesystem.Model.Dto.RadiologyRequestDTO;
import com.insurancesystem.Model.Entity.RadiologyRequest;
import org.mapstruct.*;
@Mapper(componentModel = "spring")
public interface RadiologyRequestMapper {

    // Entity → DTO
    @Mapping(source = "doctor.id", target = "doctorId")
    @Mapping(source = "doctor.fullName", target = "doctorName")
    @Mapping(source = "member.id", target = "memberId")
    @Mapping(source = "member.fullName", target = "memberName")
    @Mapping(source = "radiologist.id", target = "radiologistId")
    @Mapping(source = "radiologist.fullName", target = "radiologistName")

    // 🆕 من PriceList
    @Mapping(source = "test.id", target = "testId")
    @Mapping(source = "test.serviceName", target = "testName")
    @Mapping(source = "test.price", target = "approvedPrice")  // default union price
    @Mapping(source = "member.employeeId", target = "employeeId")
    @Mapping(source = "member.universityCardImage", target = "universityCardImage")
    RadiologyRequestDTO toDto(RadiologyRequest request);

    // DTO → Entity
    @Mapping(source = "doctorId", target = "doctor.id")
    @Mapping(source = "memberId", target = "member.id")
    @Mapping(source = "radiologistId", target = "radiologist.id")
    @Mapping(source = "testId", target = "test.id")

    @Mapping(target = "doctor", ignore = true)
    @Mapping(target = "member", ignore = true)
    @Mapping(target = "radiologist", ignore = true)
    @Mapping(target = "test", ignore = true)   // يتم Inject في Service

    RadiologyRequest toEntity(RadiologyRequestDTO dto);
}
