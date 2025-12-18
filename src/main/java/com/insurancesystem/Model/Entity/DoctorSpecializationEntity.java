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
}