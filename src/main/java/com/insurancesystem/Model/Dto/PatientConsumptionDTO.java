package com.insurancesystem.Model.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PatientConsumptionDTO {
    private String patientName;
    private Double totalAmount;
}
