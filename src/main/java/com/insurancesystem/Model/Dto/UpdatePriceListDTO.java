package com.insurancesystem.Model.Dto;

import lombok.Data;

import java.util.List;

@Data
public class UpdatePriceListDTO {

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
    private Boolean active;
    private String serviceDetails;  // JSON
    private List<Long> allowedSpecializationIds;

    /**
     * List of allowed genders for this service (e.g., "MALE", "FEMALE")
     * If null, the field is not updated
     * If empty list, restrictions are cleared (available to all)
     * If contains genders, only those genders can use the service
     */
    private List<String> allowedGenders;

    /**
     * Minimum age required to use this service
     * If null, the field is not updated
     */
    private Integer minAge;

    /**
     * Maximum age allowed to use this service
     * If null, the field is not updated
     */
    private Integer maxAge;
}
