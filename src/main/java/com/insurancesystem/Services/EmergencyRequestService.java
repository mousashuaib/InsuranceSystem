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
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
@Service
@RequiredArgsConstructor
public class EmergencyRequestService {

    private final EmergencyRequestRepository emergencyRepo;
    private final ClientRepository clientRepo;
    private final EmergencyRequestMapper emergencyRequestMapper;
    private final NotificationService notificationService;


    //  إنشاء طلب طوارئ
    public EmergencyRequestDTO createEmergencyRequest(UUID memberId, CreateEmergencyRequestDTO dto) {
        Client member = clientRepo.findById(memberId)
                .orElseThrow(() -> new NotFoundException("Member not found"));

        EmergencyRequest emergency = EmergencyRequest.builder()
                .member(member)
                .description(dto.getDescription())
                .location(dto.getLocation())
                .contactPhone(dto.getContactPhone())
                .incidentDate(dto.getIncidentDate())
                .notes(dto.getNotes())
                .status(EmergencyStatus.PENDING)
                .submittedAt(Instant.now())
                .build();

        emergencyRepo.save(emergency);

        notificationService.sendToUser(
                member.getId(),
                "تم استلام طلب الطوارئ الخاص بك وهو الآن قيد المراجعة."
        );

        //  إشعار لكل مدراء الطوارئ
        notificationService.sendToRole(
                RoleName.EMERGENCY_MANAGER,
                "طلب طوارئ جديد من " + member.getFullName() +
                        " في " + dto.getLocation()
        );


        // إشعار لكل مدراء التأمين
        notificationService.sendToRole(
                RoleName.INSURANCE_MANAGER,
                "طلب طوارئ جديد من " + member.getFullName() +
                        " في " + dto.getLocation()
        );



        return emergencyRequestMapper.toDto(emergency);
    }

    //  طلبات العضو نفسه
    public List<EmergencyRequestDTO> getMemberEmergencyRequests(UUID memberId) {
        Client member = clientRepo.findById(memberId)
                .orElseThrow(() -> new NotFoundException("Member not found"));
        return emergencyRepo.findByMember(member)
                .stream()
                .map(emergencyRequestMapper::toDto)
                .toList();
    }

    //  عرض كل الطلبات الـ Pending
    public List<EmergencyRequestDTO> getAllPendingRequests() {
        return emergencyRepo.findByStatus(EmergencyStatus.PENDING)
                .stream()
                .map(emergencyRequestMapper::toDto)
                .toList();
    }

    //  الموافقة
    public EmergencyRequestDTO approveEmergencyRequest(UUID id) {
        EmergencyRequest emergency = emergencyRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Emergency request not found"));

        if (emergency.getStatus() != EmergencyStatus.PENDING) {
            throw new BadRequestException("Request is already processed");
        }

        emergency.setStatus(EmergencyStatus.APPROVED);
        emergency.setApprovedAt(Instant.now());
        emergencyRepo.save(emergency);
        notificationService.sendToUser(
                emergency.getMember().getId(),
                "تمت الموافقة على طلب الطوارئ الخاص بك."
        );
        notificationService.markNotificationAsReadByMessage(
                RoleName.EMERGENCY_MANAGER,
                "طلب طوارئ جديد من " + emergency.getMember().getFullName() +
                        " في " + emergency.getLocation()
        );
        return emergencyRequestMapper.toDto(emergency);
    }

    // الرفض
    public EmergencyRequestDTO rejectEmergencyRequest(UUID id, RejectEmergencyDTO dto) {
        EmergencyRequest emergency = emergencyRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Emergency request not found"));

        if (emergency.getStatus() != EmergencyStatus.PENDING) {
            throw new BadRequestException("Request is already processed");
        }

        emergency.setStatus(EmergencyStatus.REJECTED);
        emergency.setRejectedAt(Instant.now());
        emergency.setRejectionReason(dto.getReason());
        emergencyRepo.save(emergency);

        notificationService.sendToUser(
                emergency.getMember().getId(),
                "تم رفض طلب الطوارئ. السبب: " + dto.getReason()
        );
        notificationService.markNotificationAsReadByMessage(
                RoleName.EMERGENCY_MANAGER,
                "طلب طوارئ جديد من " + emergency.getMember().getFullName() +
                        " في " + emergency.getLocation()
        );

        return emergencyRequestMapper.toDto(emergency);
    }

}
