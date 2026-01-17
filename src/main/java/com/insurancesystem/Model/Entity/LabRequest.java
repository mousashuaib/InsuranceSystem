package com.insurancesystem.Model.Entity;

import com.insurancesystem.Model.Entity.Enums.LabRequestStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "lab_requests")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LabRequest {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String testName; // نوع الفحص

    private String notes; // ملاحظات من الدكتور

    private String resultUrl; // رابط أو مسار ملف النتيجة

    @Enumerated(EnumType.STRING)
    private LabRequestStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Client doctor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Client member;

    // 🟢 علاقة مع جدول الفحصوات
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "price_id")
    private PriceList test;

    private Double enteredPrice; // السعر الذي دخله اللاب تِك
    private Double approvedPrice; // السعر المعتمد (الأقل من النقابي)
    // 🔥 NEW
    @Column(columnDefinition = "TEXT")
    private String diagnosis;

    @Column(columnDefinition = "TEXT")
    private String treatment;

    private Instant createdAt;
    private Instant updatedAt;

    @ManyToOne
    @JoinColumn(name = "lab_tech_id")
    private Client labTech;

}
