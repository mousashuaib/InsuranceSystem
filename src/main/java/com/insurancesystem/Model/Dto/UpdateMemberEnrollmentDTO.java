package com.insurancesystem.Model.Dto;

import com.insurancesystem.Model.Entity.Enums.EnrollmentStatus;
import lombok.*;

import java.time.LocalDate;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class UpdateMemberEnrollmentDTO {
    private LocalDate startDate;
    private LocalDate endDate;
    private EnrollmentStatus status;
}
