package com.insurancesystem.Model.Dto;

import lombok.Data;

import java.util.List;

@Data
public class UpdatePriceListDTO {

    private String serviceName;
    private String serviceCode;
    private Double price;
    private String notes;
    private Boolean active;
    private String serviceDetails;  // JSON
    private List<Long> allowedSpecializationIds;
}
