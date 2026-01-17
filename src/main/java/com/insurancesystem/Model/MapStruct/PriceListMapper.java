package com.insurancesystem.Model.MapStruct;

import com.insurancesystem.Model.Dto.PriceListResponseDTO;
import com.insurancesystem.Model.Entity.PriceList;
import com.insurancesystem.Model.Entity.DoctorSpecializationEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;
import com.fasterxml.jackson.databind.ObjectMapper;

@Mapper(componentModel = "spring")
public interface PriceListMapper {

    /**
     * Maps PriceList entity to PriceListResponseDTO
     * Note: allowedGenders, minAge, maxAge, and quantity are automatically mapped by MapStruct
     * since they have the same names and compatible types in both entity and DTO
     */
    @Mapping(target = "serviceDetails", source = "serviceDetails", qualifiedByName = "convertJson")
    @Mapping(target = "allowedSpecializations", source = "allowedSpecializations", qualifiedByName = "mapSpecializations")
    PriceListResponseDTO toDto(PriceList entity);

    /**
     * Convert JSON string to Map (for serviceDetails)
     */
    @Named("convertJson")
    default Map<String, Object> convertJson(String json) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(json, Map.class);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Map List<DoctorSpecializationEntity> to List of Map objects with id and displayName
     * This ensures the frontend receives the specialization data
     */
    @Named("mapSpecializations")
    default List<Map<String, Object>> mapSpecializations(List<DoctorSpecializationEntity> specializations) {
        if (specializations == null || specializations.isEmpty()) {
            return null;
        }
        return specializations.stream()
                .map(spec -> {
                    Map<String, Object> specMap = new HashMap<>();
                    specMap.put("id", spec.getId());
                    specMap.put("displayName", spec.getDisplayName());
                    return specMap;
                })
                .collect(Collectors.toList());
    }
}
