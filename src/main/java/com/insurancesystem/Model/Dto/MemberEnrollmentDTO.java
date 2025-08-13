package com.insurancesystem.Model.Dto;

import com.insurancesystem.Model.Entity.Enums.EnrollmentStatus;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class MemberEnrollmentDTO {
    private UUID id;
    private UUID userId;
    private UUID policyId;
    private LocalDate startDate;
    private LocalDate endDate;
    private EnrollmentStatus status;
}
