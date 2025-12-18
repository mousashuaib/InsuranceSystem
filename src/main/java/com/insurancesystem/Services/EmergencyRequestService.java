package com.insurancesystem.Services;

import com.insurancesystem.Exception.BadRequestException;
import com.insurancesystem.Exception.NotFoundException;
import com.insurancesystem.Model.Dto.CreateEmergencyRequestDTO;
import com.insurancesystem.Model.Dto.EmergencyRequestDTO;
import com.insurancesystem.Model.Dto.RejectEmergencyDTO;
import com.insurancesystem.Model.Entity.Client;
import com.insurancesystem.Model.Entity.EmergencyRequest;
import com.insurancesystem.Model.Entity.Enums.EmergencyStatus;
import com.insurancesystem.Model.Entity.Enums.RoleName;
import com.insurancesystem.Model.MapStruct.EmergencyRequestMapper;
import com.insurancesystem.Repository.ClientRepository;
import com.insurancesystem.Repository.EmergencyRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmergencyRequestService {

    private final EmergencyRequestRepository emergencyRepo;
    private final ClientRepository clientRepo;
    private final EmergencyRequestMapper emergencyRequestMapper;
    private final NotificationService notificationService;

    // ✅ Doctor creates emergency request for a client
    @Transactional
    public EmergencyRequestDTO createEmergencyRequest(UUID doctorId, CreateEmergencyRequestDTO dto) {
        log.info("🔹 Creating emergency request for client: {}", dto.getClientName());

        // ✅ التحقق من وجود الـ Doctor
        Client doctor = clientRepo.findById(doctorId)
                .orElseThrow(() -> {
                    log.error("❌ Doctor not found: {}", doctorId);
                    return new NotFoundException("DOCTOR_NOT_FOUND");
                });

        // ✅ البحث عن Client باستخدام اسمه
        Client member = clientRepo.findByFullName(dto.getClientName())
                .orElseThrow(() -> {
                    log.error("❌ Client not found by name: {}", dto.getClientName());
                    return new NotFoundException("CLIENT_NOT_FOUND");
                });

        log.info("✅ Client found: {}", member.getFullName());

        EmergencyRequest emergency = EmergencyRequest.builder()
                .member(member)
                .doctorId(doctorId)  // ✅ حفظ معرف الدكتور
                .description(dto.getDescription())
                .location(dto.getLocation())
                .contactPhone(dto.getContactPhone())
                .incidentDate(dto.getIncidentDate())
                .notes(dto.getNotes())
                .status(EmergencyStatus.PENDING_MEDICAL)  // ✅ Changed from PENDING to PENDING_MEDICAL
                .submittedAt(Instant.now())
                .build();

        emergency = emergencyRepo.save(emergency);

        log.info("✅ Emergency request saved with ID: {}", emergency.getId());

        // 🔔 إشعار للعميل
        notificationService.sendToUser(
                member.getId(),
                "تم استلام طلب الطوارئ الخاص بك وهو الآن قيد المراجعة."
        );

        // 🔔 إشعار للدكتور الذي أنشأ الطلب
        notificationService.sendToUser(
                doctorId,
                "✅ تم إنشاء طلب طوارئ بنجاح للمريض " + member.getFullName() +
                        " في " + dto.getLocation() + " - في انتظار المراجعة الطبية"
        );

        // 🔔 إشعار للمدير الطبي
        notificationService.sendToRole(
                RoleName.MEDICAL_ADMIN,
                "طلب طوارئ جديد من " + member.getFullName() +
                        " في " + dto.getLocation()
        );

        log.info("✅ Notifications sent for emergency request ID: {}", emergency.getId());

        return emergencyRequestMapper.toDto(emergency);
    }

    // ✅ طلبات العضو نفسه
    public List<EmergencyRequestDTO> getMemberEmergencyRequests(UUID memberId) {
        log.info("🔹 Fetching emergency requests for member: {}", memberId);

        Client member = clientRepo.findById(memberId)
                .orElseThrow(() -> {
                    log.error("❌ Member not found: {}", memberId);
                    return new NotFoundException("MEMBER_NOT_FOUND");
                });

        List<EmergencyRequestDTO> requests = emergencyRepo.findByMember(member)
                .stream()
                .map(emergencyRequestMapper::toDto)
                .toList();

        log.info("✅ Found {} emergency requests for member", requests.size());

        return requests;
    }

    // ✅ جميع طلبات الطوارئ
    public List<EmergencyRequestDTO> getAllEmergencyRequests() {
        log.info("🔹 Fetching all emergency requests");

        List<EmergencyRequestDTO> requests = emergencyRepo.findAll()
                .stream()
                .map(emergencyRequestMapper::toDto)
                .toList();

        log.info("✅ Found {} total emergency requests", requests.size());

        return requests;
    }

    // ✅ طلبات الطوارئ التي أنشأها الـ Doctor
    public List<EmergencyRequestDTO> getDoctorEmergencyRequests(UUID doctorId) {
        log.info("🔹 Fetching emergency requests created by doctor: {}", doctorId);

        // التحقق من أن الـ Doctor موجود
        Client doctor = clientRepo.findById(doctorId)
                .orElseThrow(() -> {
                    log.error("❌ Doctor not found: {}", doctorId);
                    return new NotFoundException("DOCTOR_NOT_FOUND");
                });

        // جلب طلبات الطوارئ التي أنشأها هذا الدكتور فقط
        List<EmergencyRequestDTO> requests = emergencyRepo.findByDoctorId(doctorId)
                .stream()
                .map(emergencyRequestMapper::toDto)
                .collect(Collectors.toList());

        log.info("✅ Found {} emergency requests for doctor: {}", requests.size(), doctorId);

        return requests;
    }

    // ✅ الحصول على طلب طوارئ واحد للـ Doctor
    public EmergencyRequestDTO getDoctorEmergencyRequest(UUID doctorId, UUID requestId) {
        log.info("🔹 Fetching emergency request {} for doctor: {}", requestId, doctorId);

        // التحقق من أن الـ Doctor موجود
        Client doctor = clientRepo.findById(doctorId)
                .orElseThrow(() -> {
                    log.error("❌ Doctor not found: {}", doctorId);
                    return new NotFoundException("DOCTOR_NOT_FOUND");
                });

        // الحصول على طلب الطوارئ
        EmergencyRequest emergency = emergencyRepo.findById(requestId)
                .orElseThrow(() -> {
                    log.error("❌ Emergency request not found: {}", requestId);
                    return new NotFoundException("EMERGENCY_REQUEST_NOT_FOUND");
                });

        log.info("✅ Found emergency request: {}", requestId);

        return emergencyRequestMapper.toDto(emergency);
    }

    // ✅ الموافقة على طلب الطوارئ
    @Transactional
    public EmergencyRequestDTO approveEmergencyRequest(UUID id) {
        log.info("🔹 Approving emergency request: {}", id);

        EmergencyRequest emergency = emergencyRepo.findById(id)
                .orElseThrow(() -> {
                    log.error("❌ Emergency request not found: {}", id);
                    return new NotFoundException("EMERGENCY_REQUEST_NOT_FOUND");
                });

        if (emergency.getStatus() != EmergencyStatus.PENDING_MEDICAL) {  // ✅ Changed from PENDING to PENDING_MEDICAL
            log.warn("⚠️ Emergency request {} is already processed", id);
            throw new BadRequestException("REQUEST_ALREADY_PROCESSED");
        }

        emergency.setStatus(EmergencyStatus.APPROVED_BY_MEDICAL);  // ✅ Changed from APPROVED to APPROVED_BY_MEDICAL
        emergency.setApprovedAt(Instant.now());
        emergencyRepo.save(emergency);

        log.info("✅ Emergency request {} approved", id);

        // 🔔 إشعار للعميل
        notificationService.sendToUser(
                emergency.getMember().getId(),
                "✅ تمت الموافقة على طلب الطوارئ الخاص بك."
        );

        // 🔔 إشعار للدكتور الذي أنشأ الطلب
        notificationService.sendToUser(
                emergency.getDoctorId(),
                "✅ تمت الموافقة الطبية على طلب الطوارئ للمريض " + emergency.getMember().getFullName() +
                        " في " + emergency.getLocation()
        );

        return emergencyRequestMapper.toDto(emergency);
    }

    // ✅ رفض طلب الطوارئ
    @Transactional
    public EmergencyRequestDTO rejectEmergencyRequest(UUID id, RejectEmergencyDTO dto) {
        log.info("🔹 Rejecting emergency request: {}", id);

        EmergencyRequest emergency = emergencyRepo.findById(id)
                .orElseThrow(() -> {
                    log.error("❌ Emergency request not found: {}", id);
                    return new NotFoundException("EMERGENCY_REQUEST_NOT_FOUND");
                });

        if (emergency.getStatus() != EmergencyStatus.PENDING_MEDICAL) {  // ✅ Changed from PENDING to PENDING_MEDICAL
            log.warn("⚠️ Emergency request {} is already processed", id);
            throw new BadRequestException("REQUEST_ALREADY_PROCESSED");
        }

        emergency.setStatus(EmergencyStatus.REJECTED_BY_MEDICAL);  // ✅ Changed from REJECTED to REJECTED_BY_MEDICAL
        emergency.setRejectedAt(Instant.now());
        emergency.setRejectionReason(dto.getReason());

        emergencyRepo.save(emergency);

        log.info("✅ Emergency request {} rejected with reason: {}", id, dto.getReason());

        // 🔔 إشعار للعميل
        notificationService.sendToUser(
                emergency.getMember().getId(),
                "❌ تم رفض طلب الطوارئ. السبب: " + dto.getReason()
        );

        // 🔔 إشعار للدكتور الذي أنشأ الطلب
        notificationService.sendToUser(
                emergency.getDoctorId(),
                "❌ تم رفض طلب الطوارئ للمريض " + emergency.getMember().getFullName() +
                        " في " + emergency.getLocation() + ". السبب: " + dto.getReason()
        );

        return emergencyRequestMapper.toDto(emergency);
    }

}

