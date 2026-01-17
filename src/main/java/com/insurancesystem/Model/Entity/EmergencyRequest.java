package com.insurancesystem.Model.Entity;

import com.insurancesystem.Model.Entity.Enums.EmergencyStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "emergency_requests")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmergencyRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // المريض الذي تتعلق به الطوارئ
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Client member;

    // الدكتور الذي أنشأ الطلب
    @Column(name = "doctor_id", nullable = false)
    private UUID doctorId;

    // عضو العائلة إذا كان الطلب لعضو عائلة (nullable - null إذا كان الطلب للعميل الرئيسي)
    @Column(name = "family_member_id", nullable = true)
    private UUID familyMemberId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String location;

    @Column(nullable = false)
    private String contactPhone;

    @Column(nullable = false)
    private LocalDate incidentDate;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmergencyStatus status;

    @Column(nullable = false)
    private Instant submittedAt;

    private Instant approvedAt;

    private Instant rejectedAt;

    @Column(columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}

