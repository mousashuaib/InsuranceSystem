package com.insurancesystem.Model.Dto;

import com.insurancesystem.Model.Entity.Enums.EmergencyStatus;
import lombok.Builder;
import lombok.Data;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class EmergencyRequestDTO {
    private UUID id;
    private UUID memberId;
    private String memberName;
    private String description;
    private String location;
    private String universityCardImages;
    private String contactPhone;
    private LocalDate incidentDate;
    private String notes;
    private EmergencyStatus status;
    private Instant submittedAt;
    private Instant approvedAt;
    private Instant rejectedAt;
    private String rejectionReason;
}
