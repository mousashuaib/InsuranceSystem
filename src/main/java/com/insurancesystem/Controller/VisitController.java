package com.insurancesystem.Controller;

import com.insurancesystem.Exception.BadRequestException;
import com.insurancesystem.Exception.NotFoundException;
import com.insurancesystem.Model.Dto.CreateVisitDTO;
import com.insurancesystem.Model.Dto.VisitDTO;
import com.insurancesystem.Services.VisitService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/visits")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class VisitController {

    private final VisitService visitService;

    /**
     * Create a new visit
     * Accessible by DOCTOR role
     */
    @PostMapping("/create")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<?> createVisit(@RequestBody @Valid CreateVisitDTO dto) {
        try {
            VisitDTO visit = visitService.createVisit(dto);
            return ResponseEntity.ok(visit);
        } catch (BadRequestException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "BAD_REQUEST");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (NotFoundException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "NOT_FOUND");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "INTERNAL_ERROR");
            errorResponse.put("message", e.getMessage() != null ? e.getMessage() : "Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get all visits for a patient (employee or family member)
     * Accessible by DOCTOR, INSURANCE_CLIENT, ADMIN, INSURANCE_MANAGER
     */
    @GetMapping("/patient")
    @PreAuthorize("hasAnyRole('DOCTOR', 'INSURANCE_CLIENT', 'ADMIN', 'INSURANCE_MANAGER')")
    public ResponseEntity<?> getPatientVisits(
            @RequestParam(required = false) UUID patientId,
            @RequestParam(required = false) UUID familyMemberId) {
        try {
            List<VisitDTO> visits = visitService.getPatientVisits(patientId, familyMemberId);
            return ResponseEntity.ok(visits);
        } catch (BadRequestException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "BAD_REQUEST");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "INTERNAL_ERROR");
            errorResponse.put("message", e.getMessage() != null ? e.getMessage() : "Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get visits for a patient in a specific year
     * Accessible by DOCTOR, INSURANCE_CLIENT, ADMIN, INSURANCE_MANAGER
     */
    @GetMapping("/patient/year")
    @PreAuthorize("hasAnyRole('DOCTOR', 'INSURANCE_CLIENT', 'ADMIN', 'INSURANCE_MANAGER')")
    public ResponseEntity<?> getPatientVisitsByYear(
            @RequestParam(required = false) UUID patientId,
            @RequestParam(required = false) UUID familyMemberId,
            @RequestParam(required = false) Integer year) {
        try {
            List<VisitDTO> visits = visitService.getPatientVisitsByYear(patientId, familyMemberId, year);
            return ResponseEntity.ok(visits);
        } catch (BadRequestException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "BAD_REQUEST");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "INTERNAL_ERROR");
            errorResponse.put("message", e.getMessage() != null ? e.getMessage() : "Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get visit statistics for a patient in a year
     * Shows normal visits, follow-up visits, total visits, and remaining visits
     * Accessible by DOCTOR, INSURANCE_CLIENT, ADMIN, INSURANCE_MANAGER
     */
    @GetMapping("/patient/statistics")
    @PreAuthorize("hasAnyRole('DOCTOR', 'INSURANCE_CLIENT', 'ADMIN', 'INSURANCE_MANAGER')")
    public ResponseEntity<?> getPatientVisitStatistics(
            @RequestParam(required = false) UUID patientId,
            @RequestParam(required = false) UUID familyMemberId,
            @RequestParam(required = false) Integer year) {
        try {
            VisitDTO.VisitStatistics statistics = visitService.getPatientVisitStatistics(patientId, familyMemberId, year);
            return ResponseEntity.ok(statistics);
        } catch (BadRequestException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "BAD_REQUEST");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "INTERNAL_ERROR");
            errorResponse.put("message", e.getMessage() != null ? e.getMessage() : "Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}




