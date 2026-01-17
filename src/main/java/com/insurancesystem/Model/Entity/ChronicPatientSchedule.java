package com.insurancesystem.Model.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "chronic_patient_schedules")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChronicPatientSchedule {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Client patient;

    @Column(name = "schedule_type", nullable = false, length = 20)
    private String scheduleType; // PRESCRIPTION, LAB, RADIOLOGY, CLAIM

    @Column(name = "medication_name", length = 200)
    private String medicationName;

    @Column(name = "medication_quantity")
    private Integer medicationQuantity; // كمية الدواء المطلوبة

    @Column(name = "lab_test_name", length = 200)
    private String labTestName;

    @Column(name = "radiology_test_name", length = 200)
    private String radiologyTestName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "amount")
    private Double amount;

    @Column(name = "interval_months", nullable = false)
    private Integer intervalMonths; // كل كم شهر

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "last_sent_at")
    private Instant lastSentAt;

    @Column(name = "next_send_date")
    private LocalDate nextSendDate;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.nextSendDate == null) {
            this.nextSendDate = LocalDate.now().plusMonths(this.intervalMonths);
        }
    }

    @PreUpdate
    void preUpdate() {
        this.updatedAt = Instant.now();
    }
}

