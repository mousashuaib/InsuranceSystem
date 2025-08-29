package com.insurancesystem.Model.Dto;

import lombok.*;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PoliciesReportDto {
    private long totalMembers;       // عدد الأعضاء الكلي
    private long activeMembers;      // عدد الأعضاء الفعّالين
    private long inactiveMembers;    // عدد الأعضاء غير الفعّالين
    private long activePolicies;     // عدد السياسات النشطة
    private Map<String, Long> membersPerPolicy; // توزيع الأعضاء حسب نوع السياسة
}
