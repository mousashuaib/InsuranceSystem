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
    private String testName; // نوع الفحص (دم، صورة أشعة..)

    private String notes; // ملاحظات من الدكتور

    private String resultUrl; // رابط أو مسار ملف النتيجة (من المختبر)

    @Enumerated(EnumType.STRING)
    private LabRequestStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Client doctor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Client member;

    private Instant createdAt;
    private Instant updatedAt;
}
