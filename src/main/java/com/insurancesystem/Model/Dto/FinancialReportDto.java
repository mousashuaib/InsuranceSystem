package com.insurancesystem.Model.Dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinancialReportDto {

    private double totalExpenses; // مجموع المصاريف (approved claims)

    private List<TopProvider> topProviders; // أعلى 5 مزودين

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TopProvider {
        private java.util.UUID providerId; // معرف المزود
        private String providerName; // اسم الدكتور / الصيدلية / المختبر
        private double totalAmount;  // مجموع المبالغ المصروفة له
        private String providerType; // نوع المزود: DOCTOR, PHARMACIST, LAB_TECH, RADIOLOGIST
        private int claimCount; // عدد المطالبات
    }
}
