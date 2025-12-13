package com.insurancesystem.Model.Dto;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LabRequestDTO {

    private UUID id;

    private String testName; // اسم الفحص داخل الطلب

    private String notes;

    private String resultUrl;


    private String status;

    private String diagnosis;
    private String treatment;

     private UUID doctorId;
    private String doctorName;

    private UUID memberId;
    private String memberName;
    private String employeeId;
    private String universityCardImage;
    private UUID labTechId;
    private String labTechName;

    // 🟢 الفحص من PriceList
    private UUID testId;
    private String serviceName; // اسم الفحص من PriceList (بدل testName_test)
    private Double unionPrice;  // الآن = price من PriceList

    // 🟢 الأسعار
    private Double enteredPrice;
    private Double approvedPrice;

    // 📊 الإحصائيات
    private long total;
    private long pending;
    private long completed;

    private Instant createdAt;
    private Instant updatedAt;

}
