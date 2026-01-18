package com.insurancesystem.Model.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "doctor_medicine_assignments",
    uniqueConstraints = @UniqueConstraint(columnNames = {"doctor_id", "medicine_id"}),
    indexes = {
        @Index(name = "idx_dma_doctor", columnList = "doctor_id"),
        @Index(name = "idx_dma_medicine", columnList = "medicine_id"),
        @Index(name = "idx_dma_specialization", columnList = "specialization")
    }
)
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class DoctorMedicineAssignment {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Client doctor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicine_id", nullable = false)
    private MedicinePrice medicine;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_by")
    private Client assignedBy;

    // Specialization-based assignment (e.g., "Cardiology", "General Practice")
    @Column(length = 150)
    private String specialization;

    // Optional restrictions
    @Column(name = "max_daily_prescriptions")
    private Integer maxDailyPrescriptions;

    @Column(name = "max_quantity_per_prescription")
    private Integer maxQuantityPerPrescription;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "assigned_at", nullable = false)
    private Instant assignedAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        assignedAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}
