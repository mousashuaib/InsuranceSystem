package com.insurancesystem.Controller;

<<<<<<< HEAD
import com.insurancesystem.Model.Entity.DoctorSpecialization;
import com.insurancesystem.Repository.DoctorSpecializationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
=======
import com.insurancesystem.Model.Dto.DoctorSpecializationRequestDto;
import com.insurancesystem.Model.Dto.DoctorSpecializationResponseDto;
import com.insurancesystem.Model.Entity.DoctorSpecializationEntity;
import com.insurancesystem.Repository.DoctorSpecializationRepository;
import com.insurancesystem.Services.DoctorSpecializationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;
>>>>>>> 59fc73de7f549007a5658aab4146b5707a8a4bd8

@RestController
@RequestMapping("/api/doctor-specializations")
@RequiredArgsConstructor
<<<<<<< HEAD
public class DoctorSpecializationController {

    private final DoctorSpecializationRepository specializationRepository;

    /**
     * Get all specializations (public endpoint for registration)
     */
    @GetMapping
    public ResponseEntity<List<DoctorSpecialization>> getAllSpecializations() {
        List<DoctorSpecialization> specializations = specializationRepository.findAllByOrderByDisplayNameAsc();
        return ResponseEntity.ok(specializations);
    }

    /**
     * Get all specializations with details (for doctor dashboard)
     */
    @GetMapping("/with-details")
    public ResponseEntity<List<DoctorSpecialization>> getSpecializationsWithDetails() {
        List<DoctorSpecialization> specializations = specializationRepository.findAllByOrderByDisplayNameAsc();
        return ResponseEntity.ok(specializations);
    }

    /**
     * Get specialization by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<DoctorSpecialization> getSpecializationById(@PathVariable Long id) {
        return specializationRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get all specializations for manager
     */
    @GetMapping("/manager/all")
    public ResponseEntity<List<DoctorSpecialization>> getAllForManager() {
        List<DoctorSpecialization> specializations = specializationRepository.findAll();
        return ResponseEntity.ok(specializations);
=======
@CrossOrigin(origins = "*")
public class DoctorSpecializationController {

    private final DoctorSpecializationRepository repository;
    private final DoctorSpecializationService service;

    /**
     * Get all doctor specializations (basic info only - for signup)
     * Available to all users (including unauthenticated for signup)
     * Does NOT include diagnoses and treatment plans
     */
    @GetMapping
    @PreAuthorize("permitAll()")
    public ResponseEntity<List<Map<String, Object>>> getAllSpecializations() {
        List<DoctorSpecializationEntity> specializations = repository.findAll();

        List<Map<String, Object>> specMaps = specializations.stream()
                .map(spec -> {
                    Map<String, Object> specMap = new HashMap<>();
                    specMap.put("id", spec.getId());
                    specMap.put("displayName", spec.getDisplayName());
                    specMap.put("consultationPrice", spec.getConsultationPrice());
                    // Do NOT include diagnoses and treatment plans for public access
                    return specMap;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(specMaps);
    }

    /**
     * Get all doctor specializations with diagnoses and treatment plans
     * Available only to authenticated DOCTOR role
     */
    @GetMapping("/with-details")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<List<Map<String, Object>>> getAllSpecializationsWithDetails() {
        List<DoctorSpecializationEntity> specializations = repository.findAll();

        List<Map<String, Object>> specMaps = specializations.stream()
                .map(spec -> {
                    Map<String, Object> specMap = new HashMap<>();
                    specMap.put("id", spec.getId());
                    specMap.put("displayName", spec.getDisplayName());
                    specMap.put("consultationPrice", spec.getConsultationPrice());
                    specMap.put("diagnoses", spec.getDiagnoses());
                    specMap.put("treatmentPlans", spec.getTreatmentPlans());
                    // Add restriction fields
                    specMap.put("gender", spec.getGender());
                    specMap.put("minAge", spec.getMinAge());
                    specMap.put("maxAge", spec.getMaxAge());
                    specMap.put("allowedGenders", spec.getAllowedGenders());
                    return specMap;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(specMaps);
    }

    /**
     * Get a specific specialization by ID (with full details)
     * Available only to authenticated DOCTOR role
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<Map<String, Object>> getSpecializationById(@PathVariable Long id) {
        Optional<DoctorSpecializationEntity> specializationOpt = repository.findById(id);

        if (specializationOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        DoctorSpecializationEntity spec = specializationOpt.get();
        Map<String, Object> specMap = new HashMap<>();
        specMap.put("id", spec.getId());
        specMap.put("displayName", spec.getDisplayName());
        specMap.put("consultationPrice", spec.getConsultationPrice());
        specMap.put("diagnoses", spec.getDiagnoses());
        specMap.put("treatmentPlans", spec.getTreatmentPlans());
        // Add restriction fields
        specMap.put("gender", spec.getGender());
        specMap.put("minAge", spec.getMinAge());
        specMap.put("maxAge", spec.getMaxAge());
        specMap.put("allowedGenders", spec.getAllowedGenders());
        return ResponseEntity.ok(specMap);
    }

    /**
     * Create a new doctor specialization
     * Available only to INSURANCE_MANAGER role
     */
    @PostMapping
    @PreAuthorize("hasRole('INSURANCE_MANAGER')")
    public ResponseEntity<DoctorSpecializationResponseDto> createSpecialization(
            @RequestBody DoctorSpecializationRequestDto requestDto) {
        try {
            DoctorSpecializationResponseDto responseDto = service.saveSpecialization(requestDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Update an existing doctor specialization
     * Available only to INSURANCE_MANAGER role
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('INSURANCE_MANAGER')")
    public ResponseEntity<DoctorSpecializationResponseDto> updateSpecialization(
            @PathVariable Long id,
            @RequestBody DoctorSpecializationRequestDto requestDto) {
        try {
            DoctorSpecializationResponseDto responseDto = service.updateSpecialization(id, requestDto);
            return ResponseEntity.ok(responseDto);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Delete a doctor specialization
     * Available only to INSURANCE_MANAGER role
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('INSURANCE_MANAGER')")
    public ResponseEntity<Void> deleteSpecialization(@PathVariable Long id) {
        try {
            service.deleteSpecializationById(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Get all doctor specializations with full details (for INSURANCE_MANAGER)
     * Available only to INSURANCE_MANAGER role
     */
    @GetMapping("/manager/all")
    @PreAuthorize("hasRole('INSURANCE_MANAGER')")
    public ResponseEntity<List<Map<String, Object>>> getAllSpecializationsForManager() {
        List<DoctorSpecializationEntity> specializations = repository.findAll();

        List<Map<String, Object>> specMaps = specializations.stream()
                .map(spec -> {
                    Map<String, Object> specMap = new HashMap<>();
                    specMap.put("id", spec.getId());
                    specMap.put("displayName", spec.getDisplayName());
                    specMap.put("consultationPrice", spec.getConsultationPrice());
                    specMap.put("diagnoses", spec.getDiagnoses());
                    specMap.put("treatmentPlans", spec.getTreatmentPlans());
                    specMap.put("gender", spec.getGender());
                    specMap.put("minAge", spec.getMinAge());
                    specMap.put("maxAge", spec.getMaxAge());
                    specMap.put("allowedGenders", spec.getAllowedGenders());
                    return specMap;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(specMaps);
>>>>>>> 59fc73de7f549007a5658aab4146b5707a8a4bd8
    }
}
