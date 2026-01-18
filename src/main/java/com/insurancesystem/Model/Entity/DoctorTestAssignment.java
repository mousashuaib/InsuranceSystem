package com.insurancesystem.Model.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "doctor_test_assignments",
    uniqueConstraints = @UniqueConstraint(columnNames = {"doctor_id", "test_id"}),
    indexes = {
        @Index(name = "idx_dta_doctor", columnList = "doctor_id"),
        @Index(name = "idx_dta_test", columnList = "test_id"),
        @Index(name = "idx_dta_specialization", columnList = "specialization"),
        @Index(name = "idx_dta_test_type", columnList = "test_type")
    }
)
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class DoctorTestAssignment {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Client doctor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_id", nullable = false)
    private MedicalTest test;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_by")
    private Client assignedBy;

    // Test type: LAB or RADIOLOGY
    @Column(name = "test_type", length = 20, nullable = false)
    private String testType;

    // Specialization-based assignment (e.g., "Cardiology", "General Practice")
    @Column(length = 150)
    private String specialization;

    // Optional restrictions
    @Column(name = "max_daily_requests")
    private Integer maxDailyRequests;

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
