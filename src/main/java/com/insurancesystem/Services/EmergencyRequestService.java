package com.insurancesystem.Services;

import com.insurancesystem.Exception.BadRequestException;
import com.insurancesystem.Exception.NotFoundException;
import com.insurancesystem.Model.Dto.CreateEmergencyRequestDTO;
import com.insurancesystem.Model.Dto.EmergencyRequestDTO;
import com.insurancesystem.Model.Dto.RejectEmergencyDTO;
import com.insurancesystem.Model.Entity.Client;
import com.insurancesystem.Model.Entity.EmergencyRequest;
import com.insurancesystem.Model.Entity.FamilyMember;
import com.insurancesystem.Model.Entity.Enums.EmergencyStatus;
import com.insurancesystem.Model.Entity.Enums.RoleName;
import com.insurancesystem.Model.MapStruct.EmergencyRequestMapper;
import com.insurancesystem.Repository.ClientRepository;
import com.insurancesystem.Repository.EmergencyRequestRepository;
import com.insurancesystem.Repository.FamilyMemberRepository;
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
    private final FamilyMemberRepository familyMemberRepo;
    private final EmergencyRequestMapper emergencyRequestMapper;
    private final NotificationService notificationService;

    // ✅ Doctor creates emergency request for a client or family member
    @Transactional
    public EmergencyRequestDTO createEmergencyRequest(UUID doctorId, CreateEmergencyRequestDTO dto) {
        log.info("🔹 Creating emergency request for: {}", dto.getClientName());
        log.info("🔹 Is Family Member: {}, MemberId: {}, FamilyMemberId: {}", 
            dto.getIsFamilyMember(), dto.getMemberId(), dto.getFamilyMemberId());

        // ✅ التحقق من وجود الـ Doctor
        Client doctor = clientRepo.findById(doctorId)
                .orElseThrow(() -> {
                    log.error("❌ Doctor not found: {}", doctorId);
                    return new NotFoundException("DOCTOR_NOT_FOUND");
                });

        Client member;
        FamilyMember familyMember = null;

        // ✅ إذا كان الطلب لعضو عائلة
        if (dto.getIsFamilyMember() != null && dto.getIsFamilyMember() && dto.getFamilyMemberId() != null) {
            // البحث عن FamilyMember
            familyMember = familyMemberRepo.findById(dto.getFamilyMemberId())
                    .orElseThrow(() -> {
                        log.error("❌ Family member not found: {}", dto.getFamilyMemberId());
                        return new NotFoundException("FAMILY_MEMBER_NOT_FOUND");
                    });
            
            // الحصول على العميل الرئيسي
            member = familyMember.getClient();
            
            log.info("✅ Family member found: {} (ID: {}), Main Client: {} (ID: {})", 
                familyMember.getFullName(), familyMember.getId(), 
                member.getFullName(), member.getId());
        } else if (dto.getMemberId() != null) {
            // ✅ البحث عن Client باستخدام memberId
            member = clientRepo.findById(dto.getMemberId())
                    .orElseThrow(() -> {
                        log.error("❌ Client not found by ID: {}", dto.getMemberId());
                        return new NotFoundException("CLIENT_NOT_FOUND");
                    });
            log.info("✅ Main client found: {} (ID: {})", member.getFullName(), member.getId());
        } else {
            // ✅ البحث عن Client باستخدام اسمه (fallback)
            member = clientRepo.findByFullName(dto.getClientName())
                    .orElseThrow(() -> {
                        log.error("❌ Client not found by name: {}", dto.getClientName());
                        return new NotFoundException("CLIENT_NOT_FOUND");
                    });
            log.info("✅ Client found by name: {} (ID: {})", member.getFullName(), member.getId());
        }

        // Store only the notes entered by the user (no family member info in notes)
        // Family member info will be extracted by mapper from database when needed
        String notes = dto.getNotes() != null ? dto.getNotes().trim() : "";

        EmergencyRequest emergency = EmergencyRequest.builder()
                .member(member)  // دائماً العميل الرئيسي (جميع الطلبات مرتبطة بالعميل الرئيسي)
                .doctorId(doctorId)  // ✅ حفظ معرف الدكتور
                .familyMemberId(familyMember != null ? familyMember.getId() : null)  // ✅ حفظ معرف عضو العائلة إذا كان موجوداً
                .description(dto.getDescription())
                .location(dto.getLocation())
                .contactPhone(dto.getContactPhone())
                .incidentDate(dto.getIncidentDate())
                .notes(notes)  // ✅ Only user-entered notes (family member info fetched from DB by mapper)
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

        // Force initialization of member fields before mapping
        if (emergency.getMember() != null) {
            Client m = emergency.getMember();
            m.getFullName();
            m.getDateOfBirth();
            m.getGender();
            m.getNationalId();
            m.getEmployeeId();
            if (m.getUniversityCardImages() != null) {
                m.getUniversityCardImages().size();
            }
        }

        return emergencyRequestMapper.toDto(emergency, familyMemberRepo);
    }

    // ✅ طلبات العضو نفسه
    public List<EmergencyRequestDTO> getMemberEmergencyRequests(UUID memberId) {
        log.info("🔹 Fetching emergency requests for member: {}", memberId);

        Client member = clientRepo.findById(memberId)
                .orElseThrow(() -> {
                    log.error("❌ Member not found: {}", memberId);
                    return new NotFoundException("MEMBER_NOT_FOUND");
                });

        List<EmergencyRequest> emergencies = emergencyRepo.findByMemberWithMember(member);
        
        // Force initialization of member fields before mapping
        for (EmergencyRequest emergency : emergencies) {
            if (emergency.getMember() != null) {
                Client m = emergency.getMember();
                m.getFullName();
                m.getDateOfBirth();
                m.getGender();
                m.getNationalId();
                m.getEmployeeId();
                if (m.getUniversityCardImages() != null) {
                    m.getUniversityCardImages().size();
                }
            }
        }

        List<EmergencyRequestDTO> requests = emergencies.stream()
                .map(e -> emergencyRequestMapper.toDto(e, familyMemberRepo))
                .toList();

        log.info("✅ Found {} emergency requests for member", requests.size());

        return requests;
    }

    // ✅ جميع طلبات الطوارئ
    public List<EmergencyRequestDTO> getAllEmergencyRequests() {
        log.info("🔹 Fetching all emergency requests");

        List<EmergencyRequest> emergencies = emergencyRepo.findAllWithMember();
        
        // Force initialization of member fields before mapping
        for (EmergencyRequest emergency : emergencies) {
            if (emergency.getMember() != null) {
                Client m = emergency.getMember();
                m.getFullName();
                m.getDateOfBirth();
                m.getGender();
                m.getNationalId();
                m.getEmployeeId();
                if (m.getUniversityCardImages() != null) {
                    m.getUniversityCardImages().size();
                }
            }
        }

        List<EmergencyRequestDTO> requests = emergencies.stream()
                .map(e -> emergencyRequestMapper.toDto(e, familyMemberRepo))
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
        List<EmergencyRequest> emergencies = emergencyRepo.findByDoctorIdWithMember(doctorId);
        
        // Force initialization of member fields before mapping
        for (EmergencyRequest emergency : emergencies) {
            if (emergency.getMember() != null) {
                Client m = emergency.getMember();
                m.getFullName();
                m.getDateOfBirth();
                m.getGender();
                m.getNationalId();
                m.getEmployeeId();
                if (m.getUniversityCardImages() != null) {
                    m.getUniversityCardImages().size();
                }
            }
        }

        List<EmergencyRequestDTO> requests = emergencies.stream()
                .map(e -> emergencyRequestMapper.toDto(e, familyMemberRepo))
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
        EmergencyRequest emergency = emergencyRepo.findByIdWithMember(requestId)
                .orElseThrow(() -> {
                    log.error("❌ Emergency request not found: {}", requestId);
                    return new NotFoundException("EMERGENCY_REQUEST_NOT_FOUND");
                });

        // Force initialization of member fields before mapping
        if (emergency.getMember() != null) {
            Client m = emergency.getMember();
            m.getFullName();
            m.getDateOfBirth();
            m.getGender();
            m.getNationalId();
            m.getEmployeeId();
            if (m.getUniversityCardImages() != null) {
                m.getUniversityCardImages().size();
            }
        }

        log.info("✅ Found emergency request: {}", requestId);

        return emergencyRequestMapper.toDto(emergency, familyMemberRepo);
    }

    // ✅ الموافقة على طلب الطوارئ
    @Transactional
    public EmergencyRequestDTO approveEmergencyRequest(UUID id) {
        log.info("🔹 Approving emergency request: {}", id);

        EmergencyRequest emergency = emergencyRepo.findByIdWithMember(id)
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

        // Force initialization of member fields before mapping
        if (emergency.getMember() != null) {
            Client m = emergency.getMember();
            m.getFullName();
            m.getDateOfBirth();
            m.getGender();
            m.getNationalId();
            m.getEmployeeId();
            if (m.getUniversityCardImages() != null) {
                m.getUniversityCardImages().size();
            }
        }

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

        return emergencyRequestMapper.toDto(emergency, familyMemberRepo);
    }

    // ✅ رفض طلب الطوارئ
    @Transactional
    public EmergencyRequestDTO rejectEmergencyRequest(UUID id, RejectEmergencyDTO dto) {
        log.info("🔹 Rejecting emergency request: {}", id);

        EmergencyRequest emergency = emergencyRepo.findByIdWithMember(id)
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

        // Force initialization of member fields before mapping
        if (emergency.getMember() != null) {
            Client m = emergency.getMember();
            m.getFullName();
            m.getDateOfBirth();
            m.getGender();
            m.getNationalId();
            m.getEmployeeId();
            if (m.getUniversityCardImages() != null) {
                m.getUniversityCardImages().size();
            }
        }

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

        return emergencyRequestMapper.toDto(emergency, familyMemberRepo);
    }

}

