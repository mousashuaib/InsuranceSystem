package com.insurancesystem.Model.Dto;

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
    private UUID memberId;
    private Instant createdAt;
    private Instant updatedAt;
}
