package com.insurancesystem.Model.Entity;

import com.insurancesystem.Model.Entity.Enums.ClaimStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "healthcare_provider_claims")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HealthcareProviderClaim {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "provider_id")
    private Client healthcareProvider; // الطبيب/الصيدلي/فني المختبر/فني الأشعة

    @Column(columnDefinition = "TEXT")
    private String diagnosis;

    @Column(columnDefinition = "TEXT")
    private String treatmentDetails;

    @Column(name = "client_id")
    private UUID clientId; // معرف المريض

    @Column(name = "client_name")
    private String clientName; // اسم المريض

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description; // وصف الخدمة المقدمة

    @Column(nullable = false)
    private Double amount; // قيمة الخدمة

    @Column(nullable = false)
    private LocalDate serviceDate; // تاريخ تقديم الخدمة

    @Column(columnDefinition = "TEXT")
    private String roleSpecificData; // بيانات إضافية حسب الدور (JSON)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ClaimStatus status;


    @Column(name = "invoice_image_path")
    private String invoiceImagePath; // مسار صورة الفاتورة/الوثيقة

    private Instant submittedAt;

    private Instant approvedAt;

    private Instant rejectedAt;

    @Column(columnDefinition = "text")
    private String rejectionReason;

    @PrePersist
    void onCreate() {
        this.submittedAt = Instant.now();
        if (this.status == null) {
            this.status = ClaimStatus.PENDING_MEDICAL;
        }

    }
    // === Medical Admin Reviewer Info ===
    @Column(name = "medical_reviewer_id")
    private UUID medicalReviewerId;

    @Column(name = "medical_reviewer_name")
    private String medicalReviewerName;

    @Column(name = "medical_reviewed_at")
    private Instant medicalReviewedAt;

}

