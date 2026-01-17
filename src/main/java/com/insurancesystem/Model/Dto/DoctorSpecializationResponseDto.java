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

    /**
     * List of allowed genders for this specialization (e.g., "MALE", "FEMALE")
     * If null or empty, the specialization can treat ALL genders
     */
    private List<String> allowedGenders;

    /**
     * Minimum age that this specialization can treat
     * If null, there is no minimum age restriction
     */
    private Integer minAge;

    /**
     * Maximum age that this specialization can treat
     * If null, there is no maximum age restriction
     */
    private Integer maxAge;

    /**
     * Gender restriction for this specialization (e.g., "MALE", "FEMALE", "ALL")
     * If null or "ALL", the specialization can treat all genders
     */
    private String gender;
}
