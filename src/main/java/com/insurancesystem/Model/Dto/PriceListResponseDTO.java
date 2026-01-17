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
    /**
     * Quantity of medicine in the package
     * For Tablet: number of pills in the package
     * For Syrup: volume in ml
     * For Injection: number of injections in the package
     * For Cream: weight in grams
     * For Drops: volume in ml
     */
    private Integer quantity;
    private String notes;
    private Map<String, Object> serviceDetails; // Already converted from JSON string
    private boolean active;

    /**
     * List of allowed specializations for this service
     * Each item is a Map with "id" and "displayName"
     * If null or empty, the service is available to ALL specializations
     */
    private List<Map<String, Object>> allowedSpecializations;

    /**
     * List of allowed genders for this service (e.g., "MALE", "FEMALE")
     * If null or empty, the service is available to ALL genders
     */
    private List<String> allowedGenders;

    /**
     * Minimum age required to use this service
     * If null, there is no minimum age restriction
     */
    private Integer minAge;

    /**
     * Maximum age allowed to use this service
     * If null, there is no maximum age restriction
     */
    private Integer maxAge;
}
