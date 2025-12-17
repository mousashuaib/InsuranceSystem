package com.insurancesystem.Controller;

import com.insurancesystem.Model.Entity.DoctorSpecializationEntity;
import com.insurancesystem.Repository.DoctorSpecializationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/doctor-specializations")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DoctorSpecializationController {

    private final DoctorSpecializationRepository repository;

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
        return ResponseEntity.ok(specMap);
    }
}
