package com.insurancesystem.Model.Entity;

import com.insurancesystem.Model.Entity.Enums.LabRequestStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;
@Entity
@Table(name = "radiology_requests")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RadiologyRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private String notes;
    private String resultUrl;

    @Column(nullable = true)
    private String testName;

    @Enumerated(EnumType.STRING)
    private LabRequestStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Client doctor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Client member;

    // 🆕 ربط الأشعة مع PriceList
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "price_id")
    private PriceList test;

    private Double enteredPrice;
    private Double approvedPrice; // 🆕 السعر المعتمد

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "radiologist_id")
    private Client radiologist;

    private Instant createdAt;
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }
    // 🔥 NEW
    @Column(columnDefinition = "TEXT")
    private String diagnosis;

    @Column(columnDefinition = "TEXT")
    private String treatment;
    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}
