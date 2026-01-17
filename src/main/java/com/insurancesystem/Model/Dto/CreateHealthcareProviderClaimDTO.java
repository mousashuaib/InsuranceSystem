package com.insurancesystem.Model.Dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class CreateHealthcareProviderClaimDTO {
    private UUID clientId;
    private String description;
    private Double amount;
    private LocalDate serviceDate;
    private String roleSpecificData; // JSON: medicines, testResults, imaging, etc
<<<<<<< HEAD

    // Additional fields for better claim info
    private String diagnosis;
    private String treatmentDetails;
    private Boolean isFollowUp;
    private String providerRole;

    // Client info fields
    private Integer clientAge;
    private String clientGender;
    private String clientEmployeeId;
    private String clientNationalId;
    private String clientFaculty;
    private String clientDepartment;

    // Provider info fields
    private String providerName;
    private String providerEmployeeId;
    private String providerNationalId;
    private String providerSpecialization;
    private String providerPharmacyCode;
    private String providerLabCode;
    private String providerRadiologyCode;

    // Family member fields
    private String familyMemberName;
    private String familyMemberRelation;
    private Integer familyMemberAge;
    private String familyMemberGender;
    private String familyMemberInsuranceNumber;
    private String familyMemberNationalId;
=======
    private String providerRole;
    private String employeeId;
    private String diagnosis;         // NEW
    private String treatmentDetails;  // NEW
>>>>>>> 59fc73de7f549007a5658aab4146b5707a8a4bd8
}

