package com.insurancesystem.Model.Dto;

import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PrescriptionDTO {
    private UUID id;
    private String medicine;
    private String dosage;
    private String instructions;
    private String status; // String for simplicity
    private UUID memberId;
    private Instant createdAt;
    private Instant updatedAt;
}
