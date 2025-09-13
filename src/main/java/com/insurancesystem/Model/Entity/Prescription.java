package com.insurancesystem.Model.Entity;

import com.insurancesystem.Model.Entity.Enums.PrescriptionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "prescriptions")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Prescription {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String medicine;

    private String dosage;

    private String instructions;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PrescriptionStatus status;

    // ✅ الدكتور اللي أنشأ الوصفة
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Client doctor;

    // ✅ المريض (العضو) اللي إله الوصفة
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Client member;

    // ✅ الصيدلي اللي تعامل مع الوصفة (Verify / Reject)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pharmacist_id")
    private Client pharmacist;

    private Instant createdAt;
    private Instant updatedAt;
}
