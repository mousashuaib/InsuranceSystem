package com.insurancesystem.Model.Dto;

import lombok.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RadiologyRequestDTO {

    private UUID id;

    private UUID testId;   // 🆕 ID من PriceList
    private String testName; // اسم الفحص المستخرج من PriceList

    private String notes;
    private String resultUrl;
    private String status;

    private UUID doctorId;
    private String doctorName;


    private UUID memberId;
    private String memberName;
    private String employeeId;
    private UUID radiologistId;
    private String radiologistName;
    private List<String> universityCardImages;
    private String universityCardImage; // First university card image (for easier access)
    private Double enteredPrice;  // السعر الذي يدخله الراديولوجي
    private Double approvedPrice; // 🆕 السعر المعتمد (أقل من النقابي)

    private long total;
    private long pending;
    private long completed;

    private Instant createdAt;
    private Instant updatedAt;

    private String diagnosis;
    private String treatment;
    // 🆕 Family Member Information (if prescription is for a family member)
    private Boolean isFamilyMember;
    private UUID familyMemberId; // 🆕 Family member ID for direct lookup
    private String familyMemberName;
    private String familyMemberRelation;
    private String familyMemberInsuranceNumber;
    private String familyMemberAge;
    private String familyMemberGender;
    private String familyMemberNationalId; // National ID of the family member
    // 🆕 Main Client Information (age and gender)
    private String memberAge;
    private String memberGender;
    private String memberNationalId; // National ID of the main client/patient
}
