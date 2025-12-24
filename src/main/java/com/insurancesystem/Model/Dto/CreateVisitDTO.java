package com.insurancesystem.Model.Dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateVisitDTO {
    
    // Patient can be either employee (patientId) or family member (familyMemberId)
    private UUID patientId; // Employee/Client ID
    private UUID familyMemberId; // Family member ID
    
    // Doctor ID (Client with DOCTOR role)
    private UUID doctorId;
    
    // Visit date (defaults to today if not provided)
    private LocalDate visitDate;
    
    // Optional notes/diagnosis
    private String notes;
}




