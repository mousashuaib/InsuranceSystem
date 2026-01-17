package com.insurancesystem.Model.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProviderExpenseDTO {
    private String providerName;
    private Double totalAmount;
}
