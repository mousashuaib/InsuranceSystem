package com.insurancesystem.Model.MapStruct;

import com.insurancesystem.Model.Dto.MedicalRecordDTO;
import com.insurancesystem.Model.Entity.MedicalRecord;
import org.mapstruct.*;
@Mapper(componentModel = "spring")
public interface MedicalRecordMapper {

    @Mapping(source = "member.id", target = "memberId")
    MedicalRecordDTO toDto(MedicalRecord record);

    @Mapping(source = "memberId", target = "member.id")
    @Mapping(target = "doctor", ignore = true) // الدكتور رح نضيفه من الـ Service مش من DTO
    MedicalRecord toEntity(MedicalRecordDTO dto);
}
