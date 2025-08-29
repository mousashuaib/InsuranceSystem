package com.insurancesystem.Model.MapStruct;

import com.insurancesystem.Model.Dto.PrescriptionDTO;
import com.insurancesystem.Model.Entity.Prescription;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface PrescriptionMapper {

    @Mapping(source = "member.id", target = "memberId")
    @Mapping(source = "status", target = "status")
    PrescriptionDTO toDto(Prescription entity);

    @Mapping(source = "memberId", target = "member.id")
    @Mapping(target = "doctor", ignore = true) // doctor يجي من الـ Service
    @Mapping(target = "status", ignore = true) // default → PENDING
    Prescription toEntity(PrescriptionDTO dto);
}
