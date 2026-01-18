package com.insurancesystem.Services;

import com.insurancesystem.Exception.NotFoundException;
import com.insurancesystem.Model.Dto.ClientDto;
import com.insurancesystem.Model.Dto.UpdateUserDTO;
import com.insurancesystem.Model.Entity.Client;
import com.insurancesystem.Model.MapStruct.ClientMapper;
import com.insurancesystem.Repository.ClientRepository;
import com.insurancesystem.Repository.LabRequestRepository;
import com.insurancesystem.Repository.MedicalRecordRepository;
import com.insurancesystem.Repository.PrescriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DoctorService {

    private final MedicalRecordRepository medicalRecordRepository;
    private final ClientRepository clientRepo;

    private final PrescriptionRepository prescriptionRepository;
    private final LabRequestRepository labRequestRepository;
    private final ClientMapper clientMapper;



    public ClientDto updateProfile(String username, UpdateUserDTO dto, MultipartFile[] universityCard){
        Client client = clientRepo.findByEmail(username.toLowerCase())
                .orElseThrow(() -> new NotFoundException("User not found"));


        if (dto.getFullName() != null) client.setFullName(dto.getFullName());
        if (dto.getEmail() != null) client.setEmail(dto.getEmail());
        if (dto.getPhone() != null) client.setPhone(dto.getPhone());

        if (universityCard != null && universityCard.length > 0) {
            try {
                String uploadDir = "uploads/university-cards";
                Files.createDirectories(Paths.get(uploadDir));

                if (client.getUniversityCardImages() == null) {
                    client.setUniversityCardImages(new java.util.ArrayList<>());
                }

                for (MultipartFile file : universityCard) {
                    if (file == null || file.isEmpty()) continue;

                    String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
                    Path filePath = Paths.get(uploadDir, fileName);

                    Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                    client.getUniversityCardImages().add("/" + uploadDir + "/" + fileName);
                }

            } catch (Exception e) {
                throw new RuntimeException("Failed to upload university card", e);
            }
        }


        clientRepo.save(client);

        return clientMapper.toDTO(client);


    }

    public Map<String, Long> getDoctorStats(String username) {
        Client doctor = clientRepo.findByEmail(username.toLowerCase())
                .orElseThrow(() -> new NotFoundException("Doctor not found"));


        long prescriptionsCount = prescriptionRepository.countByDoctorId(doctor.getId());
        long labRequestsCount = labRequestRepository.countByDoctorId(doctor.getId());
        long medicalRecordsCount = medicalRecordRepository.countByDoctorId(doctor.getId());

        long total = prescriptionsCount + labRequestsCount + medicalRecordsCount;

        Map<String, Long> stats = new HashMap<>();
        stats.put("prescriptions", prescriptionsCount);
        stats.put("labRequests", labRequestsCount);
        stats.put("medicalRecords", medicalRecordsCount);
        stats.put("total", total);

        return stats;
    }

}

