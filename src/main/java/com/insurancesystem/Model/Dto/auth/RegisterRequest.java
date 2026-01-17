package com.insurancesystem.Model.Dto.auth;

import com.insurancesystem.Model.Dto.RegisterFamilyMemberDTO;
import com.insurancesystem.Model.Entity.Enums.ChronicDisease;
import com.insurancesystem.Model.Entity.Enums.RoleName;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {



    @NotBlank
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$",
            message = "Password must contain letters and numbers"
    )
    private String password;


    @NotBlank
    @Size(min = 3, max = 150)
    private String fullName;

    @Email
    @Size(max = 150)
    private String email;

    @Size(max = 40)
    private String phone;

    private RoleName desiredRole; // INSURANCE_CLIENT, PHARMACY_STAFF, LAB_STAFF, CLINIC_STAFF...
    private boolean agreeToPolicy;

    // optional info (depends on role)
    private String employeeId;
    private String department;
    private String faculty;
    private String specialization;
    private String clinicLocation;
    private String pharmacyCode;
    private String pharmacyName;
    private String pharmacyLocation;
    private String labCode;
    private String labName;
    @NotBlank
    @Size(min = 9, max = 20)
    private String nationalId;
    private String gender;
    private String labLocation;
    private String radiologyCode;
    private String radiologyName;
    private String radiologyLocation;
    @NotNull
    private LocalDate dateOfBirth;
    private List<RegisterFamilyMemberDTO> familyMembers;
    private boolean hasChronicDiseases;
    private List<ChronicDisease> chronicDiseases;


}