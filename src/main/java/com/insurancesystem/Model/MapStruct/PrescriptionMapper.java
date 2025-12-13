package com.insurancesystem.Model.MapStruct;

import com.insurancesystem.Model.Dto.PrescriptionDTO;
import com.insurancesystem.Model.Entity.Prescription;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = {PrescriptionItemMapper.class})
public interface PrescriptionMapper {

    @Mapping(source = "pharmacist.fullName", target = "pharmacistName")
    @Mapping(source = "member.id", target = "memberId")
    @Mapping(source = "doctor.fullName", target = "doctorName")
    @Mapping(source = "member.fullName", target = "memberName")
    @Mapping(source = "items", target = "items")
    @Mapping(source = "totalPrice", target = "totalPrice")

    // 🔥 NEW — diagnosis + treatment
    @Mapping(source = "diagnosis", target = "diagnosis")
    @Mapping(source = "treatment", target = "treatment")
    @Mapping(source = "member.employeeId", target = "employeeId")
    @Mapping(source = "member.universityCardImage", target = "universityCardImage")
    PrescriptionDTO toDto(Prescription entity);

    @Mapping(target = "pharmacist", ignore = true)
    @Mapping(source = "memberId", target = "member.id")
    @Mapping(target = "doctor", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "items", ignore = true)
    @Mapping(target = "totalPrice", ignore = true)

    // 🔥 NEW — diagnosis + treatment
    @Mapping(source = "diagnosis", target = "diagnosis")
    @Mapping(source = "treatment", target = "treatment")

    Prescription toEntity(PrescriptionDTO dto);
}