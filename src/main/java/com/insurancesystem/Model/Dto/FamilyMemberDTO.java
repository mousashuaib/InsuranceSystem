package com.insurancesystem.Model.Dto;

import com.insurancesystem.Model.Entity.Enums.FamilyRelation;
import com.insurancesystem.Model.Entity.Enums.Gender;
import com.insurancesystem.Model.Entity.Enums.MemberStatus;
import com.insurancesystem.Model.Entity.Enums.ProfileStatus;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FamilyMemberDTO {

    private UUID id;
    private String fullName;
    private String nationalId;
    private String insuranceNumber;
    private FamilyRelation relation;
    private Gender gender;
    private LocalDate dateOfBirth;
    private java.util.List<String> documentImages;
    private Instant createdAt;
    private ProfileStatus status;
    private String clientFullName;
    private UUID clientId;
    private String clientNationalId;
    private MemberStatus clientStatus;


}
