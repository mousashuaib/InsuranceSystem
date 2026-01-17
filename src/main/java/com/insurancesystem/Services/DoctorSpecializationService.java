package com.insurancesystem.Services;

import com.insurancesystem.Model.Dto.DoctorSpecializationRequestDto;
import com.insurancesystem.Model.Dto.DoctorSpecializationResponseDto;
import com.insurancesystem.Model.Entity.DoctorSpecializationEntity;
import com.insurancesystem.Model.MapStruct.DoctorSpecializationMapper;
import com.insurancesystem.Repository.DoctorSpecializationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DoctorSpecializationService {

    private final DoctorSpecializationRepository repository;
    private final DoctorSpecializationMapper mapper;

    /**
     * Fetch all specializations and map them to ResponseDto.
     *
     * @return List<DoctorSpecializationResponseDto>
     */
    public List<DoctorSpecializationResponseDto> getAllSpecializations() {
        return repository.findAll()
                .stream()
                .map(mapper::toResponseDto) // Convert Entity to ResponseDto
                .collect(Collectors.toList());
    }

    /**
     * Fetch a specialization by its display name and map to ResponseDto.
     *
     * @param name Specialization name
     * @return DoctorSpecializationResponseDto
     */
    public DoctorSpecializationResponseDto getSpecializationByName(String name) {
        DoctorSpecializationEntity entity = repository.findByDisplayName(name)
                .orElseThrow(() -> new RuntimeException("Specialization not found with name: " + name));
        return mapper.toResponseDto(entity);
    }

    /**
     * Save or update a specialization by mapping from RequestDto to Entity.
     *
     * @param requestDto DoctorSpecializationRequestDto
     * @return DoctorSpecializationResponseDto
     */
    public DoctorSpecializationResponseDto saveSpecialization(DoctorSpecializationRequestDto requestDto) {
        DoctorSpecializationEntity entity = mapper.toEntity(requestDto); // Convert RequestDto to Entity
        DoctorSpecializationEntity savedEntity = repository.save(entity);
        return mapper.toResponseDto(savedEntity); // Convert saved Entity to ResponseDto
    }

    /**
     * Delete specialization by ID.
     *
     * @param id Specialization ID
     */
    public void deleteSpecializationById(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Specialization not found with ID: " + id);
        }
        repository.deleteById(id);
    }

    /**
     * Update an existing specialization.
     *
     * @param id ID of the specialization to update
     * @param requestDto Updated specialization data
     * @return Updated DoctorSpecializationResponseDto
     */
    public DoctorSpecializationResponseDto updateSpecialization(Long id, DoctorSpecializationRequestDto requestDto) {
        // Fetch the existing specialization
        DoctorSpecializationEntity existingEntity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Specialization not found with ID: " + id));

        // Update the fields
        existingEntity.setDisplayName(requestDto.getDisplayName());
        existingEntity.setConsultationPrice(requestDto.getConsultationPrice());
        existingEntity.setDiagnoses(requestDto.getDiagnoses());
        existingEntity.setTreatmentPlans(requestDto.getTreatmentPlans());
        existingEntity.setAllowedGenders(requestDto.getAllowedGenders());
        existingEntity.setMinAge(requestDto.getMinAge());
        existingEntity.setMaxAge(requestDto.getMaxAge());
        existingEntity.setGender(requestDto.getGender());

        // Save the updated specialization
        DoctorSpecializationEntity updatedEntity = repository.save(existingEntity);
        return mapper.toResponseDto(updatedEntity);
    }
}

