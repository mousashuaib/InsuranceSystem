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
    private String serviceDetails;

    /**
     * List of specialization IDs from doctor_specialization table that are allowed to use this service
     * If null or empty, the service is available to ALL specializations
     */
    private List<Long> allowedSpecializationIds;

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

