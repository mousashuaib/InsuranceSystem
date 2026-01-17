package com.insurancesystem.Model.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(
    name = "visits",
    indexes = {
        @Index(name = "idx_visit_patient_date", columnList = "patient_id, visit_date"),
        @Index(name = "idx_visit_doctor_date", columnList = "doctor_id, visit_date"),
        @Index(name = "idx_visit_patient_year", columnList = "patient_id, visit_year"),
        @Index(name = "idx_visit_family_member_date", columnList = "family_member_id, visit_date")
    }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Visit {

    @Id
    @GeneratedValue
    private UUID id;

    // Patient can be either Client (employee) or FamilyMember
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = true)
    private Client patient; // Employee/Client

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_member_id", nullable = true)
    private FamilyMember familyMember; // Family member

    // Doctor (Client with DOCTOR role)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Client doctor;

    // Doctor's specialization at the time of visit
    @Column(name = "doctor_specialization", nullable = false, length = 150)
    private String doctorSpecialization;

    // Visit date
    @Column(name = "visit_date", nullable = false)
    private LocalDate visitDate;

    // Visit type: NORMAL (counted) or FOLLOW_UP (not counted)
    @Enumerated(EnumType.STRING)
    @Column(name = "visit_type", nullable = false, length = 20)
    private VisitType visitType;

    // Year for tracking yearly visit count
    @Column(name = "visit_year", nullable = false)
    private Integer visitYear;

    // Reference to previous visit if this is a follow-up
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "previous_visit_id", nullable = true)
    private Visit previousVisit;

    // Notes or diagnosis
    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.visitYear == null && this.visitDate != null) {
            this.visitYear = this.visitDate.getYear();
        }
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }

    // Helper method to get the patient identifier (for tracking)
    public UUID getPatientIdentifier() {
        if (patient != null) {
            return patient.getId();
        } else if (familyMember != null) {
            return familyMember.getId();
        }
        return null;
    }

    public enum VisitType {
        NORMAL,      // Counted towards yearly limit
        FOLLOW_UP    // Not counted towards yearly limit
    }
}

