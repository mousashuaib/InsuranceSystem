package com.insurancesystem.Model.Dto;

import com.insurancesystem.Model.Entity.Enums.EnrollmentStatus;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CreateMemberEnrollmentDTO {

    @NotNull
    private UUID userId;

    @NotNull
    private UUID policyId;

    @NotNull
    private LocalDate startDate;

    private LocalDate endDate;

    @NotNull
    private EnrollmentStatus status; // غالبًا ACTIVE عند الإنشاء
}
