package com.insurancesystem.Services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.insurancesystem.Exception.NotFoundException;
import com.insurancesystem.Model.Dto.ClientDto;
import com.insurancesystem.Model.Dto.PrescriptionDTO;
import com.insurancesystem.Model.Dto.PrescriptionItemDTO;
import com.insurancesystem.Model.Dto.UpdateUserDTO;
import com.insurancesystem.Model.Entity.Client;
import com.insurancesystem.Model.Entity.Enums.PrescriptionStatus;
import com.insurancesystem.Model.Entity.Enums.RoleName;
import com.insurancesystem.Model.Entity.FamilyMember;
import com.insurancesystem.Model.Entity.Prescription;
import com.insurancesystem.Model.Entity.PrescriptionItem;
import com.insurancesystem.Model.Entity.PriceList;
import com.insurancesystem.Model.MapStruct.ClientMapper;
import com.insurancesystem.Model.MapStruct.PrescriptionMapper;
import com.insurancesystem.Repository.*;
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
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepo;
    private final PrescriptionItemRepository prescriptionItemRepo;
    private final ClientRepository clientRepo;
    private final PriceListRepository priceListRepo;
    private final FamilyMemberRepository familyMemberRepo;
    private final PrescriptionMapper prescriptionMapper;
    private final ClientMapper clientMapper;
    private final NotificationService notificationService;
    private final ObjectMapper json = new ObjectMapper();

    private int extractQuantity(String jsonStr) {
        try {
            if (jsonStr == null) return 1;
            Map<String, Object> data = json.readValue(jsonStr, Map.class);
            return data.get("quantity") != null ? (int) data.get("quantity") : 1;
        } catch (Exception e) {
            return 1;
        }
    }

    // ➕ Doctor creates prescription
    @Transactional
    public PrescriptionDTO create(PrescriptionDTO dto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String doctorUsername = auth.getName();

        Client doctor = clientRepo.findByEmail(doctorUsername.toLowerCase())
                .orElseThrow(() -> new NotFoundException("DOCTOR_NOT_FOUND"));

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
                    throw new NotFoundException("MEMBER_NOT_FOUND");
                }
            }
        } else {
            member = clientRepo.findByFullName(dto.getMemberName())
                    .orElseThrow(() -> new NotFoundException("MEMBER_NOT_FOUND"));
        }

        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            throw new IllegalArgumentException("PRESCRIPTION_MUST_HAVE_MEDICINES");
        }

        // Add family member info to treatment notes if applicable
        String treatmentNotes = dto.getTreatment();
        if (familyMember != null && !familyMemberInfo.isEmpty()) {
            treatmentNotes = dto.getTreatment() + familyMemberInfo;
        }

        Prescription prescription = Prescription.builder()
                .doctor(doctor)
                .member(member) // Always link to main client
                .status(PrescriptionStatus.PENDING)
                .diagnosis(dto.getDiagnosis())
                .treatment(treatmentNotes)
                .totalPrice(0.0)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        prescriptionRepo.save(prescription);

        List<PrescriptionItem> savedItems = new ArrayList<>();

        for (PrescriptionItemDTO itemDto : dto.getItems()) {
            PriceList med = priceListRepo.findById(itemDto.getMedicineId())
                    .orElseThrow(() -> new NotFoundException("MEDICINE_NOT_FOUND_IN_PRICE_LIST"));

            // Use duration if provided, otherwise calculate from quantity
            Integer duration = itemDto.getDuration();
            Instant expiry;
            if (duration != null && duration > 0) {
                expiry = Instant.now().plus(duration, ChronoUnit.DAYS);
            } else {
                // Fallback to old calculation if duration not provided
                int quantity = extractQuantity(med.getServiceDetails());
                int dailyConsumption = itemDto.getDosage() * itemDto.getTimesPerDay();
                int daysOfSupply = dailyConsumption > 0 ? quantity / dailyConsumption : 1;
                expiry = Instant.now().plus(daysOfSupply, ChronoUnit.DAYS);
            }

            PrescriptionItem item = PrescriptionItem.builder()
                    .prescription(prescription)
                    .priceList(med)
                    .dosage(itemDto.getDosage())
                    .timesPerDay(itemDto.getTimesPerDay())
                    .duration(duration)
                    .expiryDate(expiry)
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build();

            savedItems.add(prescriptionItemRepo.save(item));
        }

        prescription.setItems(savedItems);
        prescriptionRepo.save(prescription);

        // 🔔 إشعار للصيادلة (جميع الصيادلة)
        String pharmacistMessage = familyMember != null
                ? String.format(
                "📋 لديك وصفة طبية جديدة من الدكتور %s لعضو العائلة %s (%s) - العميل: %s",
                doctor.getFullName(),
                familyMember.getFullName(),
                familyMember.getRelation(),
                member.getFullName()
        )
                : "📋 لديك وصفة طبية جديدة من الدكتور " + doctor.getFullName() +
                " للمريض " + member.getFullName();

        clientRepo.findByRoles_Name(RoleName.PHARMACIST)
                .forEach(pharmacist -> notificationService.sendToUser(
                        pharmacist.getId(),
                        pharmacistMessage
                ));

        // 🔔 إشعار للمريض (main client)
        String notificationMessage = familyMember != null
                ? String.format(
                "💊 تم إنشاء وصفة طبية جديدة من الدكتور %s لعضو العائلة %s (%s)",
                doctor.getFullName(),
                familyMember.getFullName(),
                familyMember.getRelation()
        )
                : "💊 تم إنشاء وصفة طبية جديدة لك من الدكتور " + doctor.getFullName();

        notificationService.sendToUser(
                member.getId(),
                notificationMessage
        );

        return prescriptionMapper.toDto(prescription);
    }

    // Member sees prescriptions
    @Transactional(readOnly = true)
    public List<PrescriptionDTO> getMyPrescriptions() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Client member = clientRepo.findByEmail(auth.getName().toLowerCase())
                .orElseThrow(() -> new NotFoundException("Member not found"));

        // Use custom query that eagerly fetches member with dateOfBirth and gender
        return prescriptionRepo.findByMemberIdWithMember(member.getId())
                .stream()
                .map(prescriptionMapper::toDto)
                .collect(Collectors.toList());
    }

    // Pending prescriptions for pharmacists
    @Transactional(readOnly = true)
    public List<PrescriptionDTO> getPending() {
        // Use custom query that eagerly fetches member with dateOfBirth and gender
        List<Prescription> prescriptions = prescriptionRepo.findByStatusWithMember(PrescriptionStatus.PENDING);

        log.info("📋 [SERVICE] Found {} pending prescriptions", prescriptions.size());

        // Force initialization of member fields to ensure they're loaded
        for (Prescription p : prescriptions) {
            if (p.getMember() != null) {
                Client member = p.getMember();
                // Force access to fields to trigger loading
                String name = member.getFullName();
                java.time.LocalDate dob = member.getDateOfBirth();
                String gender = member.getGender();
                log.info("👤 [SERVICE] Prescription {} - Member: {} | DOB: {} | Gender: {}",
                        p.getId(), name, dob, gender);
            } else {
                log.warn("⚠️ [SERVICE] Prescription {} has null member", p.getId());
            }
        }

        List<PrescriptionDTO> dtos = prescriptions.stream()
                .map(prescriptionMapper::toDto)
                .collect(Collectors.toList());

        // Verify DTOs have the data
        for (PrescriptionDTO dto : dtos) {
            log.info("📦 [SERVICE] DTO for {} - memberAge: {} | memberGender: {}",
                    dto.getId(), dto.getMemberAge(), dto.getMemberGender());
        }

        return dtos;
    }

    // Pharmacist verifies items
    @Transactional
    public PrescriptionDTO verify(UUID id, List<PrescriptionItemDTO> itemsWithPrices) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Client pharmacist = clientRepo.findByEmail(auth.getName().toLowerCase())
                .orElseThrow(() -> new NotFoundException("PHARMACIST_NOT_FOUND"));

        Prescription prescription = prescriptionRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("PRESCRIPTION_NOT_FOUND"));

        double total = 0;

        for (PrescriptionItemDTO dto : itemsWithPrices) {
            PrescriptionItem item = prescriptionItemRepo.findById(dto.getId())
                    .orElseThrow(() -> new NotFoundException("ITEM_NOT_FOUND"));

            PriceList med = item.getPriceList();
            double unionPrice = med.getPrice();
            double pharmacistPrice = dto.getPharmacistPrice();
            double finalPrice = Math.min(pharmacistPrice, unionPrice);

            item.setPharmacistPrice(pharmacistPrice);
            item.setFinalPrice(finalPrice);
            item.setUpdatedAt(Instant.now());
            prescriptionItemRepo.save(item);

            total += finalPrice;
        }

        prescription.setPharmacist(pharmacist);
        prescription.setStatus(PrescriptionStatus.VERIFIED);
        prescription.setTotalPrice(total);
        prescription.setUpdatedAt(Instant.now());
        prescriptionRepo.save(prescription);

        // 🔔 إشعار للمريض
        notificationService.sendToUser(
                prescription.getMember().getId(),
                "✅ تمت الموافقة على وصفتك من الصيدلي " + pharmacist.getFullName() +
                        " - المجموع: " + total + " دينار"
        );

        // 🔔 إشعار للطبيب
        notificationService.sendToUser(
                prescription.getDoctor().getId(),
                "✅ تمت الموافقة على وصفتك للمريض " + prescription.getMember().getFullName() +
                        " من الصيدلي " + pharmacist.getFullName()
        );

        return prescriptionMapper.toDto(prescription);
    }

    // Pharmacist reject
    @Transactional
    public PrescriptionDTO reject(UUID id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Client pharmacist = clientRepo.findByEmail(auth.getName().toLowerCase())
                .orElseThrow(() -> new NotFoundException("PHARMACIST_NOT_FOUND"));

        Prescription prescription = prescriptionRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("PRESCRIPTION_NOT_FOUND"));

        prescription.setStatus(PrescriptionStatus.REJECTED);
        prescription.setPharmacist(pharmacist);
        prescription.setUpdatedAt(Instant.now());
        prescriptionRepo.save(prescription);

        // 🔔 إشعار للمريض
        notificationService.sendToUser(
                prescription.getMember().getId(),
                "❌ تم رفض وصفتك من الصيدلي " + pharmacist.getFullName()
        );

        // 🔔 إشعار للطبيب
        notificationService.sendToUser(
                prescription.getDoctor().getId(),
                "❌ تم رفض وصفتك للمريض " + prescription.getMember().getFullName() +
                        " من الصيدلي " + pharmacist.getFullName()
        );

        return prescriptionMapper.toDto(prescription);
    }

    // Doctor updates prescription
    @Transactional
    public PrescriptionDTO update(UUID id, PrescriptionDTO dto) {
        Prescription prescription = prescriptionRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("PRESCRIPTION_NOT_FOUND"));

        if (prescription.getStatus() != PrescriptionStatus.PENDING) {
            throw new IllegalStateException("CANNOT_UPDATE_NON_PENDING");
        }

        prescription.setDiagnosis(dto.getDiagnosis());
        prescription.setTreatment(dto.getTreatment());
        prescription.setUpdatedAt(Instant.now());

        prescriptionItemRepo.deleteAll(prescription.getItems());
        prescription.getItems().clear();

        List<PrescriptionItem> newItems = new ArrayList<>();

        for (PrescriptionItemDTO itemDto : dto.getItems()) {
            PriceList med = priceListRepo.findById(itemDto.getMedicineId())
                    .orElseThrow(() -> new NotFoundException("MEDICINE_NOT_FOUND_IN_PRICE_LIST"));

            // Use duration if provided, otherwise calculate from quantity
            Integer duration = itemDto.getDuration();
            Instant expiry;
            if (duration != null && duration > 0) {
                expiry = Instant.now().plus(duration, ChronoUnit.DAYS);
            } else {
                // Fallback to old calculation if duration not provided
                int quantity = extractQuantity(med.getServiceDetails());
                int daily = itemDto.getDosage() * itemDto.getTimesPerDay();
                int days = daily > 0 ? quantity / daily : 1;
                expiry = Instant.now().plus(days, ChronoUnit.DAYS);
            }

            PrescriptionItem item = PrescriptionItem.builder()
                    .prescription(prescription)
                    .priceList(med)
                    .dosage(itemDto.getDosage())
                    .timesPerDay(itemDto.getTimesPerDay())
                    .duration(duration)
                    .expiryDate(expiry)
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build();

            newItems.add(prescriptionItemRepo.save(item));
        }

        prescription.setItems(newItems);
        prescriptionRepo.save(prescription);

        // 🔔 إشعار للمريض
        notificationService.sendToUser(
                prescription.getMember().getId(),
                "✏️ تم تحديث وصفتك الطبية من الدكتور " + prescription.getDoctor().getFullName()
        );

        // 🔔 إشعار للصيادلة (في حالة وجود صيدلي مرتبط)
        if (prescription.getPharmacist() != null) {
            notificationService.sendToUser(
                    prescription.getPharmacist().getId(),
                    "✏️ تم تحديث الوصفة الطبية من الدكتور " + prescription.getDoctor().getFullName() +
                            " للمريض " + prescription.getMember().getFullName()
            );
        }

        return prescriptionMapper.toDto(prescription);
    }

    // Doctor deletes prescription
    @Transactional
    public void delete(UUID id) {
        Prescription prescription = prescriptionRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("PRESCRIPTION_NOT_FOUND"));

        if (prescription.getStatus() != PrescriptionStatus.PENDING) {
            throw new IllegalStateException("CANNOT_DELETE_NON_PENDING");
        }

        // 🔔 إشعار للمريض
        notificationService.sendToUser(
                prescription.getMember().getId(),
                "🗑️ تم حذف وصفتك الطبية من الدكتور " + prescription.getDoctor().getFullName()
        );

        prescriptionRepo.delete(prescription);
    }

    // Doctor stats
    public PrescriptionDTO getDoctorStats() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Client doctor = clientRepo.findByEmail(auth.getName().toLowerCase())
                .orElseThrow(() -> new NotFoundException("DOCTOR_NOT_FOUND"));

        return PrescriptionDTO.builder()
                .total(prescriptionRepo.countByDoctorId(doctor.getId()))
                .pending(prescriptionRepo.countByDoctorIdAndStatus(doctor.getId(), PrescriptionStatus.PENDING))
                .verified(prescriptionRepo.countByDoctorIdAndStatus(doctor.getId(), PrescriptionStatus.VERIFIED))
                .rejected(prescriptionRepo.countByDoctorIdAndStatus(doctor.getId(), PrescriptionStatus.REJECTED))
                .build();
    }

    // Pharmacist stats
    public PrescriptionDTO getPharmacistStats() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Client pharmacist = clientRepo.findByEmail(auth.getName().toLowerCase())
                .orElseThrow(() -> new NotFoundException("PHARMACIST_NOT_FOUND"));

        return PrescriptionDTO.builder()
                .pending(prescriptionRepo.countByStatus(PrescriptionStatus.PENDING))
                .verified(prescriptionRepo.countByPharmacistIdAndStatus(pharmacist.getId(), PrescriptionStatus.VERIFIED))
                .rejected(prescriptionRepo.countByPharmacistIdAndStatus(pharmacist.getId(), PrescriptionStatus.REJECTED))
                .total(
                        prescriptionRepo.countByPharmacistIdAndStatus(pharmacist.getId(), PrescriptionStatus.PENDING)
                                + prescriptionRepo.countByPharmacistIdAndStatus(pharmacist.getId(), PrescriptionStatus.VERIFIED)
                                + prescriptionRepo.countByPharmacistIdAndStatus(pharmacist.getId(), PrescriptionStatus.REJECTED)
                )
                .build();
    }

    // Pharmacist update profile
    @Transactional
    public ClientDto updatePharmacistProfile(String username, UpdateUserDTO dto, MultipartFile[] universityCard){
        Client pharmacist = clientRepo.findByEmail(username.toLowerCase())
                .orElseThrow(() -> new NotFoundException("PHARMACIST_NOT_FOUND"));

        if (dto.getFullName() != null) pharmacist.setFullName(dto.getFullName());
        if (dto.getEmail() != null) pharmacist.setEmail(dto.getEmail());
        if (dto.getPhone() != null) pharmacist.setPhone(dto.getPhone());

        if (universityCard != null && universityCard.length > 0) {
            try {
                String uploadDir = "uploads/pharmacists";
                Files.createDirectories(Paths.get(uploadDir));

                if (pharmacist.getUniversityCardImages() == null) {
                    pharmacist.setUniversityCardImages(new ArrayList<>());
                }

                for (MultipartFile file : universityCard) {
                    if (file == null || file.isEmpty()) continue;

                    String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
                    Path filePath = Paths.get(uploadDir, fileName);

                    Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                    pharmacist.getUniversityCardImages()
                            .add("/" + uploadDir + "/" + fileName);
                }

            } catch (IOException e) {
                throw new RuntimeException("IMAGE_UPLOAD_FAILED", e);
            }
        }

        pharmacist.setUpdatedAt(Instant.now());
        Client saved = clientRepo.save(pharmacist);
        return clientMapper.toDTO(saved);
    }

    // Doctor sees his prescriptions
    @Transactional(readOnly = true)
    public List<PrescriptionDTO> getByDoctor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Client doctor = clientRepo.findByEmail(auth.getName().toLowerCase())
                .orElseThrow(() -> new NotFoundException("DOCTOR_NOT_FOUND"));

        // Use custom query that eagerly fetches member with dateOfBirth and gender
        return prescriptionRepo.findByDoctorIdWithMember(doctor.getId())
                .stream()
                .map(prescriptionMapper::toDto)
                .collect(Collectors.toList());
    }

    // Pharmacist sees all his prescriptions
    @Transactional(readOnly = true)
    public List<PrescriptionDTO> getAllForCurrentPharmacist() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Client pharmacist = clientRepo.findByEmail(auth.getName().toLowerCase())
                .orElseThrow(() -> new NotFoundException("PHARMACIST_NOT_FOUND"));

        // Use custom query that eagerly fetches member with dateOfBirth and gender
        List<Prescription> prescriptions = prescriptionRepo.findByPharmacistIdWithMember(pharmacist.getId());

        // Force initialization of member fields to ensure they're loaded
        for (Prescription p : prescriptions) {
            if (p.getMember() != null) {
                // Force access to fields to trigger loading
                p.getMember().getFullName();
                p.getMember().getDateOfBirth();
                p.getMember().getGender();
            }
        }

        return prescriptions.stream()
                .map(prescriptionMapper::toDto)
                .collect(Collectors.toList());
    }

    // Get all pharmacists
    public List<ClientDto> getAllPharmacists() {
        return clientRepo.findByRoles_Name(RoleName.PHARMACIST)
                .stream()
                .map(clientMapper::toDTO)
                .collect(Collectors.toList());
    }

    // Active prescription checker
    public Map<String, Object> checkActivePrescription(String memberName, UUID medicineId) {
        Map<String, Object> response = new HashMap<>();
        response.put("active", false);

        Client member = clientRepo.findByFullName(memberName).orElse(null);
        if (member == null) return response;

        List<Prescription> all = prescriptionRepo.findByMemberId(member.getId());

        for (Prescription p : all) {
            for (PrescriptionItem item : p.getItems()) {
                if (!item.getPriceList().getId().equals(medicineId)) continue;

                if (p.getStatus() == PrescriptionStatus.PENDING) {
                    response.put("active", true);
                    response.put("status", "PENDING");
                    return response;
                }

                if (p.getStatus() == PrescriptionStatus.VERIFIED &&
                        item.getExpiryDate().isAfter(Instant.now())) {
                    response.put("active", true);
                    response.put("status", "VERIFIED");
                    response.put("expiryDate", item.getExpiryDate());
                    return response;
                }
            }
        }

        return response;
    }

    // Bill prescription
    @Transactional
    public PrescriptionDTO bill(UUID id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Client pharmacist = clientRepo.findByEmail(auth.getName().toLowerCase())
                .orElseThrow(() -> new NotFoundException("PHARMACIST_NOT_FOUND"));

        Prescription prescription = prescriptionRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("PRESCRIPTION_NOT_FOUND"));

        // التحقق من أن الوصفة في حالة VERIFIED فقط
        if (prescription.getStatus() != PrescriptionStatus.VERIFIED) {
            throw new IllegalStateException("CAN_ONLY_BILL_VERIFIED_PRESCRIPTIONS");
        }

        // التحقق من أن الصيدلي هو نفسه الذي قام بالتحقق
        if (prescription.getPharmacist() == null ||
                !prescription.getPharmacist().getId().equals(pharmacist.getId())) {
            throw new IllegalStateException("ONLY_VERIFYING_PHARMACIST_CAN_BILL");
        }

        prescription.setStatus(PrescriptionStatus.BILLED);
        prescription.setUpdatedAt(Instant.now());
        prescriptionRepo.save(prescription);

        // 🔔 إشعار للمريض
        notificationService.sendToUser(
                prescription.getMember().getId(),
                "💊 تم صرف الأدوية من وصفتك الطبية بواسطة الصيدلي " + pharmacist.getFullName() +
                        " - المجموع: " + prescription.getTotalPrice() + " دينار. شكراً لاستخدامك خدماتنا."
        );

        // 🔔 إشعار للطبيب
        notificationService.sendToUser(
                prescription.getDoctor().getId(),
                "💊 تم صرف الأدوية من وصفتك للمريض " + prescription.getMember().getFullName() +
                        " بواسطة الصيدلي " + pharmacist.getFullName()
        );

        return prescriptionMapper.toDto(prescription);
    }
}

