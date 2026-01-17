package com.insurancesystem.Services;

import com.insurancesystem.Model.Dto.*;
import com.insurancesystem.Model.Entity.*;
import com.insurancesystem.Model.MapStruct.PriceListMapper;
import com.insurancesystem.Repository.PriceListRepository;
import com.insurancesystem.Repository.DoctorSpecializationRepository;
import com.insurancesystem.Model.Entity.Enums.ProviderType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PriceListService {

    private final PriceListRepository priceListRepository;
    private final PriceListMapper priceListMapper;
    private final DoctorSpecializationRepository doctorSpecializationRepository;

    public PriceListResponseDTO create(CreatePriceListDTO dto) {
        PriceList entity = PriceList.builder()
                .providerType(dto.getProviderType())
                .serviceName(dto.getServiceName())
                .serviceCode(dto.getServiceCode())
                .price(dto.getPrice())
                .quantity(dto.getQuantity())
                .notes(dto.getNotes())
                .serviceDetails(dto.getServiceDetails())
                .active(true)
                .build();

        // If allowedSpecializationIds are provided, load and set them
        if (dto.getAllowedSpecializationIds() != null && !dto.getAllowedSpecializationIds().isEmpty()) {
            List<DoctorSpecializationEntity> allowedSpecs = dto.getAllowedSpecializationIds().stream()
                    .map(id -> doctorSpecializationRepository.findById(id)
                            .orElseThrow(() -> new RuntimeException("Specialization not found: " + id)))
                    .collect(Collectors.toList());
            entity.setAllowedSpecializations(allowedSpecs);
        }

        // Set allowed genders if provided
        if (dto.getAllowedGenders() != null && !dto.getAllowedGenders().isEmpty()) {
            entity.setAllowedGenders(dto.getAllowedGenders());
        }

        // Set age restrictions if provided
        entity.setMinAge(dto.getMinAge());
        entity.setMaxAge(dto.getMaxAge());

        priceListRepository.save(entity);
        return priceListMapper.toDto(entity);
    }

    public PriceListResponseDTO updatePrice(UUID id, UpdatePriceListDTO dto) {
        PriceList price = priceListRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Price not found"));

        if (dto.getServiceName() != null) price.setServiceName(dto.getServiceName());
        if (dto.getServiceCode() != null) price.setServiceCode(dto.getServiceCode());
        if (dto.getPrice() != null) price.setPrice(dto.getPrice());
        if (dto.getQuantity() != null) price.setQuantity(dto.getQuantity());
        if (dto.getNotes() != null) price.setNotes(dto.getNotes());
        if (dto.getActive() != null) price.setActive(dto.getActive());
        if (dto.getServiceDetails() != null) price.setServiceDetails(dto.getServiceDetails());

        // Update allowed specializations if provided
        if (dto.getAllowedSpecializationIds() != null) {
            if (dto.getAllowedSpecializationIds().isEmpty()) {
                price.setAllowedSpecializations(null); // Clear restrictions (available to all)
            } else {
                List<DoctorSpecializationEntity> allowedSpecs = dto.getAllowedSpecializationIds().stream()
                        .map(specId -> doctorSpecializationRepository.findById(specId)
                                .orElseThrow(() -> new RuntimeException("Specialization not found: " + specId)))
                        .collect(Collectors.toList());
                price.setAllowedSpecializations(allowedSpecs);
            }
        }

        // Update allowed genders if provided
        if (dto.getAllowedGenders() != null) {
            if (dto.getAllowedGenders().isEmpty()) {
                price.setAllowedGenders(null); // Clear restrictions (available to all)
            } else {
                price.setAllowedGenders(dto.getAllowedGenders());
            }
        }

        // Update age restrictions if provided
        if (dto.getMinAge() != null) {
            price.setMinAge(dto.getMinAge());
        }
        if (dto.getMaxAge() != null) {
            price.setMaxAge(dto.getMaxAge());
        }

        priceListRepository.save(price);
        return priceListMapper.toDto(price);
    }

    public void deletePrice(UUID id) {
        priceListRepository.deleteById(id);
    }

    /**
     * Get services by type WITHOUT restrictions (for admin/manager)
     */
    public List<PriceListResponseDTO> getByType(ProviderType type) {
        List<PriceListResponseDTO> result = priceListRepository.findByProviderType(type)
                .stream()
                .map(priceListMapper::toDto)
                .collect(Collectors.toList());
        
        // Debug: log quantity for each item
        for (PriceListResponseDTO dto : result) {
            System.out.println("🔍 [PriceListService] Item: " + dto.getServiceName() + 
                ", Quantity: " + dto.getQuantity() + 
                ", Price: " + dto.getPrice());
        }
        
        return result;
    }

    /**
     * Get services by provider type (alias for getByType)
     */
    public List<PriceListResponseDTO> getByProviderType(ProviderType type) {
        return getByType(type);
    }

    /**
     * Get services by type WITH specialization restrictions
     * @param type Provider type (PHARMACY, LAB, RADIOLOGY)
     * @param doctorSpecializationId Doctor's specialization ID from doctor_specialization table
     * @return Filtered list of services based on restrictions
     */
    @Transactional(readOnly = true)
    public List<PriceListResponseDTO> getByTypeWithRestrictions(ProviderType type, Long doctorSpecializationId) {
        // Use repository method that eagerly loads allowedSpecializations
        // Make sure PriceListRepository.findByProviderType uses @EntityGraph or JOIN FETCH
        List<PriceList> allServices = priceListRepository.findByProviderType(type);

        // If no specialization ID provided, return all services
        if (doctorSpecializationId == null) {
            return allServices.stream()
                    .map(priceListMapper::toDto)
                    .collect(Collectors.toList());
        }

        // Get the doctor's specialization entity
        DoctorSpecializationEntity doctorSpecialization = doctorSpecializationRepository.findById(doctorSpecializationId)
                .orElse(null);

        if (doctorSpecialization == null) {
            // If specialization not found, return all services
            return allServices.stream()
                    .map(priceListMapper::toDto)
                    .collect(Collectors.toList());
        }

        // Filter services based on restrictions
        // IMPORTANT: Ensure allowedSpecializations is loaded by initializing the collection
        return allServices.stream()
                .filter(service -> {
                    // Force initialization of the lazy-loaded collection
                    List<DoctorSpecializationEntity> allowedSpecs = service.getAllowedSpecializations();
                    // If the collection is not initialized, it will be null or empty
                    // This means the repository query needs to eagerly fetch it
                    return isServiceAllowedForSpecialization(service, doctorSpecialization);
                })
                .map(priceListMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Check if a service is allowed for a specific doctor specialization
     */
    private boolean isServiceAllowedForSpecialization(PriceList service, DoctorSpecializationEntity doctorSpecialization) {
        List<DoctorSpecializationEntity> allowedSpecs = service.getAllowedSpecializations();

        // If no restrictions (null or empty), service is available to all specializations
        if (allowedSpecs == null || allowedSpecs.isEmpty()) {
            return true;
        }

        // Check if doctor's specialization is in the allowed list
        return allowedSpecs.stream()
                .anyMatch(allowed -> allowed.getId().equals(doctorSpecialization.getId()));
    }
}

