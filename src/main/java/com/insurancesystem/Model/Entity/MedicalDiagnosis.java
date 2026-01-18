package com.insurancesystem.Model.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "medical_diagnoses")
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class MedicalDiagnosis {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String englishName;

    @Column(nullable = false)
    private String arabicName;

    @Column(columnDefinition = "TEXT")
    private String description;

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
