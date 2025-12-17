package com.insurancesystem.Model.Dto;

import com.insurancesystem.Model.Entity.Enums.ProviderType;
import lombok.Data;
import java.util.List;

@Data
public class CreatePriceListDTO {
    private ProviderType providerType;
    private String serviceName;
    private String serviceCode;
    private Double price;
    private String notes;
    private String serviceDetails;

    /**
     * List of specialization IDs from doctor_specialization table that are allowed to use this service
     * If null or empty, the service is available to ALL specializations
     */
    private List<Long> allowedSpecializationIds;
}

