package com.insurancesystem.Model.Entity;

import com.insurancesystem.Model.Entity.Enums.CoverageStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "medicine_prices")
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class MedicinePrice {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String drugName;

    @Column(columnDefinition = "TEXT")
    private String composition;

    @Column(columnDefinition = "TEXT")
    private String genericName;

    private String type;

    private String unit;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private CoverageStatus coverageStatus = CoverageStatus.COVERED;

    @Builder.Default
    private Integer coveragePercentage = 100; // Percentage of coverage (0-100) when status is COVERED

    @Builder.Default
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
