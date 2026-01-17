package com.insurancesystem.Services;

import com.insurancesystem.Exception.NotFoundException;
import com.insurancesystem.Model.Dto.ClientDto;
import com.insurancesystem.Model.Dto.RadiologyRequestDTO;
import com.insurancesystem.Model.Dto.UpdateUserDTO;
import com.insurancesystem.Model.Entity.Client;
import com.insurancesystem.Model.Entity.Enums.RoleName;
import com.insurancesystem.Model.Entity.FamilyMember;
import com.insurancesystem.Model.Entity.RadiologyRequest;
import com.insurancesystem.Model.Entity.Enums.LabRequestStatus;
import com.insurancesystem.Model.Entity.PriceList;
import com.insurancesystem.Model.MapStruct.ClientMapper;
import com.insurancesystem.Model.MapStruct.RadiologyRequestMapper;
import com.insurancesystem.Repository.ClientRepository;
import com.insurancesystem.Repository.FamilyMemberRepository;
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
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RadiologyRequestService {

    private final RadiologistRepository radiologyRequestRepository;
    private final ClientRepository clientRepository;
    private final FamilyMemberRepository familyMemberRepo;
    private final PriceListRepository priceListRepository; // 🆕 ربط PriceList
    private final RadiologyRequestMapper radiologyRequestMapper;
    private final ClientMapper clientMapper;
    private final NotificationService notificationService;

    public UUID getRadiologistIdByEmail(String email) {
        Client radiologist = clientRepository.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new NotFoundException("Radiologist not found"));
        return radiologist.getId();
    }

    // ➕ Create a new Radiology Request (linked with PriceList)
    @Transactional
    public RadiologyRequestDTO create(RadiologyRequestDTO dto) {
        log.info("🔹 Creating radiology request...");

        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        Client doctor = clientRepository.findByEmail(username.toLowerCase())
                .orElseThrow(() -> new NotFoundException("Doctor not found"));

        // 👤 المريض - Check if it's a family member
        Client member;
        FamilyMember familyMember = null;
        String familyMemberInfo = "";

        if (dto.getMemberId() != null) {
            // First try to find as a Client
            Optional<Client> clientOpt = clientRepository.findById(dto.getMemberId());

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
        } else {
            throw new NotFoundException("Member ID is required");
        }

        // 🆕 جلب خدمة الأشعة من PriceList
        PriceList test = priceListRepository.findById(dto.getTestId())
                .orElseThrow(() -> new NotFoundException("Radiology test not found"));

        // 📝 إنشاء الطلب
        RadiologyRequest request = radiologyRequestMapper.toEntity(dto);
        request.setDoctor(doctor);
        request.setMember(member); // Always link to main client
        request.setRadiologist(null);
        request.setTest(test);               // 🆕 ربط PriceList
        request.setTestName(test.getServiceName()); // 🆕 اسم الفحص

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

        RadiologyRequest savedRequest = radiologyRequestRepository.save(request);
        
        // Store familyMember reference for later use (before detachment)
        final FamilyMember savedFamilyMember = familyMember;

        // 🔔 إشعار للراديولوجيين
        String radiologistMessage = savedFamilyMember != null
                ? String.format(
                "📋 لديك طلب فحص إشعاعي جديد من الدكتور %s لعضو العائلة %s (%s) - العميل: %s",
                doctor.getFullName(),
                savedFamilyMember.getFullName(),
                savedFamilyMember.getRelation(),
                member.getFullName()
        )
                : "📋 لديك طلب فحص إشعاعي جديد من الدكتور " + doctor.getFullName() +
                " للمريض " + member.getFullName();

        clientRepository.findByRoles_Name(RoleName.RADIOLOGIST)
                .forEach(r -> notificationService.sendToUser(
                        r.getId(),
                        radiologistMessage
                ));

        // 🔔 إشعار للمريض (main client)
        String memberNotification = savedFamilyMember != null
                ? String.format(
                "📊 تم إنشاء طلب فحص إشعاعي جديد من الدكتور %s لعضو العائلة %s (%s) - الفحص: %s",
                doctor.getFullName(),
                savedFamilyMember.getFullName(),
                savedFamilyMember.getRelation(),
                test.getServiceName()
        )
                : "📊 تم إنشاء طلب فحص إشعاعي جديد بواسطة الدكتور " + doctor.getFullName() +
                " - الفحص: " + test.getServiceName();

        notificationService.sendToUser(
                member.getId(),
                memberNotification
        );
        
        // Convert to DTO and explicitly set family member information if applicable
        RadiologyRequestDTO resultDto = radiologyRequestMapper.toDto(savedRequest, familyMemberRepo);
        
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

    // 📖 Radiologist views pending radiology requests
    public List<RadiologyRequestDTO> getPendingRequests(UUID radiologistId) {
        List<RadiologyRequest> requests = radiologyRequestRepository.findByStatusWithMember(LabRequestStatus.PENDING);
        
        // Force initialization of member fields
        for (RadiologyRequest r : requests) {
            if (r.getMember() != null) {
                Client member = r.getMember();
                String name = member.getFullName();
                java.time.LocalDate dob = member.getDateOfBirth();
                String gender = member.getGender();
                String nationalId = member.getNationalId();
            }
        }
        
        return requests.stream()
                .map(r -> radiologyRequestMapper.toDto(r, familyMemberRepo))
                .collect(Collectors.toList());
    }

    // 🧪 Radiologist uploads radiology result with price calculation
    @Transactional
    public RadiologyRequestDTO uploadRadiologyResult(UUID requestId, MultipartFile file, String testName, Double enteredPrice) {
        log.info("🔹 Uploading radiology result...");

        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        Client radiologist = clientRepository.findByEmail(username.toLowerCase())
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
                        " - السعر المعتمد: " + saved.getApprovedPrice() + " شيكل"
        );

        // 🔔 إشعار للطبيب
        notificationService.sendToUser(
                saved.getDoctor().getId(),
                "✅ تم إكمال فحص الأشعة للمريض " + saved.getMember().getFullName()
        );

        return radiologyRequestMapper.toDto(saved, familyMemberRepo);
    }

    // 📖 Member or Doctor views the result
    public RadiologyRequestDTO getResult(UUID id) {
        RadiologyRequest request = radiologyRequestRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Radiology request not found"));

        // Force initialization of member fields
        if (request.getMember() != null) {
            Client member = request.getMember();
            String name = member.getFullName();
            java.time.LocalDate dob = member.getDateOfBirth();
            String gender = member.getGender();
            String nationalId = member.getNationalId();
        }

        return radiologyRequestMapper.toDto(request, familyMemberRepo);
    }

    // ✏️ Doctor updates radiology request notes
    @Transactional
    public RadiologyRequestDTO update(UUID id, RadiologyRequestDTO dto) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        Client doctor = clientRepository.findByEmail(username.toLowerCase())
                .orElseThrow(() -> new NotFoundException("Doctor not found"));

        RadiologyRequest request = radiologyRequestRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Radiology request not found"));

        if (!request.getDoctor().getId().equals(doctor.getId()))
            throw new RuntimeException("Not allowed");

        if (request.getStatus() == LabRequestStatus.COMPLETED)
            throw new RuntimeException("Cannot update completed request");

        request.setNotes(dto.getNotes());
        request.setUpdatedAt(Instant.now());

        return radiologyRequestMapper.toDto(radiologyRequestRepository.save(request), familyMemberRepo);
    }

    // ❌ Doctor deletes radiology request
    @Transactional
    public void delete(UUID id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        Client doctor = clientRepository.findByEmail(username.toLowerCase())
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

        Client doctor = clientRepository.findByEmail(username.toLowerCase())
                .orElseThrow(() -> new NotFoundException("Doctor not found"));

        List<RadiologyRequest> requests = radiologyRequestRepository.findByDoctorIdWithMember(doctor.getId());
        
        // Force initialization of member fields
        for (RadiologyRequest r : requests) {
            if (r.getMember() != null) {
                Client member = r.getMember();
                String name = member.getFullName();
                java.time.LocalDate dob = member.getDateOfBirth();
                String gender = member.getGender();
                String nationalId = member.getNationalId();
            }
        }
        
        return requests.stream()
                .map(r -> radiologyRequestMapper.toDto(r, familyMemberRepo))
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
        List<RadiologyRequest> requests = radiologyRequestRepository.findByRadiologistIdWithMember(radiologistId);
        
        // Force initialization of member fields
        for (RadiologyRequest r : requests) {
            if (r.getMember() != null) {
                Client member = r.getMember();
                String name = member.getFullName();
                java.time.LocalDate dob = member.getDateOfBirth();
                String gender = member.getGender();
                String nationalId = member.getNationalId();
            }
        }
        
        return requests.stream()
                .map(r -> radiologyRequestMapper.toDto(r, familyMemberRepo))
                .collect(Collectors.toList());
    }

    // 📖 Member views his radiology requests
    public List<RadiologyRequestDTO> getByMember() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        Client member = clientRepository.findByEmail(username.toLowerCase())
                .orElseThrow(() -> new NotFoundException("Member not found"));

        List<RadiologyRequest> requests = radiologyRequestRepository.findByMemberIdWithMember(member.getId());
        
        // Force initialization of member fields
        for (RadiologyRequest r : requests) {
            if (r.getMember() != null) {
                Client memberEntity = r.getMember();
                String name = memberEntity.getFullName();
                java.time.LocalDate dob = memberEntity.getDateOfBirth();
                String gender = memberEntity.getGender();
                String nationalId = memberEntity.getNationalId();
            }
        }
        
        return requests.stream()
                .map(r -> radiologyRequestMapper.toDto(r, familyMemberRepo))
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
    public ClientDto updateRadiologistProfile(String username, UpdateUserDTO dto, MultipartFile[] universityCard){
        Client radiologist = clientRepository.findByEmail(username.toLowerCase())
                .orElseThrow(() -> new NotFoundException("Radiologist not found"));

        if (dto.getFullName() != null) radiologist.setFullName(dto.getFullName());
        if (dto.getEmail() != null) radiologist.setEmail(dto.getEmail());
        if (dto.getPhone() != null) radiologist.setPhone(dto.getPhone());

        if (universityCard != null && universityCard.length > 0) {
            try {
                String uploadDir = "uploads/radiologists";
                Files.createDirectories(Paths.get(uploadDir));

                // تأكد الليست مش null
                if (radiologist.getUniversityCardImages() == null) {
                    radiologist.setUniversityCardImages(new java.util.ArrayList<>());
                }

                for (MultipartFile file : universityCard) {
                    if (file == null || file.isEmpty()) continue;

                    String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
                    Path filePath = Paths.get(uploadDir, fileName);

                    Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                    // خزّن المسار في DB
                    radiologist.getUniversityCardImages().add("/" + uploadDir + "/" + fileName);
                }

            } catch (IOException e) {
                throw new RuntimeException("Failed to save radiologist images", e);
            }
        }

        radiologist.setUpdatedAt(Instant.now());

        return clientMapper.toDTO(clientRepository.save(radiologist));
    }
}

