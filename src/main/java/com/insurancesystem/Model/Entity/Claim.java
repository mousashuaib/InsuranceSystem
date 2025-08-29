package com.insurancesystem.Model.Entity;

import com.insurancesystem.Model.Entity.Enums.ClaimStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "claims")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Claim {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "member_id")
    private Client member; // صاحب المطالبة

    @ManyToOne(optional = false)
    @JoinColumn(name = "policy_id")
    private Policy policy; // البوليصة المرتبطة بالمطالبة

    @Column(nullable = false)
    private String description; // وصف المطالبة

    private String diagnosis; // التشخيص

    private String treatmentDetails; // تفاصيل العلاج

    @Column(nullable = false)
    private Double amount; // قيمة المطالبة

    @Column(nullable = false)
    private LocalDate serviceDate; // تاريخ تقديم الخدمة الطبية

    private String providerName; // اسم المستشفى أو العيادة

    private String doctorName; // اسم الطبيب

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ClaimStatus status;

    @Column(name = "invoice_image_path")
    private String invoiceImagePath; // مسار صورة الفاتورة

    private Instant submittedAt;
    private Instant approvedAt;
    private Instant rejectedAt;

    @Column(columnDefinition = "text")
    private String rejectionReason;

    @PrePersist
    void onCreate() {
        this.submittedAt = Instant.now();
        if (this.status == null) {
            this.status = ClaimStatus.PENDING;
        }
    }
}
