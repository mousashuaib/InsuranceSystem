package com.insurancesystem.Model.Dto.auth;

import com.insurancesystem.Model.Entity.Enums.RoleName;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank
    @Size(min = 3, max = 64)
    private String username;

    @NotBlank
    @Size(min = 8, max = 72)
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
    private String labLocation;
}