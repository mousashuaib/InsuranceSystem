package com.insurancesystem.Model.Dto;


import com.insurancesystem.Model.Entity.DoctorSpecializationEntity;
import com.insurancesystem.Model.Entity.Enums.MemberStatus;
import com.insurancesystem.Model.Entity.Enums.RoleName;
import com.insurancesystem.Model.Entity.Enums.RoleRequestStatus;

import com.insurancesystem.Model.Entity.Enums.*;

import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ClientDto {

    private UUID id;
    private String fullName;
    private String email;
    private String phone;
    private MemberStatus status;
    private Set<RoleName> roles;
    private Instant createdAt;
    private Instant updatedAt;
    private java.util.List<String> universityCardImages;
    private java.util.List<String> chronicDocumentPaths;
    private String nationalId;
    private boolean hasChronicDiseases;
    private Set<ChronicDisease> chronicDiseases;
    private String policyName;

    private LocalDate dateOfBirth;

    // Employee/Healthcare Provider Information
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
    private String radiologyCode;
    private String radiologyName;
    private String radiologyLocation;
    private DoctorSpecializationEntity doctorSpecialization;
    // Role Request Information
    private RoleName requestedRole;
    private RoleRequestStatus roleRequestStatus;

}

