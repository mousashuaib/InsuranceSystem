package com.insurancesystem.Services;

import com.insurancesystem.Exception.NotFoundException;
import com.insurancesystem.Model.Dto.ClientDto;
import com.insurancesystem.Model.Dto.PrescriptionDTO;
import com.insurancesystem.Model.Dto.UpdateUserDTO;
import com.insurancesystem.Model.Entity.Client;
import com.insurancesystem.Model.Entity.Enums.PrescriptionStatus;
import com.insurancesystem.Model.Entity.Prescription;
import com.insurancesystem.Model.MapStruct.ClientMapper;
import com.insurancesystem.Model.MapStruct.PrescriptionMapper;
import com.insurancesystem.Repository.ClientRepository;
import com.insurancesystem.Repository.PrescriptionRepository;
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
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepo;
    private final ClientRepository clientRepo;
    private final PrescriptionMapper prescriptionMapper;
    private final ClientMapper clientMapper;
    private final NotificationService notificationService;

    // ➕ Doctor ينشئ وصفة
    public PrescriptionDTO create(PrescriptionDTO dto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();

        // 🧑‍⚕️ الدكتور من التوكن
        Client doctor = clientRepo.findByUsername(currentUsername)
                .orElseThrow(() -> new NotFoundException("Doctor not found"));

        // 👤 المريض: إما بالـ ID أو بالـ Name
        Client member = null;
        if (dto.getMemberId() != null) {
            member = clientRepo.findById(dto.getMemberId())
                    .orElseThrow(() -> new NotFoundException("Member not found"));
        } else if (dto.getMemberName() != null && !dto.getMemberName().isBlank()) {
            member = clientRepo.findByFullName(dto.getMemberName())
                    .orElseThrow(() -> new NotFoundException("Member not found with name: " + dto.getMemberName()));
        } else {
            throw new IllegalArgumentException("Member ID or Member Name is required");
        }

        // 📝 بناء الوصفة
        Prescription prescription = prescriptionMapper.toEntity(dto);
        prescription.setDoctor(doctor);
        prescription.setMember(member);
        prescription.setStatus(PrescriptionStatus.PENDING);
        prescription.setCreatedAt(Instant.now());
        prescription.setUpdatedAt(Instant.now());

        // 🔔 إشعار للمريض
        notificationService.sendToUser(
                member.getId(),
                "تم إنشاء وصفة جديدة من دكتور " + doctor.getFullName()
        );

        return prescriptionMapper.toDto(prescriptionRepo.save(prescription));
    }

    // 📖 Member يشوف وصفاته
    public List<PrescriptionDTO> getMyPrescriptions() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();

        Client member = clientRepo.findByUsername(currentUsername)
                .orElseThrow(() -> new NotFoundException("Member not found"));

        return prescriptionRepo.findByMemberId(member.getId())
                .stream()
                .map(prescriptionMapper::toDto)
                .collect(Collectors.toList());
    }

    // 📖 Pharmacist يشوف الوصفات المعلقة (عامة)
    public List<PrescriptionDTO> getPending() {
        return prescriptionRepo.findByStatus(PrescriptionStatus.PENDING)
                .stream()
                .map(prescriptionMapper::toDto)
                .collect(Collectors.toList());
    }

    // ✅ Pharmacist يوافق على وصفة
    public PrescriptionDTO verify(UUID id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        Client pharmacist = clientRepo.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Pharmacist not found"));

        Prescription prescription = prescriptionRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Prescription not found"));

        prescription.setStatus(PrescriptionStatus.VERIFIED);
        prescription.setUpdatedAt(Instant.now());
        prescription.setPharmacist(pharmacist);

        Prescription saved = prescriptionRepo.save(prescription);

        notificationService.sendToUser(
                saved.getMember().getId(),
                "تمت الموافقة على وصفتك الطبية."
        );

        return prescriptionMapper.toDto(saved);
    }

    // ❌ Pharmacist يرفض وصفة
    public PrescriptionDTO reject(UUID id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        Client pharmacist = clientRepo.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Pharmacist not found"));

        Prescription prescription = prescriptionRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Prescription not found"));

        prescription.setStatus(PrescriptionStatus.REJECTED);
        prescription.setUpdatedAt(Instant.now());
        prescription.setPharmacist(pharmacist);

        Prescription saved = prescriptionRepo.save(prescription);

        notificationService.sendToUser(
                saved.getMember().getId(),
                "تم رفض وصفتك الطبية."
        );

        return prescriptionMapper.toDto(saved);
    }

    // ✏️ Doctor يعدل وصفة
    public PrescriptionDTO update(UUID id, PrescriptionDTO dto) {
        Prescription prescription = prescriptionRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Prescription not found"));

        prescription.setMedicine(dto.getMedicine());
        prescription.setDosage(dto.getDosage());
        prescription.setInstructions(dto.getInstructions());
        prescription.setUpdatedAt(Instant.now());

        return prescriptionMapper.toDto(prescriptionRepo.save(prescription));
    }

    // ❌ Doctor يحذف وصفة
    public void delete(UUID id) {
        Prescription prescription = prescriptionRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Prescription not found"));
        prescriptionRepo.delete(prescription);
    }

    // 📊 Doctor stats
    public PrescriptionDTO getDoctorStats() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();

        Client doctor = clientRepo.findByUsername(currentUsername)
                .orElseThrow(() -> new NotFoundException("Doctor not found"));

        return PrescriptionDTO.builder()
                .total(prescriptionRepo.countByDoctorId(doctor.getId()))
                .pending(prescriptionRepo.countByDoctorIdAndStatus(doctor.getId(), PrescriptionStatus.PENDING))
                .verified(prescriptionRepo.countByDoctorIdAndStatus(doctor.getId(), PrescriptionStatus.VERIFIED))
                .rejected(prescriptionRepo.countByDoctorIdAndStatus(doctor.getId(), PrescriptionStatus.REJECTED))
                .build();
    }

    // 📊 Pharmacist stats
    public PrescriptionDTO getPharmacistStats() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        Client pharmacist = clientRepo.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Pharmacist not found"));

        return PrescriptionDTO.builder()
                // Pending عامة (كل النظام)
                .pending(prescriptionRepo.countByStatus(PrescriptionStatus.PENDING))

                // Verified/Rejected خاصة بالصيدلي الحالي
                .verified(prescriptionRepo.countByPharmacistIdAndStatus(pharmacist.getId(), PrescriptionStatus.VERIFIED))
                .rejected(prescriptionRepo.countByPharmacistIdAndStatus(pharmacist.getId(), PrescriptionStatus.REJECTED))

                // Total تخص الصيدلي الحالي (Verified + Rejected فقط)
                .total(
                        prescriptionRepo.countByPharmacistIdAndStatus(pharmacist.getId(), PrescriptionStatus.VERIFIED)
                                + prescriptionRepo.countByPharmacistIdAndStatus(pharmacist.getId(), PrescriptionStatus.REJECTED)
                )
                .build();
    }

    // 👤 Pharmacist يحدّث بروفايله
    public ClientDto updatePharmacistProfile(String username, UpdateUserDTO dto, MultipartFile universityCard) {
        Client pharmacist = clientRepo.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Pharmacist not found"));

        if (dto.getFullName() != null && !dto.getFullName().isBlank()) {
            pharmacist.setFullName(dto.getFullName());
        }
        if (dto.getEmail() != null && !dto.getEmail().isBlank()) {
            pharmacist.setEmail(dto.getEmail());
        }
        if (dto.getPhone() != null && !dto.getPhone().isBlank()) {
            pharmacist.setPhone(dto.getPhone());
        }

        // ✅ تحديث الصورة
        if (universityCard != null && !universityCard.isEmpty()) {
            try {
                String fileName = UUID.randomUUID() + "_" + universityCard.getOriginalFilename();
                Path uploadPath = Paths.get("uploads/pharmacists");
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }
                Path filePath = uploadPath.resolve(fileName);
                Files.copy(universityCard.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                pharmacist.setUniversityCardImage("/uploads/pharmacists/" + fileName);
            } catch (IOException e) {
                throw new RuntimeException("❌ Failed to save pharmacist image", e);
            }
        }

        pharmacist.setUpdatedAt(Instant.now());
        Client updated = clientRepo.save(pharmacist);

        return clientMapper.toDTO(updated);
    }
    public List<PrescriptionDTO> getByDoctor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();

        Client doctor = clientRepo.findByUsername(currentUsername)
                .orElseThrow(() -> new NotFoundException("Doctor not found"));

        return prescriptionRepo.findByDoctorId(doctor.getId())
                .stream()
                .map(prescriptionMapper::toDto)
                .collect(Collectors.toList());
    }
}
