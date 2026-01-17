package com.insurancesystem.Model.Dto;

import com.insurancesystem.Model.Entity.Visit;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VisitDTO {
    
    private UUID id;
    
    // Patient information
    private UUID patientId; // Employee/Client ID (if employee)
    private String patientName; // Employee name
    private String employeeId; // Employee ID
    
    private UUID familyMemberId; // Family member ID (if family member)
    private String familyMemberName; // Family member name
    
    // Doctor information
    private UUID doctorId;
    private String doctorName;
    private String doctorSpecialization;
    
    // Visit details
    private LocalDate visitDate;
    private Visit.VisitType visitType; // NORMAL or FOLLOW_UP
    private Integer visitYear;
    
    // Reference to previous visit if follow-up
    private UUID previousVisitId;
    
    // Notes/diagnosis
    private String notes;
    
    // Timestamps
    private Instant createdAt;
    private Instant updatedAt;
    
    // Visit count information
    private Long yearlyVisitCount; // Count of normal visits in the year
    private Long remainingVisits; // Remaining visits for the year (12 - yearlyVisitCount)

    /**
     * Visit statistics for a patient in a year
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class VisitStatistics {
        private Integer year;
        private Integer normalVisits;
        private Integer followUpVisits;
        private Integer totalVisits;
        private Integer remainingVisits;
        private Integer maxYearlyVisits;
    }
}

