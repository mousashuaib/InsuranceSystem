package com.insurancesystem.Services;

import com.insurancesystem.Exception.NotFoundException;
import com.insurancesystem.Model.Dto.ClientDto;
import com.insurancesystem.Model.Dto.LabRequestDTO;
import com.insurancesystem.Model.Dto.UpdateUserDTO;
import com.insurancesystem.Model.Entity.Client;
import com.insurancesystem.Model.Entity.Enums.LabRequestStatus;
import com.insurancesystem.Model.Entity.Enums.RoleName;
import com.insurancesystem.Model.Entity.FamilyMember;
import com.insurancesystem.Model.Entity.LabRequest;
import com.insurancesystem.Model.Entity.PriceList;
import com.insurancesystem.Model.MapStruct.ClientMapper;
import com.insurancesystem.Model.MapStruct.LabRequestMapper;
import com.insurancesystem.Repository.ClientRepository;
import com.insurancesystem.Repository.FamilyMemberRepository;
import com.insurancesystem.Repository.LabRequestRepository;
import com.insurancesystem.Repository.PriceListRepository;
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
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LabRequestService {

    private final LabRequestRepository labRepo;
    private final ClientRepository clientRepo;
    private final FamilyMemberRepository familyMemberRepo;
    private final LabRequestMapper labRequestMapper;
    private final ClientMapper clientMapper;
    private final NotificationService notificationService;
    private final PriceListRepository priceListRepository;

    // ➕ Doctor ينشئ طلب فحص
    @Transactional
    public LabRequestDTO create(LabRequestDTO dto) {
        log.info("🔹 Starting lab request creation...");

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();

        // 🧑‍⚕️ الدكتور
        Client doctor = clientRepo.findByEmail(currentUsername.toLowerCase())
                .orElseThrow(() -> new NotFoundException("Doctor not found"));
        log.info("✅ Doctor found: {}", doctor.getFullName());
        
        return createLabRequestWithDoctor(dto, doctor);
    }
    
    // ✅ إنشاء طلب فحص من Medical Admin (للجداول التلقائية)
    @Transactional
    public LabRequestDTO createLabRequestWithDoctor(LabRequestDTO dto, Client doctor) {
        log.info("🔹 Starting lab request creation with doctor: {}...", doctor.getFullName());

        // 👤 المريض
        Client member;
        FamilyMember familyMember = null;
        String familyMemberInfo = "";

        if (dto.getMemberId() != null) {
            // First try to find as a Client
            Optional<Client> clientOpt = clientRepo.findById(dto.getMemberId());

            if (clientOpt.isPresent()) {
                member = clientOpt.get();
            } else {
                // If not found as Client, try to find as FamilyMember
                Optional<FamilyMember> familyMemberOpt = familyMemberRepo.findById(dto.getMemberId());

                if (familyMemberOpt.isPresent()) {
                    familyMember = familyMemberOpt.get();
                    // Get the main client (the family member's client)
                    member = familyMember.getClient();

                    // Calculate age from date of birth
                    int age = -1;
                    if (familyMember.getDateOfBirth() != null) {
                        java.time.LocalDate today = java.time.LocalDate.now();
                        java.time.LocalDate birthDate = familyMember.getDateOfBirth();
                        age = today.getYear() - birthDate.getYear();
                        if (today.getMonthValue() < birthDate.getMonthValue() ||
                                (today.getMonthValue() == birthDate.getMonthValue() && today.getDayOfMonth() < birthDate.getDayOfMonth())) {
                            age--;
                        }
                    }

                    // Store family member info for notes with age and gender
                    String ageStr = age > 0 ? age + " years" : "N/A";
                    String genderStr = familyMember.getGender() != null ? familyMember.getGender().toString() : "N/A";

                    familyMemberInfo = String.format(
                            "\nFamily Member: %s (%s) - Insurance: %s - Age: %s - Gender: %s",
                            familyMember.getFullName(),
                            familyMember.getRelation(),
                            familyMember.getInsuranceNumber(),
                            ageStr,
                            genderStr
                    );
                } else {
                    throw new NotFoundException("Member not found");
                }
            }
        } else if (dto.getMemberName() != null && !dto.getMemberName().isBlank()) {
            member = clientRepo.findByFullName(dto.getMemberName())
                    .orElseThrow(() -> new NotFoundException("Member not found"));
        } else {
            throw new RuntimeException("Member info required");
        }

        log.info("✅ Member found: {}", member.getFullName());

        // 🧪 الفحص
        PriceList test = priceListRepository.findById(dto.getTestId())
                .orElseThrow(() -> new NotFoundException("Test not found"));
        log.info("✅ Test found: {} (Union Price: {})", test.getServiceName(), test.getPrice());

        // 📝 بناء الطلب
        LabRequest request = labRequestMapper.toEntity(dto);
        request.setDoctor(doctor);
        request.setMember(member); // Always link to main client
        request.setTest(test);
        request.setTestName(test.getServiceName());

        // Add family member info to notes if applicable
        String notes = dto.getNotes() != null ? dto.getNotes() : "";
        if (familyMember != null && !familyMemberInfo.isEmpty()) {
            notes = notes.isEmpty()
                    ? familyMemberInfo.trim()
                    : notes + familyMemberInfo;
        }
        request.setNotes(notes);

        request.setStatus(LabRequestStatus.PENDING);
        request.setCreatedAt(Instant.now());
        request.setUpdatedAt(Instant.now());

        LabRequest saved = labRepo.save(request);
        log.info("✅ Lab request created: {} - Member ID: {}, Member Name: {}, Doctor: {}", 
                saved.getId(), saved.getMember().getId(), saved.getMember().getFullName(), saved.getDoctor().getFullName());
        
        // Store familyMember reference for later use (before detachment)
        final FamilyMember savedFamilyMember = familyMember;

        // 🔔 إشعار للمريض (main client)
        String memberNotification = savedFamilyMember != null
                ? String.format(
                "New lab test request created by Dr. %s for family member %s (%s) - Test: %s",
                doctor.getFullName(),
                savedFamilyMember.getFullName(),
                savedFamilyMember.getRelation(),
                test.getServiceName()
        )
                : "New lab test request created by Dr. " + doctor.getFullName() + " - Test: " + test.getServiceName();

        notificationService.sendToUser(
                member.getId(),
                memberNotification
        );
        log.info("✅ Notification sent to member");

        // 🔔 إشعار لجميع فنيي المختبر
        List<Client> labTechs = clientRepo.findByRoles_Name(RoleName.LAB_TECH);
        String labTechMessage = savedFamilyMember != null
                ? String.format(
                "New lab test request from Dr. %s for family member %s (%s) - Client: %s - Test: %s",
                doctor.getFullName(),
                savedFamilyMember.getFullName(),
                savedFamilyMember.getRelation(),
                member.getFullName(),
                test.getServiceName()
        )
                : "New lab test request from Dr. " + doctor.getFullName() +
                " for patient " + member.getFullName() +
                " - Test: " + test.getServiceName();

        for (Client labTech : labTechs) {
            notificationService.sendToUser(
                    labTech.getId(),
                    labTechMessage
            );
        }
        log.info("✅ Notifications sent to {} lab technicians", labTechs.size());

        // Convert to DTO and explicitly set family member information if applicable
        LabRequestDTO resultDto = labRequestMapper.toDto(saved, familyMemberRepo);
        
        // Explicitly set family member information in DTO (bypasses mapper's parsing)
        if (savedFamilyMember != null) {
            resultDto.setIsFamilyMember(true);
            resultDto.setFamilyMemberId(savedFamilyMember.getId()); // 🆕 Set family member ID
            resultDto.setFamilyMemberName(savedFamilyMember.getFullName());
            resultDto.setFamilyMemberRelation(savedFamilyMember.getRelation() != null ? savedFamilyMember.getRelation().toString() : null);
            resultDto.setFamilyMemberInsuranceNumber(savedFamilyMember.getInsuranceNumber());
            
            // Calculate age
            if (savedFamilyMember.getDateOfBirth() != null) {
                java.time.LocalDate today = java.time.LocalDate.now();
                java.time.LocalDate birthDate = savedFamilyMember.getDateOfBirth();
                int age = today.getYear() - birthDate.getYear();
                if (today.getMonthValue() < birthDate.getMonthValue() ||
                        (today.getMonthValue() == birthDate.getMonthValue() && today.getDayOfMonth() < birthDate.getDayOfMonth())) {
                    age--;
                }
                resultDto.setFamilyMemberAge(age > 0 ? age + " years" : null);
            } else {
                resultDto.setFamilyMemberAge(null);
            }
            
            // Set gender
            if (savedFamilyMember.getGender() != null) {
                resultDto.setFamilyMemberGender(savedFamilyMember.getGender().toString());
            } else {
                resultDto.setFamilyMemberGender(null);
            }
            
            log.info("✅ Family member information added to DTO: {} ({}) - Insurance: {}", 
                    savedFamilyMember.getFullName(), 
                    savedFamilyMember.getRelation(),
                    savedFamilyMember.getInsuranceNumber());
        } else {
            resultDto.setIsFamilyMember(false);
            log.info("✅ Main client request (not a family member)");
        }

        return resultDto;
    }

    // 📖 Doctor يشوف طلباته
    public List<LabRequestDTO> getByDoctor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();

        Client doctor = clientRepo.findByEmail(currentUsername.toLowerCase())
                .orElseThrow(() -> new NotFoundException("Doctor not found"));

        List<LabRequest> requests = labRepo.findByDoctorIdWithMember(doctor.getId());
        
        // Force initialization of member fields to ensure they're loaded (same as PrescriptionService)
        for (LabRequest r : requests) {
            if (r.getMember() != null) {
                Client member = r.getMember();
                // Force access to fields to trigger loading
                String name = member.getFullName();
                java.time.LocalDate dob = member.getDateOfBirth();
                String gender = member.getGender();
                String nationalId = member.getNationalId();
            }
        }
        
        return requests.stream()
                .map(r -> labRequestMapper.toDto(r, familyMemberRepo))
                .toList();
    }

    // 📖 Member يشوف طلباته
    public List<LabRequestDTO> getMyLabs() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();

        Client member = clientRepo.findByEmail(currentUsername.toLowerCase())
                .orElseThrow(() -> new NotFoundException("Member not found"));

        log.info("🔍 Fetching lab requests for member: {} (ID: {})", member.getFullName(), member.getId());
        
        List<LabRequest> labRequests = labRepo.findByMemberId(member.getId());
        
        log.info("✅ Found {} lab requests for member: {}", labRequests.size(), member.getFullName());
        for (LabRequest lr : labRequests) {
            log.info("  - Lab Request ID: {}, Doctor: {}, Status: {}, Test: {}", 
                    lr.getId(), 
                    lr.getDoctor() != null ? lr.getDoctor().getFullName() : "N/A",
                    lr.getStatus(),
                    lr.getTestName());
        }
        
        // Use eager fetch query to ensure member is loaded
        List<LabRequest> labRequestsWithMember = labRepo.findByMemberIdWithMember(member.getId());
        
        // Force initialization of member fields to ensure they're loaded (same as PrescriptionService)
        for (LabRequest r : labRequestsWithMember) {
            if (r.getMember() != null) {
                Client memberEntity = r.getMember();
                // Force access to fields to trigger loading
                String name = memberEntity.getFullName();
                java.time.LocalDate dob = memberEntity.getDateOfBirth();
                String gender = memberEntity.getGender();
                String nationalId = memberEntity.getNationalId();
            }
        }
        
        return labRequestsWithMember.stream()
                .map(r -> labRequestMapper.toDto(r, familyMemberRepo))
                .collect(Collectors.toList());
    }

    // 📖 Lab Tech يشوف الطلبات المعلقة
    public List<LabRequestDTO> getPending() {
        // Use query that eagerly fetches member with dateOfBirth and gender
        List<LabRequest> requests = labRepo.findByStatusWithMember(LabRequestStatus.PENDING);
        
        log.info("📋 [SERVICE] Found {} pending lab requests", requests.size());
        
        // Force initialization of member fields to ensure they're loaded (same as PrescriptionService)
        for (LabRequest r : requests) {
            if (r.getMember() != null) {
                Client member = r.getMember();
                // Force access to fields to trigger loading
                String name = member.getFullName();
                java.time.LocalDate dob = member.getDateOfBirth();
                String gender = member.getGender();
                String nationalId = member.getNationalId();
                log.info("👤 [SERVICE] Lab Request {} - Member: {} | DOB: {} | Gender: {} | NationalId: {}",
                        r.getId(), name, dob, gender, nationalId);
            } else {
                log.warn("⚠️ [SERVICE] Lab Request {} has null member", r.getId());
            }
        }
        
        List<LabRequestDTO> dtos = requests.stream()
                .map(r -> labRequestMapper.toDto(r, familyMemberRepo))
                .collect(Collectors.toList());
        
        // Verify DTOs have the data
        for (LabRequestDTO dto : dtos) {
            log.info("📦 [SERVICE] Lab Request DTO {} - memberAge: {} | memberGender: {} | memberNationalId: {}",
                    dto.getId(), dto.getMemberAge(), dto.getMemberGender(), dto.getMemberNationalId());
        }
        
        return dtos;
    }

    // 🧪 Lab Tech يرفع النتيجة والسعر
    @Transactional
    public LabRequestDTO uploadResult(UUID id, MultipartFile file, Double enteredPrice) {
        log.info("🔹 Lab Tech uploading result for request: {}", id);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();

        Client labTech = clientRepo.findByEmail(currentUsername.toLowerCase())
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
            Double unionPrice = request.getTest().getPrice(); // ← السعر النقابي من PriceList
            Double approvedPrice;

            if (enteredPrice < unionPrice) {
                approvedPrice = enteredPrice;
            } else {
                approvedPrice = unionPrice;
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

        // 🔔 إشعار للمريض بإكمال الفحص
        notificationService.sendToUser(
                saved.getMember().getId(),
                "✅ تم إكمال فحص " + saved.getTest().getServiceName() +
                        " - السعر: " + saved.getApprovedPrice() + " شيكل"
        );
        log.info("✅ Notification sent to member");

        // 🔔 إشعار للطبيب بإكمال الفحص
        notificationService.sendToUser(
                saved.getDoctor().getId(),
                "✅ تم إكمال فحص " + saved.getTest().getServiceName() +
                        " للمريض " + saved.getMember().getFullName()
        );
        log.info("✅ Notification sent to doctor");

        return labRequestMapper.toDto(saved, familyMemberRepo);
    }

    // 📖 Member أو Doctor يشوف نتيجة فحص
    public LabRequestDTO getResult(UUID id) {
        // Use query that eagerly fetches member with dateOfBirth and gender
        LabRequest request = labRepo.findByIdWithMember(id)
                .orElseThrow(() -> new NotFoundException("Lab request not found"));

        return labRequestMapper.toDto(request, familyMemberRepo);
    }

    // ✏️ Doctor يعدل طلب
    @Transactional
    public LabRequestDTO update(UUID id, LabRequestDTO dto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();

        Client doctor = clientRepo.findByEmail(currentUsername.toLowerCase())
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

        return labRequestMapper.toDto(labRepo.save(request), familyMemberRepo);
    }

    // ❌ Doctor يحذف طلب
    @Transactional
    public void delete(UUID id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();

        Client doctor = clientRepo.findByEmail(currentUsername.toLowerCase())
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

        Client labWorker = clientRepo.findByEmail(currentUsername.toLowerCase())
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

        Client labTech = clientRepo.findByEmail(currentUsername.toLowerCase())
                .orElseThrow(() -> new NotFoundException("Lab worker not found"));

        // Use query that eagerly fetches member with dateOfBirth and gender
        List<LabRequest> requests = labRepo.findByLabTechIdWithMember(labTech.getId());
        
        // Force initialization of member fields to ensure they're loaded (same as PrescriptionService)
        for (LabRequest r : requests) {
            if (r.getMember() != null) {
                Client member = r.getMember();
                // Force access to fields to trigger loading
                String name = member.getFullName();
                java.time.LocalDate dob = member.getDateOfBirth();
                String gender = member.getGender();
                String nationalId = member.getNationalId();
            }
        }
        
        return requests.stream()
                .map(r -> labRequestMapper.toDto(r, familyMemberRepo))
                .collect(Collectors.toList());
    }

    // 👤 Lab Worker يحدّث بروفايله
    @Transactional
    public ClientDto updateLabWorkerProfile(String username, UpdateUserDTO dto, MultipartFile[] universityCard){
        Client labWorker = clientRepo.findByEmail(username.toLowerCase())
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

        if (universityCard != null && universityCard.length > 0) {
            try {
                String uploadDir = "uploads/labworkers";
                Files.createDirectories(Paths.get(uploadDir));

                // تأكد الليست مش null
                if (labWorker.getUniversityCardImages() == null) {
                    labWorker.setUniversityCardImages(new java.util.ArrayList<>());
                }

                for (MultipartFile file : universityCard) {
                    if (file == null || file.isEmpty()) continue;

                    String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
                    Path filePath = Paths.get(uploadDir, fileName);

                    Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                    // خزّن المسار بالـ DB
                    labWorker.getUniversityCardImages().add("/" + uploadDir + "/" + fileName);
                }

            } catch (IOException e) {
                throw new RuntimeException("❌ Failed to save lab worker images", e);
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

