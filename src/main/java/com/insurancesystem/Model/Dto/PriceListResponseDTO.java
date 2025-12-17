package com.insurancesystem.Model.Dto;

import com.insurancesystem.Model.Entity.Enums.ProviderType;
import lombok.Data;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
public class PriceListResponseDTO {
    private UUID id;
    private ProviderType providerType;
    private String serviceName;
    private String serviceCode;
    private Double price;
    private String notes;
    private Map<String, Object> serviceDetails; // Already converted from JSON string
    private boolean active;

    /**
     * List of allowed specializations for this service
     * Each item is a Map with "id" and "displayName"
     * If null or empty, the service is available to ALL specializations
     */
    private List<Map<String, Object>> allowedSpecializations;
}
