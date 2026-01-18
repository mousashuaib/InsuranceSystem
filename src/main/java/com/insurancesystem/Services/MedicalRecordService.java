package com.insurancesystem.Services;

import com.insurancesystem.Exception.NotFoundException;
import com.insurancesystem.Model.Dto.MedicalRecordDTO;
import com.insurancesystem.Model.Entity.Client;
import com.insurancesystem.Model.Entity.MedicalRecord;
import com.insurancesystem.Model.MapStruct.MedicalRecordMapper;
import com.insurancesystem.Repository.ClientRepository;
import com.insurancesystem.Repository.MedicalRecordRepository;
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
public class MedicalRecordService {

    private final MedicalRecordRepository recordRepo;
    private final ClientRepository clientRepo;
    private final MedicalRecordMapper medicalRecordMapper;

    // ➕ إنشاء سجل جديد (Doctor فقط)
    public MedicalRecordDTO createRecord(MedicalRecordDTO dto) {
        // جلب المستخدم الحالي (الدكتور) من SecurityContext
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();

        Client doctor = clientRepo.findByUsername(currentUsername)
                .orElseThrow(() -> new NotFoundException("Doctor not found"));

        // المريض فقط ييجي من الـ body
        Client member = clientRepo.findById(dto.getMemberId())
                .orElseThrow(() -> new NotFoundException("Member not found"));

        MedicalRecord record = medicalRecordMapper.toEntity(dto);
        record.setDoctor(doctor);   //  الدكتور من التوكن
        record.setMember(member);
        record.setCreatedAt(Instant.now());
        record.setUpdatedAt(Instant.now());

        return medicalRecordMapper.toDto(recordRepo.save(record));
    }


    //  جلب كل السجلات (Doctor أو Manager)
    public List<MedicalRecordDTO> getAll() {
        return recordRepo.findAll()
                .stream().map(medicalRecordMapper::toDto).collect(Collectors.toList());
    }

    //  جلب سجلات عضو محدد
    public List<MedicalRecordDTO> getByMember(UUID memberId) {
        Client member = clientRepo.findById(memberId)
                .orElseThrow(() -> new NotFoundException("Member not found"));

        // 👤 إذا كان المستخدم الحالي Client لازم نتأكد انه يجيب سجلاته فقط
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();
        if (auth.getAuthorities().stream().anyMatch(r -> r.getAuthority().equals("ROLE_INSURANCE_CLIENT"))) {
            if (!member.getUsername().equals(currentUsername)) {
                throw new SecurityException("You can only view your own records!");
            }
        }

        return recordRepo.findByMemberId(memberId)
                .stream().map(medicalRecordMapper::toDto).collect(Collectors.toList());
    }

    //  جلب سجل واحد
    public MedicalRecordDTO getById(UUID id) {
        MedicalRecord record = recordRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Medical record not found"));

        // 👤 إذا كان Client لازم يكون هو صاحب السجل
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth.getAuthorities().stream().anyMatch(r -> r.getAuthority().equals("ROLE_INSURANCE_CLIENT"))) {
            String currentUsername = auth.getName();
            if (!record.getMember().getUsername().equals(currentUsername)) {
                throw new SecurityException("You can only view your own record!");
            }
        }

        return medicalRecordMapper.toDto(record);
    }

    //  تحديث سجل (Doctor فقط)
    public MedicalRecordDTO updateRecord(UUID id, MedicalRecordDTO dto) {
        MedicalRecord record = recordRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Medical record not found"));

        record.setDiagnosis(dto.getDiagnosis());
        record.setTreatment(dto.getTreatment());
        record.setNotes(dto.getNotes());
        record.setUpdatedAt(Instant.now());

        return medicalRecordMapper.toDto(recordRepo.save(record));
    }

    //  حذف سجل (Doctor فقط)
    public void deleteRecord(UUID id) {
        if (!recordRepo.existsById(id)) {
            throw new NotFoundException("Medical record not found");
        }
        recordRepo.deleteById(id);
    }
}
