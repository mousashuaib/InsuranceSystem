package com.insurancesystem.Services;

import com.insurancesystem.Exception.NotFoundException;
import com.insurancesystem.Model.Dto.LabRequestDTO;
import com.insurancesystem.Model.Entity.Client;
import com.insurancesystem.Model.Entity.Enums.RoleName;
import com.insurancesystem.Model.Entity.LabRequest;
import com.insurancesystem.Model.Entity.Enums.LabRequestStatus;
import com.insurancesystem.Model.MapStruct.LabRequestMapper;
import com.insurancesystem.Repository.ClientRepository;
import com.insurancesystem.Repository.LabRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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
    private final NotificationService notificationService;


    // ➕ Doctor ينشئ طلب فحص
    public LabRequestDTO create(LabRequestDTO dto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();

        Client doctor = clientRepo.findByUsername(currentUsername)
                .orElseThrow(() -> new NotFoundException("Doctor not found"));

        Client member = clientRepo.findById(dto.getMemberId())
                .orElseThrow(() -> new NotFoundException("Member not found"));

        LabRequest request = labRequestMapper.toEntity(dto);
        request.setDoctor(doctor);
        request.setMember(member);
        request.setStatus(LabRequestStatus.PENDING);
        request.setCreatedAt(Instant.now());
        request.setUpdatedAt(Instant.now());


        notificationService.sendToUser(
                member.getId(),
                "تم إنشاء طلب فحص جديد: " + dto.getTestName()
        );
        return labRequestMapper.toDto(labRepo.save(request));
    }

    // 📖 Member يشوف طلباته
    public List<LabRequestDTO> getMyLabs() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();

        Client member = clientRepo.findByUsername(currentUsername)
                .orElseThrow(() -> new NotFoundException("Member not found"));

        return labRepo.findByMemberId(member.getId())
                .stream().map(labRequestMapper::toDto).collect(Collectors.toList());
    }

    public LabRequestDTO uploadResult(UUID id, MultipartFile file) {
        LabRequest request = labRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Lab request not found"));

        if (request.getStatus() == LabRequestStatus.COMPLETED) {
            throw new RuntimeException("Result already uploaded");
        }

        try {
            // مسار مجلد التخزين
            String uploadDir = "uploads/labs";
            Files.createDirectories(Paths.get(uploadDir));

            // اسم ملف فريد
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path filePath = Paths.get(uploadDir, fileName);

            // حفظ الملف
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // تحديث الداتا
            request.setResultUrl("/" + uploadDir + "/" + fileName); // ممكن تخزن المسار أو رابط
            request.setStatus(LabRequestStatus.COMPLETED);
            request.setUpdatedAt(Instant.now());

        } catch (Exception e) {
            throw new RuntimeException("Failed to upload file", e);
        }
        LabRequest saved = labRepo.save(request);
        notificationService.sendToUser(
                saved.getMember().getId(),
                "نتيجة فحص (" + saved.getTestName() + ") أصبحت متاحة الآن."
        );
        notificationService.markNotificationAsReadByMessage(
                RoleName.INSURANCE_MANAGER,
                "طلب فحص جديد (" + saved.getTestName() + ") للمريض " + saved.getMember().getFullName()
        );

        return labRequestMapper.toDto(labRepo.save(request));
    }

    // 📖 Member أو Doctor يشوف نتيجة فحص
    public LabRequestDTO getResult(UUID id) {
        LabRequest request = labRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Lab request not found"));

        return labRequestMapper.toDto(request);
    }
    // Doctor يعدل طلب فحص
    public LabRequestDTO update(UUID id, LabRequestDTO dto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();

        Client doctor = clientRepo.findByUsername(currentUsername)
                .orElseThrow(() -> new NotFoundException("Doctor not found"));

        LabRequest request = labRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Lab request not found"));

        // السماح فقط إذا كان نفس الدكتور هو صاحب الطلب
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
    //  Doctor يحذف طلب فحص
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

}
