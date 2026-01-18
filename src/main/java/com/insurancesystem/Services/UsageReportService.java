package com.insurancesystem.Services;

import com.insurancesystem.Model.Dto.UsageReportDto;
import com.insurancesystem.Model.Entity.Enums.*;
import com.insurancesystem.Repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UsageReportService {

    private final HealthcareProviderClaimRepository claimRepo;
    private final PrescriptionRepository prescriptionRepo;
    private final LabRequestRepository labRepo;
    private final EmergencyRequestRepository emergencyRepo;
    private final MedicalRecordRepository medicalRecordRepository;

    public UsageReportDto generateReport() {
        return UsageReportDto.builder()
                // Claims
                .totalClaims(claimRepo.count())
                .approvedClaims(
                        claimRepo.countByStatus(ClaimStatus.APPROVED_FINAL)
                )
                .rejectedClaims(
                        claimRepo.countByStatus(ClaimStatus.REJECTED_FINAL)
                )
                .pendingClaims(
                        claimRepo.countByStatus(ClaimStatus.PENDING_MEDICAL)
                                + claimRepo.countByStatus(ClaimStatus.RETURNED_FOR_REVIEW)
                )


                // Prescriptions
                .totalPrescriptions(prescriptionRepo.count())
                .verifiedPrescriptions(prescriptionRepo.findByStatus(PrescriptionStatus.VERIFIED).size())
                .rejectedPrescriptions(prescriptionRepo.findByStatus(PrescriptionStatus.REJECTED).size())
                .pendingPrescriptions(prescriptionRepo.findByStatus(PrescriptionStatus.PENDING).size())

                // Lab Requests
                .totalLabRequests(labRepo.count())
                .completedLabRequests(labRepo.findByStatus(LabRequestStatus.COMPLETED).size())
                .pendingLabRequests(labRepo.findByStatus(LabRequestStatus.PENDING).size())

                // Emergency Requests (count both new and legacy status values)
                .totalEmergencyRequests(emergencyRepo.count())
                .approvedEmergencyRequests(
                        emergencyRepo.findByStatus(EmergencyStatus.APPROVED_BY_MEDICAL).size() +
                        emergencyRepo.findByStatus(EmergencyStatus.APPROVED).size()
                )
                .rejectedEmergencyRequests(
                        emergencyRepo.findByStatus(EmergencyStatus.REJECTED_BY_MEDICAL).size() +
                        emergencyRepo.findByStatus(EmergencyStatus.REJECTED).size()
                )
                .pendingEmergencyRequests(
                        emergencyRepo.findByStatus(EmergencyStatus.PENDING_MEDICAL).size() +
                        emergencyRepo.findByStatus(EmergencyStatus.PENDING).size()
                )

                // Medical Records
                .totalMedicalRecords(medicalRecordRepository.count())

                .build();
    }
}
