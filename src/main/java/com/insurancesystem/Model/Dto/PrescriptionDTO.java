package com.insurancesystem.Model.Dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrescriptionDTO {
    private UUID id;

    // الحالة
    private String status;
    private String diagnosis;
    private String treatment;

    // الأشخاص المعنيين
    private UUID memberId;
    private String memberName;
    private String employeeId;
    private String universityCardImage;
    private String doctorName;

    private UUID pharmacistId;
    private String pharmacistName;

    // 🆕 قائمة الأدوية في الوصفة
    private List<PrescriptionItemDTO> items;

    // 💰 المجموع الكلي
    private Double totalPrice;

    private String notes; // ملاحظات

    private Instant createdAt;
    private Instant updatedAt;

    // للإحصائيات فقط (stats endpoints)
    private Long total;
    private Long pending;
    private Long verified;
    private Long rejected;
}