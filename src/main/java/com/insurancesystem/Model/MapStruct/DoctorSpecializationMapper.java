package com.insurancesystem.Model.MapStruct;

import com.insurancesystem.Model.Dto.DoctorSpecializationRequestDto;
import com.insurancesystem.Model.Dto.DoctorSpecializationResponseDto;
import com.insurancesystem.Model.Entity.DoctorSpecializationEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DoctorSpecializationMapper {

    // Map from Entity to ResponseDto
    DoctorSpecializationResponseDto toResponseDto(DoctorSpecializationEntity entity);

    // Map from RequestDto to Entity
    DoctorSpecializationEntity toEntity(DoctorSpecializationRequestDto requestDto);
}
