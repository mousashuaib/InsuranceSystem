package com.insurancesystem.Model.Dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class CreateHealthcareProviderClaimDTO {
    private UUID clientId;
    private String description;
    private Double amount;
    private LocalDate serviceDate;
    private String roleSpecificData; // JSON: medicines, testResults, imaging, etc

    private String diagnosis;         // NEW
    private String treatmentDetails;  // NEW
}

