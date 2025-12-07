package com.insurancesystem.Model.Entity;

import com.insurancesystem.Model.Entity.Enums.ProviderType;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;
@Entity
@Table(name = "price_list")
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class PriceList {

    @Id
    @GeneratedValue
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProviderType providerType;
    // PHARMACY / LAB / RADIOLOGY / DOCTOR

    @Column(nullable = false)
    private String serviceName;

    private String serviceCode;

    @Column(nullable = false)
    private Double price;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(columnDefinition = "jsonb")
    private String serviceDetails;

    private boolean active = true;

    private Instant createdAt;
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}
