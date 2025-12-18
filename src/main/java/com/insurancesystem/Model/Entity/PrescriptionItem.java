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

    private Double pharmacistPrice;
    private Double finalPrice;

    private Instant expiryDate;

    private Instant createdAt;
    private Instant updatedAt;
}
