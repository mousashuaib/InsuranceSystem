package com.insurancesystem.Model.Entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Data
@Entity
@Table(name = "doctor_specialization")
public class DoctorSpecializationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    @Column(name = "consultation_price", nullable = false)
    private double consultationPrice;

    // Store diagnoses as an array or JSON column
    @Column(name = "diagnoses", columnDefinition = "TEXT[]")
    private List<String> diagnoses;

    // Store treatment plans as an array or JSON column
    @Column(name = "treatment_plans", columnDefinition = "TEXT[]")
    private List<String> treatmentPlans;

    /**
     * List of allowed genders for this specialization (e.g., "MALE", "FEMALE")
     * If null or empty, the specialization can treat ALL genders
     * If it contains genders, only those genders can be treated by this specialization
     */
    @ElementCollection
    @CollectionTable(
            name = "doctor_specialization_allowed_genders",
            joinColumns = @JoinColumn(name = "specialization_id")
    )
    @Column(name = "gender")
    private List<String> allowedGenders;

    /**
     * Minimum age that this specialization can treat
     * If null, there is no minimum age restriction
     */
    @Column(name = "min_age")
    private Integer minAge;

    /**
     * Maximum age that this specialization can treat
     * If null, there is no maximum age restriction
     */
    @Column(name = "max_age")
    private Integer maxAge;

    /**
     * Gender restriction for this specialization (e.g., "MALE", "FEMALE", "ALL")
     * If null or "ALL", the specialization can treat all genders
     * If "MALE", only male patients can be treated
     * If "FEMALE", only female patients can be treated
     */
    @Column(name = "gender")
    private String gender;
}