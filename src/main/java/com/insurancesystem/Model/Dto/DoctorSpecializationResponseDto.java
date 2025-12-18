package com.insurancesystem.Model.Dto;

import lombok.Data;

import java.util.List;

@Data
public class DoctorSpecializationResponseDto {

    private Long id; // Unique identifier for the specialization (server-generated)
    private String displayName; // Name of the specialization
    private double consultationPrice; // Price of the consultation
    private List<String> diagnoses; // Diagnoses related to the specialization
    private List<String> treatmentPlans; // Treatment plans related to the specialization
}
