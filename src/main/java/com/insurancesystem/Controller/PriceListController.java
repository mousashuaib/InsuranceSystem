package com.insurancesystem.Controller;

import com.insurancesystem.Model.Dto.*;
import com.insurancesystem.Model.Entity.Enums.ProviderType;
import com.insurancesystem.Model.Entity.Client;
import com.insurancesystem.Model.Entity.DoctorSpecializationEntity;
import com.insurancesystem.Repository.ClientRepository;
import com.insurancesystem.Repository.DoctorSpecializationRepository;
import com.insurancesystem.Services.PriceListService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/pricelist")
@RequiredArgsConstructor
public class PriceListController {

    private final PriceListService priceListService;
    private final ClientRepository clientRepository;
    private final DoctorSpecializationRepository doctorSpecializationRepository;

    @PostMapping
    @PreAuthorize("hasRole('INSURANCE_MANAGER')")
    public PriceListResponseDTO create(@RequestBody CreatePriceListDTO dto) {
        return priceListService.create(dto);
    }

    @GetMapping("/{type}")
    @PreAuthorize("hasAnyRole('INSURANCE_MANAGER','DOCTOR','PHARMACIST')")
    public List<PriceListResponseDTO> getByType(
            @PathVariable ProviderType type,
            Authentication authentication
    ) {
        // Check if user is a doctor and get their specialization ID
        Long doctorSpecializationId = getDoctorSpecializationId(authentication);

        if (doctorSpecializationId != null) {
            // Apply restrictions for doctors
            return priceListService.getByTypeWithRestrictions(type, doctorSpecializationId);
        } else {
            // No restrictions for non-doctors (managers, pharmacists)
            return priceListService.getByType(type);
        }
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('INSURANCE_MANAGER')")
    public PriceListResponseDTO update(
            @PathVariable UUID id,
            @RequestBody UpdatePriceListDTO dto
    ) {
        return priceListService.updatePrice(id, dto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('INSURANCE_MANAGER')")
    public void delete(@PathVariable UUID id) {
        priceListService.deletePrice(id);
    }

    // 🧪 Lab Tests
    @GetMapping("/lab/tests")
    @PreAuthorize("hasAnyRole('DOCTOR','LAB_TECH','INSURANCE_MANAGER','MANAGER')")
    public List<PriceListResponseDTO> getLabTests(Authentication authentication) {
        // Check if user is a doctor and get their specialization ID
        Long doctorSpecializationId = getDoctorSpecializationId(authentication);

        if (doctorSpecializationId != null) {
            // Apply restrictions for doctors
            return priceListService.getByTypeWithRestrictions(ProviderType.LAB, doctorSpecializationId);
        } else {
            // No restrictions for non-doctors
            return priceListService.getByType(ProviderType.LAB);
        }
    }

    // 🩻 Radiology Tests
    @GetMapping("/radiology/tests")
    @PreAuthorize("hasAnyRole('DOCTOR','RADIOLOGIST','INSURANCE_MANAGER','MANAGER')")
    public List<PriceListResponseDTO> getRadiologyTests(Authentication authentication) {
        // Check if user is a doctor and get their specialization ID
        Long doctorSpecializationId = getDoctorSpecializationId(authentication);

        if (doctorSpecializationId != null) {
            // Apply restrictions for doctors
            return priceListService.getByTypeWithRestrictions(ProviderType.RADIOLOGY, doctorSpecializationId);
        } else {
            // No restrictions for non-doctors
            return priceListService.getByType(ProviderType.RADIOLOGY);
        }
    }

    /**
     * Get doctor's specialization ID from authentication context
     * Returns null if user is not a doctor or specialization is not available
     */
    private Long getDoctorSpecializationId(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return null;
        }

        try {
            // Check if user has DOCTOR role
            boolean isDoctor = authentication.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_DOCTOR"));

            if (!isDoctor) {
                return null; // Not a doctor, no restrictions
            }

            // Get the authenticated user
            Client doctor = clientRepository.findByEmail(authentication.getName())
                    .orElse(null);


            if (doctor == null) {
                return null;
            }

            String specializationName = doctor.getSpecialization();
            if (specializationName == null || specializationName.trim().isEmpty()) {
                return null;
            }

            // Find the specialization entity by display name (exact match first)
            return doctorSpecializationRepository.findByDisplayName(specializationName.trim())
                    .map(DoctorSpecializationEntity::getId)
                    .orElseGet(() -> {
                        // Try case-insensitive match if exact match fails
                        // Get all specializations and find by case-insensitive comparison
                        return doctorSpecializationRepository.findAll().stream()
                                .filter(spec -> spec.getDisplayName() != null &&
                                        spec.getDisplayName().equalsIgnoreCase(specializationName.trim()))
                                .map(DoctorSpecializationEntity::getId)
                                .findFirst()
                                .orElse(null);
                    });

        } catch (Exception e) {
            // If error occurs, return null (no restrictions)
            return null;
        }
    }
}
