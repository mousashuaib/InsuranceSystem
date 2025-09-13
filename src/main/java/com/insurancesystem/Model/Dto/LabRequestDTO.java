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
    private String testName;
    private String notes;
    private String resultUrl;
    private String status;

    private UUID doctorId;
    private String doctorName;

    private UUID memberId;
    private String memberName;

    // 🟢 الجديد (عشان تحدد مين الـ Lab Tech اللي رفع النتيجة)
    private UUID labTechId;
    private String labTechName;

    private long total;
    private long pending;
    private long completed;

    private Instant createdAt;
    private Instant updatedAt;
}
