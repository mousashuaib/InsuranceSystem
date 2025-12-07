package com.insurancesystem.Model.MapStruct;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.insurancesystem.Model.Dto.PriceListResponseDTO;
import com.insurancesystem.Model.Entity.PriceList;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Map;

@Mapper(componentModel = "spring")
public interface PriceListMapper {

    @Mapping(target = "serviceDetails", expression = "java(convertJson(entity.getServiceDetails()))")
    PriceListResponseDTO toDto(PriceList entity);

    default Map<String, Object> convertJson(String json) {
        if (json == null) return null;
        try {
            return new ObjectMapper().readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            return null;
        }
    }
}
