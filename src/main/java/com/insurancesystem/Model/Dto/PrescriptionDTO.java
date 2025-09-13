package com.insurancesystem.Model.Dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrescriptionDTO {
    private UUID id;
    private String medicine;
    private String dosage;
    private String instructions;
    private String status;

    private UUID memberId;

    // ✅ جديد
    private String doctorName;
    private String memberName;

    private Instant createdAt;
    private Instant updatedAt;

    // للإحصائيات
    private long total;
    private long pending;
    private long verified;
    private long rejected;
}
