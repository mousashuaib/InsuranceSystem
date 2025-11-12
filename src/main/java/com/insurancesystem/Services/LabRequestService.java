package com.insurancesystem.Services;

import com.insurancesystem.Exception.NotFoundException;
import com.insurancesystem.Model.Dto.ClientDto;
import com.insurancesystem.Model.Dto.LabRequestDTO;
import com.insurancesystem.Model.Dto.UpdateUserDTO;
import com.insurancesystem.Model.Entity.Client;
import com.insurancesystem.Model.Entity.Enums.LabRequestStatus;
import com.insurancesystem.Model.Entity.Enums.RoleName;
import com.insurancesystem.Model.Entity.LabRequest;
import com.insurancesystem.Model.Entity.Test;
import com.insurancesystem.Model.MapStruct.ClientMapper;
import com.insurancesystem.Model.MapStruct.LabRequestMapper;
import com.insurancesystem.Repository.ClientRepository;
import com.insurancesystem.Repository.LabRequestRepository;
import com.insurancesystem.Repository.TestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
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
public class LabRequestService {

    private final LabRequestRepository labRepo;
    private final TestRepository testRepository;
    private final ClientRepository clientRepo;
    private final LabRequestMapper labRequestMapper;
    private final ClientMapper clientMapper;
    private final NotificationService notificationService;

    // ➕ Doctor ينشئ طلب فحص
    @Transactional
    public LabRequestDTO create(LabRequestDTO dto) {
        log.info("🔹 Starting lab request creation...");

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();

        // 🧑‍⚕️ الدكتور
        Client doctor = clientRepo.findByUsername(currentUsername)
                .orElseThrow(() -> new NotFoundException("Doctor not found"));
        log.info("✅ Doctor found: {}", doctor.getFullName());

        // 👤 المريض
        Client member;
        if (dto.getMemberId() != null) {
            member = clientRepo.findById(dto.getMemberId())
                    .orElseThrow(() -> new NotFoundException("Member not found"));
        } else if (dto.getMemberName() != null && !dto.getMemberName().isBlank()) {
            member = clientRepo.findByFullName(dto.getMemberName())
                    .orElseThrow(() -> new NotFoundException("Member not found"));
        } else {
            throw new RuntimeException("Member info required");
        }
        log.info("✅ Member found: {}", member.getFullName());

        // 🧪 الفحص
        Test test;
        if (dto.getTestId() != null) {
            test = testRepository.findById(dto.getTestId())
                    .orElseThrow(() -> new NotFoundException("Test not found"));
        } else if (dto.getTestName_test() != null && !dto.getTestName_test().isBlank()) {
            test = testRepository.findByTestName(dto.getTestName_test())
                    .orElseThrow(() -> new NotFoundException("Test not found with name: " + dto.getTestName_test()));
        } else {
            throw new RuntimeException("Test info required");
        }
        log.info("✅ Test found: {} (Union Price: {})", test.getTestName(), test.getUnionPrice());

        // 📝 بناء الطلب
        LabRequest request = labRequestMapper.toEntity(dto);
        request.setDoctor(doctor);
        request.setMember(member);
        request.setTest(test);
        request.setTestName(test.getTestName());
        request.setStatus(LabRequestStatus.PENDING);
        request.setCreatedAt(Instant.now());
        request.setUpdatedAt(Instant.now());

        LabRequest saved = labRepo.save(request);
        log.info("✅ Lab request created: {}", saved.getId());

        // 🔔 إشعار للمريض
        notificationService.sendToUser(
                member.getId(),
                "تم إنشاء طلب فحص جديد من الدكتور " + doctor.getFullName()
        );
        log.info("✅ Notification sent to member");

        // 🔔 إشعار لجميع فنيي المختبر
        List<Client> labTechs = clientRepo.findByRoles_Name(RoleName.LAB_TECH);
        for (Client labTech : labTechs) {
            notificationService.sendToUser(
                    labTech.getId(),
                    "طلب فحص جديد من الدكتور " + doctor.getFullName() +
                            " للمريض " + member.getFullName() +
                            " - الفحص: " + test.getTestName()
            );
        }
        log.info("✅ Notifications sent to {} lab technicians", labTechs.size());

        return labRequestMapper.toDto(saved);
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

    // 🧪 Lab Tech يرفع النتيجة والسعر
    @Transactional
    public LabRequestDTO uploadResult(UUID id, MultipartFile file, Double enteredPrice) {
        log.info("🔹 Lab Tech uploading result for request: {}", id);

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
            request.setEnteredPrice(enteredPrice);

            // 🟢 حساب السعر المعتمد
            Double unionPrice = request.getTest().getUnionPrice();
            Double approvedPrice;

            if (enteredPrice < unionPrice) {
                approvedPrice = enteredPrice;
                log.info("✅ Entered price ({}) is less than union price ({}), approved", enteredPrice, unionPrice);
            } else {
                approvedPrice = unionPrice;
                log.info("⚠️ Entered price ({}) is >= union price ({}), using union price", enteredPrice, unionPrice);
            }

            request.setApprovedPrice(approvedPrice);
            request.setStatus(LabRequestStatus.COMPLETED);
            request.setLabTech(labTech);
            request.setUpdatedAt(Instant.now());

        } catch (Exception e) {
            log.error("❌ Failed to upload file: {}", e.getMessage());
            throw new RuntimeException("Failed to upload file", e);
        }

        LabRequest saved = labRepo.save(request);
        log.info("✅ Result uploaded successfully. Approved Price: {}", saved.getApprovedPrice());

        return labRequestMapper.toDto(saved);
    }

    // 📖 Member أو Doctor يشوف نتيجة فحص
    public LabRequestDTO getResult(UUID id) {
        LabRequest request = labRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Lab request not found"));

        return labRequestMapper.toDto(request);
    }

    // ✏️ Doctor يعدل طلب
    @Transactional
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

        request.setNotes(dto.getNotes());
        request.setUpdatedAt(Instant.now());

        return labRequestMapper.toDto(labRepo.save(request));
    }

    // ❌ Doctor يحذف طلب
    @Transactional
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

        Client labWorker = clientRepo.findByUsername(currentUsername)
                .orElseThrow(() -> new NotFoundException("Lab worker not found"));

        long pending = labRepo.countByStatusAndLabTechId(LabRequestStatus.PENDING, labWorker.getId());
        long completed = labRepo.countByStatusAndLabTechId(LabRequestStatus.COMPLETED, labWorker.getId());
        long total = pending + completed;

        return LabRequestDTO.builder()
                .total(total)
                .pending(pending)
                .completed(completed)
                .build();
    }

    // 📖 Lab Tech يشوف كل طلباته
    public List<LabRequestDTO> getAllForCurrentLab() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();

        Client labTech = clientRepo.findByUsername(currentUsername)
                .orElseThrow(() -> new NotFoundException("Lab worker not found"));

        return labRepo.findByLabTechId(labTech.getId())
                .stream()
                .map(labRequestMapper::toDto)
                .collect(Collectors.toList());
    }

    // 👤 Lab Worker يحدّث بروفايله
    @Transactional
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

    // 📖 إرجاع كل الفنيين (Lab Technicians)
    public List<ClientDto> getAllLabTechs() {
        return clientRepo.findByRoles_Name(RoleName.LAB_TECH)
                .stream()
                .map(clientMapper::toDTO)
                .collect(Collectors.toList());
    }

}
