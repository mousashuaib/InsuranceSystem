package com.insurancesystem.Model.Dto;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrescriptionItemDTO {

    private UUID id;

    // معلومات الدواء
    private UUID medicineId;
    private String medicineName;
    private String scientificName;
    private Integer medicineQuantity; // عدد الحبات في العلبة
    private Double unionPrice; // سعر النقابة

    // معلومات الجرعة
    private Integer dosage; // ✅ صار Integer
    private Integer timesPerDay; // عدد المرات في اليوم
    private Integer duration; // المدة بالأيام (NEW)

    // الأسعار
    private Double pharmacistPrice; // سعر الصيدلي (يدخله الصيدلي)
    private Double finalPrice; // السعر المعتمد = min(pharmacistPrice, unionPrice)

    // التواريخ
    private Instant expiryDate; // تاريخ انتهاء الدواء
    private Instant createdAt;
}

