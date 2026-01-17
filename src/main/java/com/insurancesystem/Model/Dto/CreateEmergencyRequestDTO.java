package com.insurancesystem.Model.Dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class CreateEmergencyRequestDTO {

    @NotBlank(message = "Client name is required")
    private String clientName;  // اسم المريض

    private UUID memberId;  // ID للعميل الرئيسي أو عضو العائلة
    private UUID familyMemberId;  // ID لعضو العائلة (إذا كان الطلب لعضو عائلة)
    private Boolean isFamilyMember;  // هل الطلب لعضو عائلة

    @NotBlank(message = "Description is required")
    private String description;  // وصف الحالة الطارئة

    @NotBlank(message = "Location is required")
    private String location;  // الموقع

    @NotBlank(message = "Contact phone is required")
    private String contactPhone;  // رقم التواصل

    @NotNull(message = "Incident date is required")
    private LocalDate incidentDate;  // تاريخ الحادثة

    private String notes;  // ملاحظات (اختياري)
}