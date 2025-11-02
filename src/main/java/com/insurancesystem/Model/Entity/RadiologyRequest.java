package com.insurancesystem.Model.Entity;

import com.insurancesystem.Model.Entity.Enums.LabRequestStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;
@Entity
@Table(name = "radiology_requests")  // Table for radiology requests
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RadiologyRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)  // استخدم AUTO بدلاً من IDENTITY
    private UUID id;  // UUID لا يحتاج إلى IDENTITY

    @Column(nullable = false)
    private String testName; // نوع الفحص (مثال: صورة أشعة، MRI، إلخ)

    private String notes; // ملاحظات من الطبيب

    private String resultUrl; // رابط أو مسار ملف النتيجة (من المختبر)

    @Enumerated(EnumType.STRING)
    private LabRequestStatus status;  // Status of the request (e.g., PENDING, COMPLETED)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Client doctor;  // The doctor who created the radiology request

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Client member;  // The member (patient) for whom the radiology test is requested

    private Instant createdAt;
    private Instant updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "radiologist_id")
    private Client radiologist;  // The radiologist who will handle the request
}
