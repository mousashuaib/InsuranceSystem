package com.insurancesystem.Model.Dto;

import com.insurancesystem.Model.Entity.Enums.ProviderType;
import lombok.Data;
import java.util.UUID;
@Data
public class CreatePriceListDTO {

    private ProviderType providerType; // no provider ID

    private String serviceName;
    private String serviceCode;

    private Double price;
    private String notes;

    private String serviceDetails;
}
