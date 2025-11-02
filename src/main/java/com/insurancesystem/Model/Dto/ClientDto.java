package com.insurancesystem.Model.Dto;

import com.insurancesystem.Model.Entity.Enums.MemberStatus;
import com.insurancesystem.Model.Entity.Enums.RoleName;
import com.insurancesystem.Model.Entity.Enums.RoleRequestStatus;
import lombok.*;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ClientDto {
    private UUID id;
    private String username;
    private String fullName;
    private String email;
    private String phone;
    private MemberStatus status;
    private Set<RoleName> roles;
    private Instant createdAt;
    private Instant updatedAt;
    private String universityCardImage; // ✅ هذا السطر مهم

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


    private RoleName requestedRole;
    private RoleRequestStatus roleRequestStatus; // ← Enum وليس String
}