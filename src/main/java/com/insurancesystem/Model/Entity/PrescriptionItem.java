package com.insurancesystem.Model.Entity;

import jakarta.persistence.*;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "prescription_items")
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class PrescriptionItem {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prescription_id", nullable = false)
    private Prescription prescription;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "price_list_id", nullable = true) // Temporarily allow NULL values
    private PriceList priceList;

    private Integer dosage;
    private Integer timesPerDay;
    private Integer duration; // المدة بالأيام (NEW)

    /**
     * Calculated required quantity based on prescription (dosage × timesPerDay × duration)
     * This is automatically calculated when prescription is created
     */
    private Integer calculatedQuantity;

    /**
     * Actual quantity dispensed by pharmacist (can be more than calculated if multiple packages)
     */
    private Integer dispensedQuantity;

    /**
     * Quantity covered by insurance (min of calculatedQuantity and dispensedQuantity)
     * Used for claim calculation
     */
    private Integer coveredQuantity;

    /**
     * Drug form: Tablet, Syrup, Injection, Cream, Drops
     * Extracted from PriceList.serviceDetails.form
     */
    @Column(name = "drug_form")
    private String drugForm;

    /**
     * Price per unit (calculated from union price / package quantity)
     * Used for claim calculation
     */
    private Double unionPricePerUnit;

    /**
     * Pharmacy price per unit (calculated from pharmacist price / package quantity)
     * Used for claim calculation
     */
    private Double pharmacistPricePerUnit;

    private Double pharmacistPrice; // Total price entered by pharmacist
    private Double finalPrice; // Final claim amount = min(unionPricePerUnit, pharmacistPricePerUnit) × coveredQuantity

    private Instant expiryDate;
    private Instant createdAt;
    private Instant updatedAt;
}

