package com.insurancesystem.Services;

import com.insurancesystem.Exception.NotFoundException;
import com.insurancesystem.Model.Dto.ClientDto;
import com.insurancesystem.Model.Dto.LabRequestDTO;
import com.insurancesystem.Model.Dto.UpdateUserDTO;
import com.insurancesystem.Model.Entity.Client;
import com.insurancesystem.Model.Entity.Enums.LabRequestStatus;
import com.insurancesystem.Model.Entity.Enums.RoleName;
import com.insurancesystem.Model.Entity.LabRequest;
import com.insurancesystem.Model.MapStruct.ClientMapper;
import com.insurancesystem.Model.MapStruct.LabRequestMapper;
import com.insurancesystem.Repository.ClientRepository;
import com.insurancesystem.Repository.LabRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
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
public class LabRequestService {

    private final LabRequestRepository labRepo;
    private final ClientRepository clientRepo;
    private final LabRequestMapper labRequestMapper;
    private final ClientMapper clientMapper;
    private final NotificationService notificationService;

    // ➕ Doctor ينشئ طلب فحص
    public LabRequestDTO create(LabRequestDTO dto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();

        Client doctor = clientRepo.findByUsername(currentUsername)
                .orElseThrow(() -> new NotFoundException("Doctor not found"));

        Client member;
        if (dto.getMemberId() != null) {
            member = clientRepo.findById(dto.getMemberId())
                    .orElseThrow(() -> new NotFoundException("Member not found"));
        } else if (dto.getMemberName() != null) {
            member = clientRepo.findByFullName(dto.getMemberName())
                    .orElseThrow(() -> new NotFoundException("Member not found"));
        } else {
            throw new RuntimeException("Member info required");
        }

        LabRequest request = labRequestMapper.toEntity(dto);
        request.setDoctor(doctor);
        request.setMember(member);
        request.setStatus(LabRequestStatus.PENDING);
        request.setCreatedAt(Instant.now());
        request.setUpdatedAt(Instant.now());

        return labRequestMapper.toDto(labRepo.save(request));
    }
    // 📖 Doctor يشوف طلباته
    public List<LabRequestDTO> getByDoctor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();

        Client doctor = clientRepo.findByUsername(currentUsername)
                .orElseThrow(() -> new NotFoundException("Doctor not found"));

