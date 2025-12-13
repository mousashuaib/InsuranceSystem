package com.insurancesystem.Model.MapStruct;

import com.insurancesystem.Model.Dto.LabRequestDTO;
import com.insurancesystem.Model.Entity.LabRequest;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface LabRequestMapper {

    // ----------------------------
    // 🔹 ENTITY → DTO
    // ----------------------------
    @Mapping(source = "doctor.id", target = "doctorId")
    @Mapping(source = "doctor.fullName", target = "doctorName")

    @Mapping(source = "member.id", target = "memberId")
    @Mapping(source = "member.fullName", target = "memberName")

    @Mapping(source = "labTech.id", target = "labTechId")
    @Mapping(source = "labTech.fullName", target = "labTechName")

    // 🟢 PriceList fields
    @Mapping(source = "test.id", target = "testId")
    @Mapping(source = "test.serviceName", target = "serviceName")
    @Mapping(source = "test.price", target = "unionPrice")
    @Mapping(source = "member.employeeId", target = "employeeId")
    @Mapping(source = "member.universityCardImage", target = "universityCardImage")
    LabRequestDTO toDto(LabRequest request);

    // ----------------------------
    // 🔹 DTO → ENTITY
    // ----------------------------
    // ملاحظة: سيتم حقن doctor, member, test في Service يدويًا
    @Mapping(target = "doctor", ignore = true)
    @Mapping(target = "member", ignore = true)
    @Mapping(target = "labTech", ignore = true)
    @Mapping(target = "test", ignore = true)

    // سيتم ملؤها في الـ service
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    // 🔥 NEW — diagnosis + treatment
    @Mapping(source = "diagnosis", target = "diagnosis")
    @Mapping(source = "treatment", target = "treatment")
    LabRequest toEntity(LabRequestDTO dto);
}
