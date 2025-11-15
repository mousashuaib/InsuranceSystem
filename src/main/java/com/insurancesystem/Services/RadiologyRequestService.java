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
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RadiologyRequestService {

    private final RadiologistRepository radiologyRequestRepository;
    private final ClientRepository clientRepository;
    private final RadiologyRequestMapper radiologyRequestMapper;
    private final ClientMapper clientMapper;
    private final NotificationService notificationService;

    // 🧑‍⚕️ Get the radiologist's ID from the username
    public UUID getRadiologistIdByUsername(String username) {
        Client radiologist = clientRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Radiologist not found"));
        return radiologist.getId();
    }

    // ➕ Create a new Radiology Request (Single Request - NOT duplicated for each radiologist)
    @Transactional
    public RadiologyRequestDTO create(RadiologyRequestDTO dto) {
        log.info("🔹 Creating a single radiology request...");

        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();

        // 🧑‍⚕️ Get the doctor
        Client doctor = clientRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new NotFoundException("Doctor not found"));
        log.info("✅ Doctor found: {}", doctor.getFullName());

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
        log.info("✅ Member found: {}", member.getFullName());

        // 📝 إنشاء طلب واحد فقط (بدون تخصيص لراديولوجي معين في البداية)
        RadiologyRequest request = radiologyRequestMapper.toEntity(dto);
        request.setDoctor(doctor);
        request.setMember(member);
        request.setRadiologist(null); // سيتم تعيين الراديولوجي عند الرد
        request.setStatus(LabRequestStatus.PENDING);
        request.setCreatedAt(Instant.now());
        request.setUpdatedAt(Instant.now());

        RadiologyRequest savedRequest = radiologyRequestRepository.save(request);
        log.info("✅ Radiology request created - Request ID: {}", savedRequest.getId());

        // 🧑‍🔬 جلب جميع الراديولوجيين لإرسال إشعارات
        List<Client> allRadiologists = clientRepository.findByRoles_Name(RoleName.RADIOLOGIST);

        // 🔔 إرسال إشعار لجميع الراديولوجيين
        for (Client radiologist : allRadiologists) {
            notificationService.sendToUser(
                    radiologist.getId(),
                    "📋 لديك طلب فحص إشعاعي جديد من الدكتور " + doctor.getFullName() +
                            " للمريض " + member.getFullName()
            );
        }

        // 🔔 إرسال إشعار للمريض
        notificationService.sendToUser(
                member.getId(),
                "📊 تم إنشاء طلب فحص إشعاعي جديد بواسطة الدكتور " + doctor.getFullName()
        );

        log.info("✅ Radiology request created successfully");
        return radiologyRequestMapper.toDto(savedRequest);
    }

    // 📖 Radiologist views pending radiology requests (all unassigned requests)
    public List<RadiologyRequestDTO> getPendingRequests(UUID radiologistId) {
        // جلب جميع الطلبات المعلقة (سواء كانت null radiologist أو لم تكتمل بعد)
        return radiologyRequestRepository.findByStatus(LabRequestStatus.PENDING)
                .stream()
                .map(radiologyRequestMapper::toDto)
                .collect(Collectors.toList());
    }

    // 🧪 Radiologist uploads radiology result with test name and price
    @Transactional
    public RadiologyRequestDTO uploadRadiologyResult(UUID requestId, MultipartFile file, String testName, Double enteredPrice) {
        log.info("🔹 Radiologist uploading result for request: {}", requestId);

        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        Client radiologist = clientRepository.findByUsername(currentUsername)
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
            request.setTestName(testName);
            request.setEnteredPrice(enteredPrice);
            request.setStatus(LabRequestStatus.COMPLETED);
            request.setRadiologist(radiologist);
            request.setUpdatedAt(Instant.now());

            log.info("✅ Radiology result uploaded successfully. Test: {}, Price: {}", testName, enteredPrice);

        } catch (IOException e) {
            log.error("❌ Failed to upload radiology result: {}", e.getMessage());
            throw new RuntimeException("Failed to upload radiology result", e);
        }

        RadiologyRequest saved = radiologyRequestRepository.save(request);

        // 🔔 إرسال إشعار للمريض عند إكمال الفحص
        notificationService.sendToUser(
                saved.getMember().getId(),
                "✅ تم إكمال فحص الأشعة: " + testName + " - السعر: " + enteredPrice + " د.ك"
        );

        // 🔔 إرسال إشعار للطبيب عند إكمال الفحص
        notificationService.sendToUser(
                saved.getDoctor().getId(),
                "✅ تم إكمال فحص الأشعة للمريض " + saved.getMember().getFullName() + ": " + testName
        );

        return radiologyRequestMapper.toDto(saved);
    }

    // 📖 Member or Doctor views the result
    public RadiologyRequestDTO getResult(UUID id) {
        RadiologyRequest request = radiologyRequestRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Radiology request not found"));

        return radiologyRequestMapper.toDto(request);
    }

    // ✏️ Doctor updates the radiology request
    @Transactional
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

        request.setNotes(dto.getNotes());
        request.setUpdatedAt(Instant.now());

        return radiologyRequestMapper.toDto(radiologyRequestRepository.save(request));
    }

    // ❌ Doctor deletes a radiology request
    @Transactional
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

    // 📊 Radiologist views stats
    public RadiologyRequestDTO getRadiologyStats(UUID radiologistId) {
        // الطلبات المعلقة (كل طلب جديد لم يتم إكماله)
        long pending = radiologyRequestRepository.findByStatus(LabRequestStatus.PENDING).size();
        // الطلبات المكتملة من قبل هذا الراديولوجي
        long completed = radiologyRequestRepository.countByStatusAndRadiologistId(LabRequestStatus.COMPLETED, radiologistId);
        long total = pending + completed;

        return RadiologyRequestDTO.builder()
                .total(total)
                .pending(pending)
                .completed(completed)
                .build();
    }

    // 📖 Radiologist views all their requests (completed requests where they are assigned)
    public List<RadiologyRequestDTO> getAllForCurrentRadiologist(UUID radiologistId) {
        return radiologyRequestRepository.findByRadiologistId(radiologistId)
                .stream()
                .map(radiologyRequestMapper::toDto)
                .collect(Collectors.toList());
    }

    // 📖 Member views their radiology requests
    public List<RadiologyRequestDTO> getByMember() {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        Client member = clientRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new NotFoundException("Member not found"));
        return radiologyRequestRepository.findByMemberId(member.getId())
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
    @Transactional
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
                throw new RuntimeException("Failed to save radiologist image", e);
            }
        }

        radiologist.setUpdatedAt(Instant.now());
        Client updated = clientRepository.save(radiologist);

        return clientMapper.toDTO(updated);
    }
}