package com.insurancesystem.Model.Dto;

import lombok.Data;
@Data
public class UpdatePriceListDTO {

    private String serviceName;
    private String serviceCode;
    private Double price;
    private String notes;
    private Boolean active;
    private String serviceDetails;  // JSON
}
