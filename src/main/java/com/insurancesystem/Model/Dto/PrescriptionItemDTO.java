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
    private Boolean noDosage; // Flag to indicate if medicine doesn't need dosage
    private String form; // Form of medicine (Tablet, Syrup, Injection, Cream, Drops)

    // الكميات المحسوبة
    private Integer calculatedQuantity; // الكمية المحسوبة تلقائياً بناءً على الوصفة (يمكن تحديدها مباشرة من المدير الطبي)
    private Integer dispensedQuantity; // الكمية المصروفة من الصيدلي
    private Integer coveredQuantity; // الكمية المشمولة بالتأمين (min of calculated and dispensed)

    // الأسعار
    private Double pharmacistPrice; // السعر الكلي المدخل من الصيدلي
    private Double pharmacistPricePerUnit; // سعر الصيدلي لكل وحدة
    private Double unionPricePerUnit; // سعر النقابة لكل وحدة
    private Double finalPrice; // السعر النهائي للمطالبة = min(unionPricePerUnit, pharmacistPricePerUnit) × coveredQuantity

    // التواريخ
    private Instant expiryDate; // تاريخ انتهاء الدواء
    private Instant createdAt;
}