        return labRepo.findByDoctorId(doctor.getId())
                .stream()
                .map(labRequestMapper::toDto)
                .toList();
    }

    // 📖 Member يشوف طلباته
    public List<LabRequestDTO> getMyLabs() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();

        Client member = clientRepo.findByUsername(currentUsername)
                .orElseThrow(() -> new NotFoundException("Member not found"));

        return labRepo.findByMemberId(member.getId())
                .stream()
                .map(labRequestMapper::toDto)
                .collect(Collectors.toList());
    }

    // 📖 Lab Tech يشوف الطلبات المعلقة
    public List<LabRequestDTO> getPending() {
        return labRepo.findByStatus(LabRequestStatus.PENDING)
                .stream()
                .map(labRequestMapper::toDto)
                .collect(Collectors.toList());
    }

    public LabRequestDTO uploadResult(UUID id, MultipartFile file) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();
        Client labTech = clientRepo.findByUsername(currentUsername)
                .orElseThrow(() -> new NotFoundException("Lab worker not found"));

        LabRequest request = labRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Lab request not found"));

        if (request.getStatus() == LabRequestStatus.COMPLETED) {
            throw new RuntimeException("Result already uploaded");
        }

        try {
            String uploadDir = "uploads/labs";
            Files.createDirectories(Paths.get(uploadDir));

            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path filePath = Paths.get(uploadDir, fileName);

            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            request.setResultUrl("/" + uploadDir + "/" + fileName);
            request.setStatus(LabRequestStatus.COMPLETED);
            request.setLabTech(labTech); // 🟢 لازم تحفظ مين اللابر
            request.setUpdatedAt(Instant.now());

        } catch (Exception e) {
            throw new RuntimeException("Failed to upload file", e);
        }

        return labRequestMapper.toDto(labRepo.save(request));
    }

    // 📖 Member أو Doctor يشوف نتيجة فحص
    public LabRequestDTO getResult(UUID id) {
        LabRequest request = labRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Lab request not found"));

        return labRequestMapper.toDto(request);
    }

    // ✏️ Doctor يعدل طلب
    public LabRequestDTO update(UUID id, LabRequestDTO dto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();

        Client doctor = clientRepo.findByUsername(currentUsername)
                .orElseThrow(() -> new NotFoundException("Doctor not found"));

        LabRequest request = labRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Lab request not found"));

        if (!request.getDoctor().getId().equals(doctor.getId())) {
            throw new RuntimeException("You are not allowed to update this request");
        }

        if (request.getStatus() == LabRequestStatus.COMPLETED) {
            throw new RuntimeException("Cannot update a completed lab request");
        }

        request.setTestName(dto.getTestName());
        request.setNotes(dto.getNotes());
        request.setUpdatedAt(Instant.now());

        return labRequestMapper.toDto(labRepo.save(request));
    }

    // ❌ Doctor يحذف طلب
    public void delete(UUID id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();

        Client doctor = clientRepo.findByUsername(currentUsername)
                .orElseThrow(() -> new NotFoundException("Doctor not found"));

        LabRequest request = labRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Lab request not found"));

        if (!request.getDoctor().getId().equals(doctor.getId())) {
            throw new RuntimeException("You are not allowed to delete this request");
        }

        if (request.getStatus() == LabRequestStatus.COMPLETED) {
            throw new RuntimeException("Cannot delete a completed lab request");
        }

        labRepo.delete(request);
    }

    // 📊 Lab Technician يشوف إحصائيات الطلبات
    public LabRequestDTO getLabStats() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();

        // جيب اليوزر (lab worker الحالي)
        Client labWorker = clientRepo.findByUsername(currentUsername)
                .orElseThrow(() -> new NotFoundException("Lab worker not found"));

        long pending = labRepo.countByStatus(LabRequestStatus.PENDING);
        long completed = labRepo.countByStatusAndLabTechId(LabRequestStatus.COMPLETED, labWorker.getId());

        // المجموع = pending + completed
        long total = pending + completed;

        return LabRequestDTO.builder()
                .total(total)
                .pending(pending)
                .completed(completed)
                .build();
    }



    // 👤 Lab Worker يحدّث بروفايله
    public ClientDto updateLabWorkerProfile(String username, UpdateUserDTO dto, MultipartFile universityCard) {
        Client labWorker = clientRepo.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Lab worker not found"));

        if (dto.getFullName() != null && !dto.getFullName().isBlank()) {
            labWorker.setFullName(dto.getFullName());
        }
        if (dto.getEmail() != null && !dto.getEmail().isBlank()) {
            labWorker.setEmail(dto.getEmail());
        }
        if (dto.getPhone() != null && !dto.getPhone().isBlank()) {
            labWorker.setPhone(dto.getPhone());
        }

        if (universityCard != null && !universityCard.isEmpty()) {
            try {
                String fileName = UUID.randomUUID() + "_" + universityCard.getOriginalFilename();
                Path uploadPath = Paths.get("uploads/labworkers");
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }
                Path filePath = uploadPath.resolve(fileName);
                Files.copy(universityCard.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                labWorker.setUniversityCardImage("/uploads/labworkers/" + fileName);
            } catch (IOException e) {
                throw new RuntimeException("❌ Failed to save lab worker image", e);
            }
        }

        labWorker.setUpdatedAt(Instant.now());
        Client updated = clientRepo.save(labWorker);

        return clientMapper.toDTO(updated);
    }

}
