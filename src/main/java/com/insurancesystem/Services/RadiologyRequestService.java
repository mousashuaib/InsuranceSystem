package com.insurancesystem.Services;

import com.insurancesystem.Exception.NotFoundException;
import com.insurancesystem.Model.Dto.ClientDto;
import com.insurancesystem.Model.Dto.RadiologyRequestDTO;
import com.insurancesystem.Model.Dto.UpdateUserDTO;
import com.insurancesystem.Model.Entity.Client;
import com.insurancesystem.Model.Entity.Enums.RoleName;
import com.insurancesystem.Model.Entity.RadiologyRequest;
import com.insurancesystem.Model.Entity.Enums.LabRequestStatus;
import com.insurancesystem.Model.Entity.PriceList;
import com.insurancesystem.Model.MapStruct.ClientMapper;
import com.insurancesystem.Model.MapStruct.RadiologyRequestMapper;
import com.insurancesystem.Repository.ClientRepository;
import com.insurancesystem.Repository.PriceListRepository;
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
    private final PriceListRepository priceListRepository; // 🆕 ربط PriceList
    private final RadiologyRequestMapper radiologyRequestMapper;
    private final ClientMapper clientMapper;
    private final NotificationService notificationService;

    // 🧑‍⚕️ Get the radiologist's ID from the username
    public UUID getRadiologistIdByUsername(String username) {
        Client radiologist = clientRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Radiologist not found"));
        return radiologist.getId();
    }

    // ➕ Create a new Radiology Request (linked with PriceList)
    @Transactional
    public RadiologyRequestDTO create(RadiologyRequestDTO dto) {
        log.info("🔹 Creating radiology request...");

        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        Client doctor = clientRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Doctor not found"));

        Client member = clientRepository.findById(dto.getMemberId())
                .orElseThrow(() -> new NotFoundException("Member not found"));

        // 🆕 جلب خدمة الأشعة من PriceList
        PriceList test = priceListRepository.findById(dto.getTestId())
                .orElseThrow(() -> new NotFoundException("Radiology test not found"));

        // 📝 إنشاء الطلب
        RadiologyRequest request = radiologyRequestMapper.toEntity(dto);
        request.setDoctor(doctor);
        request.setMember(member);
        request.setRadiologist(null);
        request.setTest(test);               // 🆕 ربط PriceList
        request.setTestName(test.getServiceName()); // 🆕 اسم الفحص
        request.setStatus(LabRequestStatus.PENDING);
        request.setCreatedAt(Instant.now());
        request.setUpdatedAt(Instant.now());

        RadiologyRequest savedRequest = radiologyRequestRepository.save(request);

        // 🔔 إشعار للراديولوجيين
        clientRepository.findByRoles_Name(RoleName.RADIOLOGIST)
                .forEach(r -> notificationService.sendToUser(
                        r.getId(),
                        "📋 لديك طلب فحص إشعاعي جديد من الدكتور " + doctor.getFullName()
                ));

        // 🔔 إشعار للمريض
        notificationService.sendToUser(
                member.getId(),
                "📊 تم إنشاء طلب فحص إشعاعي جديد بواسطة الدكتور " + doctor.getFullName()
        );

        return radiologyRequestMapper.toDto(savedRequest);
    }

    // 📖 Radiologist views pending radiology requests
    public List<RadiologyRequestDTO> getPendingRequests(UUID radiologistId) {
        return radiologyRequestRepository.findByStatus(LabRequestStatus.PENDING)
                .stream()
                .map(radiologyRequestMapper::toDto)
                .collect(Collectors.toList());
    }

    // 🧪 Radiologist uploads radiology result with price calculation
    @Transactional
    public RadiologyRequestDTO uploadRadiologyResult(UUID requestId, MultipartFile file, String testName, Double enteredPrice) {
        log.info("🔹 Uploading radiology result...");

        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        Client radiologist = clientRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Radiologist not found"));

        RadiologyRequest request = radiologyRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Radiology request not found"));

        if (request.getStatus() == LabRequestStatus.COMPLETED) {
            throw new RuntimeException("Result already uploaded");
        }

        try {
            String dir = "uploads/radiology_results";
            Files.createDirectories(Paths.get(dir));

            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path path = Paths.get(dir, fileName);

            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

            request.setResultUrl("/" + dir + "/" + fileName);

            // 🆕 السعر النقابي من PriceList
            double unionPrice = request.getTest().getPrice();
            double approvedPrice = Math.min(enteredPrice, unionPrice);

            request.setTestName(testName);
            request.setEnteredPrice(enteredPrice);
            request.setApprovedPrice(approvedPrice); // 🆕 السعر المعتمد
            request.setStatus(LabRequestStatus.COMPLETED);
            request.setRadiologist(radiologist);
            request.setUpdatedAt(Instant.now());

        } catch (IOException e) {
            throw new RuntimeException("Failed to upload radiology result", e);
        }

        RadiologyRequest saved = radiologyRequestRepository.save(request);

        // 🔔 إشعار للمريض
        notificationService.sendToUser(
                saved.getMember().getId(),
                "✅ تم إكمال فحص الأشعة: " + saved.getTestName() +
                        " - السعر المعتمد: " + saved.getApprovedPrice()
        );

        // 🔔 إشعار للطبيب
        notificationService.sendToUser(
                saved.getDoctor().getId(),
                "✅ تم إكمال فحص الأشعة للمريض " + saved.getMember().getFullName()
        );

        return radiologyRequestMapper.toDto(saved);
    }

    // 📖 Member or Doctor views the result
    public RadiologyRequestDTO getResult(UUID id) {
        RadiologyRequest request = radiologyRequestRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Radiology request not found"));
        return radiologyRequestMapper.toDto(request);
    }

    // ✏️ Doctor updates radiology request notes
    @Transactional
    public RadiologyRequestDTO update(UUID id, RadiologyRequestDTO dto) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        Client doctor = clientRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Doctor not found"));

        RadiologyRequest request = radiologyRequestRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Radiology request not found"));

        if (!request.getDoctor().getId().equals(doctor.getId()))
            throw new RuntimeException("Not allowed");

        if (request.getStatus() == LabRequestStatus.COMPLETED)
            throw new RuntimeException("Cannot update completed request");

        request.setNotes(dto.getNotes());
        request.setUpdatedAt(Instant.now());

        return radiologyRequestMapper.toDto(radiologyRequestRepository.save(request));
    }

    // ❌ Doctor deletes radiology request
    @Transactional
    public void delete(UUID id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        Client doctor = clientRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Doctor not found"));

        RadiologyRequest request = radiologyRequestRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Radiology request not found"));

        if (!request.getDoctor().getId().equals(doctor.getId()))
            throw new RuntimeException("Not allowed");

        if (request.getStatus() == LabRequestStatus.COMPLETED)
            throw new RuntimeException("Cannot delete completed request");

        radiologyRequestRepository.delete(request);
    }

    // 📖 Doctor views all his radiology requests
    public List<RadiologyRequestDTO> getByDoctor() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        Client doctor = clientRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Doctor not found"));

        return radiologyRequestRepository.findByDoctorId(doctor.getId())
                .stream()
                .map(radiologyRequestMapper::toDto)
                .collect(Collectors.toList());
    }

    // 📊 Radiologist stats
    public RadiologyRequestDTO getRadiologyStats(UUID radiologistId) {
        long pending = radiologyRequestRepository.findByStatus(LabRequestStatus.PENDING).size();
        long completed = radiologyRequestRepository.countByStatusAndRadiologistId(LabRequestStatus.COMPLETED, radiologistId);

        return RadiologyRequestDTO.builder()
                .pending(pending)
                .completed(completed)
                .total(pending + completed)
                .build();
    }

    // 📖 Radiologist views his completed requests
    public List<RadiologyRequestDTO> getAllForCurrentRadiologist(UUID radiologistId) {
        return radiologyRequestRepository.findByRadiologistId(radiologistId)
                .stream()
                .map(radiologyRequestMapper::toDto)
                .collect(Collectors.toList());
    }

    // 📖 Member views his radiology requests
    public List<RadiologyRequestDTO> getByMember() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        Client member = clientRepository.findByUsername(username)
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

    // 👤 Radiologist updates profile
    @Transactional
    public ClientDto updateRadiologistProfile(String username, UpdateUserDTO dto, MultipartFile universityCard) {

        Client radiologist = clientRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Radiologist not found"));

        if (dto.getFullName() != null) radiologist.setFullName(dto.getFullName());
        if (dto.getEmail() != null) radiologist.setEmail(dto.getEmail());
        if (dto.getPhone() != null) radiologist.setPhone(dto.getPhone());

        if (universityCard != null && !universityCard.isEmpty()) {
            try {
                String fileName = UUID.randomUUID() + "_" + universityCard.getOriginalFilename();
                Path uploadPath = Paths.get("uploads/radiologists");
                Files.createDirectories(uploadPath);
                Files.copy(universityCard.getInputStream(), uploadPath.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);

                radiologist.setUniversityCardImage("/uploads/radiologists/" + fileName);

            } catch (IOException e) {
                throw new RuntimeException("Failed to save radiologist image", e);
            }
        }

        radiologist.setUpdatedAt(Instant.now());
        return clientMapper.toDTO(clientRepository.save(radiologist));
    }
}
