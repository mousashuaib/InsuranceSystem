package com.insurancesystem.Model.Dto;

import com.insurancesystem.Model.Entity.Enums.SearchProfileType;
import com.insurancesystem.Model.Entity.Enums.ProfileStatus;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchProfileDto {

    private UUID id;
    private String name;
    private SearchProfileType type;
    private String address;
    private Double locationLat;
    private Double locationLng;
    private String contactInfo; // ✅ رقم الدكتور/الصيدلية/المختبر
    private String description;
    private String ownerName; // اسم المالك (doctor/pharmacist/lab)
    private ProfileStatus status; // PENDING, APPROVED, REJECTED
    private String rejectionReason; // ✅ سبب الرفض

    // ✅ New Document Fields
    private String medicalLicense;       // رخصة مزاولة المهنة (Required)
    private String universityDegree;     // الشهادة الجامعية (Required)
    private String clinicRegistration;   // تسجيل العيادة (Optional)
    private String idOrPassportCopy;     // نسخة الهوية / الجواز (Required)
}
