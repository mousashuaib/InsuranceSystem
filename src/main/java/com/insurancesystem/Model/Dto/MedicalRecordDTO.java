package com.insurancesystem.Model.Dto;

import lombok.*;
import java.time.Instant;
import java.util.UUID;

import lombok.*;
import java.time.Instant;
import java.util.UUID;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MedicalRecordDTO {
    private UUID id;
    private String diagnosis;
    private String treatment;
    private String notes;

    // 🟢 المريض
    private UUID memberId;
    private String memberName;

    // 🟢 الدكتور
    private UUID doctorId;
    private String doctorName;

    private Instant createdAt;
    private Instant updatedAt;
}
