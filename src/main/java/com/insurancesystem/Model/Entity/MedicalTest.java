package com.insurancesystem.Model.Entity;

import com.insurancesystem.Model.Entity.Enums.CoverageStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "medical_tests")
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class MedicalTest {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String testName;

    @Column(nullable = false)
    private String category; // LAB or RADIOLOGY

    @Column(precision = 12, scale = 2)
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
