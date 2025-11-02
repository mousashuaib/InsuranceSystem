package com.insurancesystem.Services;

import com.insurancesystem.Exception.NotFoundException;
import com.insurancesystem.Model.Dto.ClientDto;
import com.insurancesystem.Model.Dto.RadiologyRequestDTO;
import com.insurancesystem.Model.Dto.UpdateUserDTO;
import com.insurancesystem.Model.Entity.Client;
import com.insurancesystem.Model.Entity.Enums.RoleName;
import com.insurancesystem.Model.Entity.RadiologyRequest;
import com.insurancesystem.Model.Entity.Enums.LabRequestStatus;
import com.insurancesystem.Model.MapStruct.ClientMapper;
import com.insurancesystem.Model.MapStruct.RadiologyRequestMapper;
import com.insurancesystem.Repository.ClientRepository;
import com.insurancesystem.Repository.RadiologistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RadiologyRequestService {

    private final RadiologistRepository radiologyRequestRepository;
    private final ClientRepository clientRepository;
    private final RadiologyRequestMapper radiologyRequestMapper;
    private final ClientMapper clientMapper;

    // 🧑‍⚕️ Get the radiologist's ID from the username (authentication)
    public UUID getRadiologistIdByUsername(String username) {
        Client radiologist = clientRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Radiologist not found"));
        return radiologist.getId();  // Return the Radiologist's ID
    }

    // ➕ Create a new Radiology Request
    public RadiologyRequestDTO create(RadiologyRequestDTO dto) {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();

        // 🧑‍⚕️ Get the doctor from the authenticated username
        Client doctor = clientRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new NotFoundException("Doctor not found"));

        // 👤 Get the member (patient)
        Client member;
        if (dto.getMemberId() != null) {
            member = clientRepository.findById(dto.getMemberId())
                    .orElseThrow(() -> new NotFoundException("Member not found"));
        } else if (dto.getMemberName() != null && !dto.getMemberName().isBlank()) {
            member = clientRepository.findByFullName(dto.getMemberName())
                    .orElseThrow(() -> new NotFoundException("Member not found"));
        } else {
            throw new RuntimeException("Member info required");
        }

        // 🧑‍🔬 Get the radiologist (lab tech)
        Client radiologist;
        if (dto.getRadiologistId() != null) {
            radiologist = clientRepository.findById(dto.getRadiologistId())
                    .orElseThrow(() -> new NotFoundException("Radiologist not found"));
        } else if (dto.getRadiologistName() != null && !dto.getRadiologistName().isBlank()) {
            radiologist = clientRepository.findByFullName(dto.getRadiologistName())
                    .orElseThrow(() -> new NotFoundException("Radiologist not found with name: " + dto.getRadiologistName()));
        } else {
            throw new RuntimeException("Radiologist info required");
        }

        // 📝 Create the radiology request
        RadiologyRequest request = radiologyRequestMapper.toEntity(dto);
        request.setDoctor(doctor);
        request.setMember(member);
        request.setRadiologist(radiologist);  // Set the radiologist for the request
        request.setStatus(LabRequestStatus.PENDING);
        request.setCreatedAt(Instant.now());
        request.setUpdatedAt(Instant.now());

        return radiologyRequestMapper.toDto(radiologyRequestRepository.save(request));
    }

    // 📖 Radiologist views pending radiology requests
    public List<RadiologyRequestDTO> getPendingRequests(UUID radiologistId) {
        return radiologyRequestRepository.findByRadiologistId(radiologistId)
                .stream()
                .filter(request -> request.getStatus() == LabRequestStatus.PENDING)
                .map(radiologyRequestMapper::toDto)
                .collect(Collectors.toList());
    }

    // 🧪 Radiologist uploads radiology result
    public RadiologyRequestDTO uploadRadiologyResult(UUID requestId, MultipartFile file) {
        Client radiologist = clientRepository.findByUsername("radiologistUsername")  // Get radiologist from authentication
                .orElseThrow(() -> new NotFoundException("Radiologist not found"));

        RadiologyRequest request = radiologyRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Radiology request not found"));

        if (request.getStatus() == LabRequestStatus.COMPLETED) {
            throw new RuntimeException("Result already uploaded");
        }

        try {
            String uploadDir = "uploads/radiology_results";
            Files.createDirectories(Paths.get(uploadDir));

            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path filePath = Paths.get(uploadDir, fileName);

            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            request.setResultUrl("/" + uploadDir + "/" + fileName);
            request.setStatus(LabRequestStatus.COMPLETED);
            request.setRadiologist(radiologist);
            request.setUpdatedAt(Instant.now());

        } catch (IOException e) {
            throw new RuntimeException("Failed to upload radiology result", e);
        }

        return radiologyRequestMapper.toDto(radiologyRequestRepository.save(request));
    }

    // 📖 Member or Doctor views the result of a radiology request
    public RadiologyRequestDTO getResult(UUID id) {
        RadiologyRequest request = radiologyRequestRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Radiology request not found"));

        return radiologyRequestMapper.toDto(request);
    }

    // ✏️ Doctor updates the radiology request
    public RadiologyRequestDTO update(UUID id, RadiologyRequestDTO dto) {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        Client doctor = clientRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new NotFoundException("Doctor not found"));

        RadiologyRequest request = radiologyRequestRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Radiology request not found"));

        if (!request.getDoctor().getId().equals(doctor.getId())) {
            throw new RuntimeException("You are not allowed to update this request");
        }

        if (request.getStatus() == LabRequestStatus.COMPLETED) {
            throw new RuntimeException("Cannot update a completed radiology request");
        }

        request.setTestName(dto.getTestName());
        request.setNotes(dto.getNotes());
        request.setUpdatedAt(Instant.now());

        return radiologyRequestMapper.toDto(radiologyRequestRepository.save(request));
    }

    // ❌ Doctor deletes a radiology request
    public void delete(UUID id) {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        Client doctor = clientRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new NotFoundException("Doctor not found"));

        RadiologyRequest request = radiologyRequestRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Radiology request not found"));

        if (!request.getDoctor().getId().equals(doctor.getId())) {
            throw new RuntimeException("You are not allowed to delete this request");
        }

        if (request.getStatus() == LabRequestStatus.COMPLETED) {
            throw new RuntimeException("Cannot delete a completed radiology request");
        }

        radiologyRequestRepository.delete(request);
    }

    // 📖 Doctor views all their radiology requests
    public List<RadiologyRequestDTO> getByDoctor() {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        Client doctor = clientRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new NotFoundException("Doctor not found"));

        return radiologyRequestRepository.findByDoctorId(doctor.getId())
                .stream()
                .map(radiologyRequestMapper::toDto)
                .collect(Collectors.toList());
    }

    // 📊 Radiologist views stats of radiology requests
    public RadiologyRequestDTO getRadiologyStats(UUID radiologistId) {
        long pending = radiologyRequestRepository.countByStatusAndRadiologistId(LabRequestStatus.PENDING, radiologistId);
        long completed = radiologyRequestRepository.countByStatusAndRadiologistId(LabRequestStatus.COMPLETED, radiologistId);
        long total = pending + completed;

        return RadiologyRequestDTO.builder()
                .total(total)
                .pending(pending)
                .completed(completed)
                .build();
    }

    // 📖 Radiologist views all their radiology requests (pending + completed)
    public List<RadiologyRequestDTO> getAllForCurrentRadiologist(UUID radiologistId) {
        return radiologyRequestRepository.findByRadiologistId(radiologistId)
                .stream()
                .map(radiologyRequestMapper::toDto)
                .collect(Collectors.toList());
    }

    // 📖 Get all radiologists
    public List<ClientDto> getAllRadiologists() {
        return clientRepository.findByRoles_Name(RoleName.RADIOLOGIST)
                .stream()
                .map(clientMapper::toDTO)
                .collect(Collectors.toList());
    }

    // 📖 Radiologist updates their profile
    public ClientDto updateRadiologistProfile(String username, UpdateUserDTO dto, MultipartFile universityCard) {
        Client radiologist = clientRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Radiologist not found"));

        if (dto.getFullName() != null && !dto.getFullName().isBlank()) {
            radiologist.setFullName(dto.getFullName());
        }
        if (dto.getEmail() != null && !dto.getEmail().isBlank()) {
            radiologist.setEmail(dto.getEmail());
        }
        if (dto.getPhone() != null && !dto.getPhone().isBlank()) {
            radiologist.setPhone(dto.getPhone());
        }

        if (universityCard != null && !universityCard.isEmpty()) {
            try {
                String fileName = UUID.randomUUID() + "_" + universityCard.getOriginalFilename();
                Path uploadPath = Paths.get("uploads/radiologists");
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }
                Path filePath = uploadPath.resolve(fileName);
                Files.copy(universityCard.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                radiologist.setUniversityCardImage("/uploads/radiologists/" + fileName);
            } catch (IOException e) {
                throw new RuntimeException("❌ Failed to save radiologist image", e);
            }
        }

        radiologist.setUpdatedAt(Instant.now());
        Client updated = clientRepository.save(radiologist);

        return clientMapper.toDTO(updated);
    }
}
