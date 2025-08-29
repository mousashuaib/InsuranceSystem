package com.insurancesystem.Services;

import com.insurancesystem.Model.Dto.MembersActivityReportDto;
import com.insurancesystem.Repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MembersActivityReportService {

    private final ClientRepository clientRepo;
    private final ClaimRepository claimRepo;
    private final PrescriptionRepository prescriptionRepo;
    private final LabRequestRepository labRepo;
    private final EmergencyRequestRepository emergencyRepo;
    private final MedicalRecordRepository recordRepo;

    public MembersActivityReportDto generateReport() {
        long totalMembers = clientRepo.count();

        long membersWithClaims = claimRepo.findAll().stream()
                .map(c -> c.getMember().getId())
                .distinct()
                .count();

        long membersWithPrescriptions = prescriptionRepo.findAll().stream()
                .map(p -> p.getMember().getId())
                .distinct()
                .count();

        long membersWithLabRequests = labRepo.findAll().stream()
                .map(l -> l.getMember().getId())
                .distinct()
                .count();

        long membersWithEmergencyRequests = emergencyRepo.findAll().stream()
                .map(e -> e.getMember().getId())
                .distinct()
                .count();

        long membersWithMedicalRecords = recordRepo.findAll().stream()
                .map(r -> r.getMember().getId())
                .distinct()
                .count();

        return MembersActivityReportDto.builder()
                .totalMembers(totalMembers)
                .membersWithClaims(membersWithClaims)
                .membersWithPrescriptions(membersWithPrescriptions)
                .membersWithLabRequests(membersWithLabRequests)
                .membersWithEmergencyRequests(membersWithEmergencyRequests)
                .membersWithMedicalRecords(membersWithMedicalRecords)
                .build();
    }
}
