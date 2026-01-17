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
    private List<String> universityCardImages;
    private String universityCardImage; // First university card image (for easier access)
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

    // 🆕 Family Member Information (if prescription is for a family member)
    private Boolean isFamilyMember;
    private String familyMemberName;
    private String familyMemberRelation;
    private String familyMemberInsuranceNumber;
    private String familyMemberAge;
    private String familyMemberGender;

    // 🆕 Main Client Information (age and gender)
    private String memberAge;
    private String memberGender;
    private String memberNationalId; // National ID of the main client/patient

    // 🆕 Family Member National ID
    private String familyMemberNationalId; // National ID of the family member

    // 🆕 Chronic Disease Prescription Flag
    private Boolean isChronic;
}

