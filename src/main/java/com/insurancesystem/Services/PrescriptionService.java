package com.insurancesystem.Services;

import com.insurancesystem.Exception.NotFoundException;
import com.insurancesystem.Model.Dto.PrescriptionDTO;
import com.insurancesystem.Model.Entity.Client;
import com.insurancesystem.Model.Entity.Enums.RoleName;
import com.insurancesystem.Model.Entity.Prescription;
import com.insurancesystem.Model.Entity.Enums.PrescriptionStatus;
import com.insurancesystem.Model.MapStruct.PrescriptionMapper;
import com.insurancesystem.Repository.ClientRepository;
import com.insurancesystem.Repository.PrescriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

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
    private final NotificationService notificationService;


    //  Doctor ينشئ وصفة
    public PrescriptionDTO create(PrescriptionDTO dto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();

        Client doctor = clientRepo.findByUsername(currentUsername)
                .orElseThrow(() -> new NotFoundException("Doctor not found"));

        Client member = clientRepo.findById(dto.getMemberId())
                .orElseThrow(() -> new NotFoundException("Member not found"));

        Prescription prescription = prescriptionMapper.toEntity(dto);
        prescription.setDoctor(doctor);
        prescription.setMember(member);
        prescription.setStatus(PrescriptionStatus.PENDING);
        prescription.setCreatedAt(Instant.now());
        prescription.setUpdatedAt(Instant.now());

        notificationService.sendToUser(
                member.getId(),
                "تم إنشاء وصفة جديدة من دكتور " + doctor.getFullName()
        );
        return prescriptionMapper.toDto(prescriptionRepo.save(prescription));
    }

    //  Member يشوف وصفاته
    public List<PrescriptionDTO> getMyPrescriptions() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();

        Client member = clientRepo.findByUsername(currentUsername)
                .orElseThrow(() -> new NotFoundException("Member not found"));

        return prescriptionRepo.findByMemberId(member.getId())
                .stream().map(prescriptionMapper::toDto).collect(Collectors.toList());
    }

    //  Pharmacist يشوف الوصفات المعلقة
    public List<PrescriptionDTO> getPending() {
        return prescriptionRepo.findByStatus(PrescriptionStatus.PENDING)
                .stream().map(prescriptionMapper::toDto).collect(Collectors.toList());
    }

    //  Pharmacist يوافق على وصفة
    public PrescriptionDTO verify(UUID id) {
        Prescription prescription = prescriptionRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Prescription not found"));

        prescription.setStatus(PrescriptionStatus.VERIFIED);
        prescription.setUpdatedAt(Instant.now());

        Prescription saved = prescriptionRepo.save(prescription);
        notificationService.sendToUser(
                saved.getMember().getId(),
                "تمت الموافقة على وصفتك الطبية."
        );
        notificationService.markNotificationAsReadByMessage(
                RoleName.INSURANCE_MANAGER,
                "وصفة جديدة من " + saved.getDoctor().getFullName() +
                        " للمريض " + saved.getMember().getFullName()
        );

        return prescriptionMapper.toDto(prescriptionRepo.save(prescription));
    }

    //  Pharmacist يرفض وصفة
    public PrescriptionDTO reject(UUID id) {
        Prescription prescription = prescriptionRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Prescription not found"));

        prescription.setStatus(PrescriptionStatus.REJECTED);
        prescription.setUpdatedAt(Instant.now());

        Prescription saved = prescriptionRepo.save(prescription);
        notificationService.sendToUser(
                saved.getMember().getId(),
                "تم رفض وصفتك الطبية."
        );
        notificationService.markNotificationAsReadByMessage(
                RoleName.INSURANCE_MANAGER,
                "وصفة جديدة من " + saved.getDoctor().getFullName() +
                        " للمريض " + saved.getMember().getFullName()
        );
        return prescriptionMapper.toDto(prescriptionRepo.save(prescription));
    }

    // ️ Doctor يعدل وصفة
    public PrescriptionDTO update(UUID id, PrescriptionDTO dto) {
        Prescription prescription = prescriptionRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Prescription not found"));

        // نسمح بتعديل فقط بعض الحقول
        prescription.setMedicine(dto.getMedicine());
        prescription.setDosage(dto.getDosage());
        prescription.setInstructions(dto.getInstructions());
        prescription.setUpdatedAt(Instant.now());

        return prescriptionMapper.toDto(prescriptionRepo.save(prescription));
    }

    //  Doctor يحذف وصفة
    public void delete(UUID id) {
        Prescription prescription = prescriptionRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Prescription not found"));
        prescriptionRepo.delete(prescription);
    }

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

    public PrescriptionDTO getPharmacistStats() {
        return PrescriptionDTO.builder()
                .total(prescriptionRepo.count())
                .pending(prescriptionRepo.countByStatus(PrescriptionStatus.PENDING))
                .verified(prescriptionRepo.countByStatus(PrescriptionStatus.VERIFIED))
                .rejected(prescriptionRepo.countByStatus(PrescriptionStatus.REJECTED))
                .build();
    }


}
