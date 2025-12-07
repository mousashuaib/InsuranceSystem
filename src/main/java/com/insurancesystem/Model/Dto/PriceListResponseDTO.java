package com.insurancesystem.Model.Dto;

import com.insurancesystem.Model.Entity.Enums.ProviderType;
import lombok.Data;

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
    private boolean active;

    private Map<String, Object> serviceDetails; // <-- مهم جداً
}
