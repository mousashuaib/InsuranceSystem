package com.insurancesystem.Model.Dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class CreateClaimDTO {
    private UUID policyId;
    private String description;
    private String diagnosis;
    private String treatmentDetails;
    private Double amount;
    private LocalDate serviceDate;
    private String providerName;
    private String doctorName;
}
