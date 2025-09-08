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

    private UUID memberId;
    private UUID doctorId;

    private long total;
    private long pending;   // طلبات لسه ما إلها نتيجة
    private long completed; // طلبات مرفوع إلها نتيجة

    private Instant createdAt;
    private Instant updatedAt;
}
