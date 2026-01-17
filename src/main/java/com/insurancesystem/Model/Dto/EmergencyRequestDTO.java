package com.insurancesystem.Model.Dto;

import com.insurancesystem.Model.Entity.Enums.EmergencyStatus;
import lombok.Builder;
import lombok.Data;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class EmergencyRequestDTO {
    private UUID id;
    private UUID memberId;
    private String memberName;
    private String employeeId;
    private String memberAge;
    private String memberGender;
    private String memberNationalId;
    private List<String> universityCardImages;
    private String universityCardImage; // First university card image (for easier access)
    private String description;
    private String location;
    private String contactPhone;
    private LocalDate incidentDate;
    private String notes;
    private EmergencyStatus status;
    private Instant submittedAt;
    private Instant approvedAt;
    private Instant rejectedAt;
    private String rejectionReason;
    // Family Member Information (if emergency is for a family member)
    private Boolean isFamilyMember;
    private UUID familyMemberId;
    private String familyMemberName;
    private String familyMemberRelation;
    private String familyMemberInsuranceNumber;
    private String familyMemberAge;
    private String familyMemberGender;
    private String familyMemberNationalId;
}
