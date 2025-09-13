package com.insurancesystem.Model.MapStruct;

import com.insurancesystem.Model.Dto.MedicalRecordDTO;
import com.insurancesystem.Model.Entity.MedicalRecord;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface MedicalRecordMapper {

    // ✅ نرجع معلومات المريض + الدكتور
    @Mapping(source = "member.id", target = "memberId")
    @Mapping(source = "member.fullName", target = "memberName")
    @Mapping(source = "doctor.id", target = "doctorId")
    @Mapping(source = "doctor.fullName", target = "doctorName")
    MedicalRecordDTO toDto(MedicalRecord record);

    // ✅ بالـ create/update ما منجيب doctor/member من DTO → بنضيفهم من Service
    @Mapping(source = "memberId", target = "member.id")
    @Mapping(target = "doctor", ignore = true) // الدكتور ينعطى من الـ Service
    @Mapping(target = "member", ignore = true) // المريض ينعطى من الـ Service
    MedicalRecord toEntity(MedicalRecordDTO dto);
}
