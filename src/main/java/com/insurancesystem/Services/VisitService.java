package com.insurancesystem.Services;

import com.insurancesystem.Exception.BadRequestException;
import com.insurancesystem.Exception.NotFoundException;
import com.insurancesystem.Model.Dto.CreateVisitDTO;
import com.insurancesystem.Model.Dto.VisitDTO;
import com.insurancesystem.Model.Entity.Client;
import com.insurancesystem.Model.Entity.Enums.RoleName;
import com.insurancesystem.Model.Entity.FamilyMember;
import com.insurancesystem.Model.Entity.Visit;
import com.insurancesystem.Repository.ClientRepository;
import com.insurancesystem.Repository.FamilyMemberRepository;
import com.insurancesystem.Repository.VisitRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class VisitService {

    private static final int MAX_YEARLY_VISITS = 12;

    private final VisitRepository visitRepository;
    private final ClientRepository clientRepository;
    private final FamilyMemberRepository familyMemberRepository;

    /**
     * Create a new visit with all business rules validation
     */
    @Transactional
    public VisitDTO createVisit(CreateVisitDTO dto) {
        log.info("Creating visit: patientId={}, familyMemberId={}, doctorId={}, visitDate={}",
                dto.getPatientId(), dto.getFamilyMemberId(), dto.getDoctorId(), dto.getVisitDate());

        // Validate patient (either employee or family member, but not both)
        if (dto.getPatientId() != null && dto.getFamilyMemberId() != null) {
            throw new BadRequestException("Cannot specify both patientId and familyMemberId. Please specify only one.");
        }
        if (dto.getPatientId() == null && dto.getFamilyMemberId() == null) {
            throw new BadRequestException("Either patientId or familyMemberId must be provided.");
        }

        // Get patient (employee or family member)
        Client patient = null;
        FamilyMember familyMember = null;
        UUID patientIdentifier;

        if (dto.getPatientId() != null) {
            patient = clientRepository.findById(dto.getPatientId())
                    .orElseThrow(() -> new NotFoundException("Patient (employee) not found with ID: " + dto.getPatientId()));
            patientIdentifier = patient.getId();
        } else {
            familyMember = familyMemberRepository.findById(dto.getFamilyMemberId())
                    .orElseThrow(() -> new NotFoundException("Family member not found with ID: " + dto.getFamilyMemberId()));
            patientIdentifier = familyMember.getId();
        }

        // Get doctor
        Client doctor = clientRepository.findById(dto.getDoctorId())
                .orElseThrow(() -> new NotFoundException("Doctor not found with ID: " + dto.getDoctorId()));

        // Validate doctor has DOCTOR role
        if (!doctor.hasRole(RoleName.DOCTOR)) {
            throw new BadRequestException("The specified client is not a doctor.");
        }

        // Get doctor's specialization
        String doctorSpecialization = doctor.getSpecialization();
        if (doctorSpecialization == null || doctorSpecialization.trim().isEmpty()) {
            throw new BadRequestException("Doctor does not have a specialization assigned.");
        }

        // Set visit date (default to today if not provided)
        LocalDate visitDate = dto.getVisitDate() != null ? dto.getVisitDate() : LocalDate.now();
        int visitYear = visitDate.getYear();

        // ========== BUSINESS RULE 1: Check if patient already visited a doctor with same specialization on same day ==========
        List<Visit> sameDaySameSpecializationVisits = visitRepository.findVisitsByPatientDateAndSpecialization(
                dto.getPatientId() != null ? patientIdentifier : null,
                dto.getFamilyMemberId() != null ? patientIdentifier : null,
                visitDate,
                doctorSpecialization
        );

        if (!sameDaySameSpecializationVisits.isEmpty()) {
            // Check if any of these visits are with a different doctor
            boolean hasDifferentDoctor = sameDaySameSpecializationVisits.stream()
                    .anyMatch(v -> !v.getDoctor().getId().equals(doctor.getId()));

            if (hasDifferentDoctor) {
                String patientName = patient != null ? patient.getFullName() : familyMember.getFullName();
                // Find the other doctor's name for better error message
                String otherDoctorName = sameDaySameSpecializationVisits.stream()
                        .filter(v -> !v.getDoctor().getId().equals(doctor.getId()))
                        .findFirst()
                        .map(v -> v.getDoctor().getFullName())
                        .orElse("طبيب آخر");
                
                throw new BadRequestException(
                        String.format("This patient cannot visit two doctors of the same specialization on the same day. " +
                                "Patient '%s' already visited doctor '%s' (%s) on %s. " +
                                "Please choose a different specialization or visit on another day.",
                                patientName, otherDoctorName, doctorSpecialization, visitDate)
                );
            }
        }

        // ========== BUSINESS RULE 2: Check if patient returns to same doctor on same day (FOLLOW-UP) ==========
        List<Visit> sameDaySameDoctorVisits = visitRepository.findVisitsByPatientDoctorAndDate(
                dto.getPatientId() != null ? patientIdentifier : null,
                dto.getFamilyMemberId() != null ? patientIdentifier : null,
                doctor.getId(),
                visitDate
        );

        Visit.VisitType visitType;
        Visit previousVisit = null;

        if (!sameDaySameDoctorVisits.isEmpty()) {
            // Same doctor, same day = FOLLOW-UP (not counted)
            visitType = Visit.VisitType.FOLLOW_UP;
            previousVisit = sameDaySameDoctorVisits.get(0); // Get the first visit of the day
            log.info("Visit is a FOLLOW-UP: same doctor ({}) and same day ({})", doctor.getFullName(), visitDate);
        } else {
            // ========== BUSINESS RULE 3: Check if visit is within 14 days of last visit with same doctor ==========
            List<Visit> lastVisitsWithSameDoctor = visitRepository.findLastVisitsByPatientAndDoctor(
                    dto.getPatientId() != null ? patientIdentifier : null,
                    dto.getFamilyMemberId() != null ? patientIdentifier : null,
                    doctor.getId()
            );

            if (!lastVisitsWithSameDoctor.isEmpty()) {
                Visit lastVisit = lastVisitsWithSameDoctor.get(0);
                long daysBetween = ChronoUnit.DAYS.between(lastVisit.getVisitDate(), visitDate);

                if (daysBetween <= 14 && daysBetween >= 0) {
                    // Same doctor, within 14 days = FOLLOW-UP (not counted)
                    visitType = Visit.VisitType.FOLLOW_UP;
                    previousVisit = lastVisit;
                    log.info("Visit is a FOLLOW-UP: same doctor ({}) and within 14 days ({} days) of last visit",
                            doctor.getFullName(), daysBetween);
                } else {
                    // Same doctor but more than 14 days = NORMAL (counted)
                    visitType = Visit.VisitType.NORMAL;
                }
            } else {
                // First visit with this doctor = NORMAL (counted)
                visitType = Visit.VisitType.NORMAL;
            }
        }

        // ========== BUSINESS RULE 4: Check yearly visit limit (only for NORMAL visits) ==========
        if (visitType == Visit.VisitType.NORMAL) {
            Long currentYearlyCount = visitRepository.countNormalVisitsByPatientAndYear(
                    dto.getPatientId() != null ? patientIdentifier : null,
                    dto.getFamilyMemberId() != null ? patientIdentifier : null,
                    visitYear
            );

            if (currentYearlyCount >= MAX_YEARLY_VISITS) {
                String patientName = patient != null ? patient.getFullName() : familyMember.getFullName();
                throw new BadRequestException(
                        String.format("Patient '%s' has reached the maximum allowed visits (%d) for year %d. " +
                                "This visit cannot be counted. If this is a follow-up visit, please ensure it meets the follow-up criteria.",
                                patientName, MAX_YEARLY_VISITS, visitYear)
                );
            }

            log.info("Yearly visit count for patient: {}/{} (year {})", currentYearlyCount + 1, MAX_YEARLY_VISITS, visitYear);
        } else {
            log.info("Follow-up visit - not counted towards yearly limit");
        }

        // Create and save visit
        Visit visit = Visit.builder()
                .patient(patient)
                .familyMember(familyMember)
                .doctor(doctor)
                .doctorSpecialization(doctorSpecialization)
                .visitDate(visitDate)
                .visitType(visitType)
                .visitYear(visitYear)
                .previousVisit(previousVisit)
                .notes(dto.getNotes())
                .build();

        visit = visitRepository.save(visit);
        log.info("Visit created successfully: ID={}, Type={}", visit.getId(), visitType);

        // Convert to DTO
        return convertToDTO(visit);
    }

    /**
     * Get all visits for a patient (employee or family member)
     */
    public List<VisitDTO> getPatientVisits(UUID patientId, UUID familyMemberId) {
        if (patientId != null && familyMemberId != null) {
            throw new BadRequestException("Cannot specify both patientId and familyMemberId.");
        }
        if (patientId == null && familyMemberId == null) {
            throw new BadRequestException("Either patientId or familyMemberId must be provided.");
        }

        UUID patientIdentifier = patientId != null ? patientId : familyMemberId;

        List<Visit> visits = visitRepository.findAllVisitsByPatient(
                patientId != null ? patientIdentifier : null,
                familyMemberId != null ? patientIdentifier : null
        );

        return visits.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get visits for a patient in a specific year
     */
    public List<VisitDTO> getPatientVisitsByYear(UUID patientId, UUID familyMemberId, Integer year) {
        if (patientId != null && familyMemberId != null) {
            throw new BadRequestException("Cannot specify both patientId and familyMemberId.");
        }
        if (patientId == null && familyMemberId == null) {
            throw new BadRequestException("Either patientId or familyMemberId must be provided.");
        }
        if (year == null) {
            year = LocalDate.now().getYear();
        }

        UUID patientIdentifier = patientId != null ? patientId : familyMemberId;

        List<Visit> visits = visitRepository.findVisitsByPatientAndYear(
                patientId != null ? patientIdentifier : null,
                familyMemberId != null ? patientIdentifier : null,
                year
        );

        return visits.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get visit statistics for a patient in a year
     */
    public VisitDTO.VisitStatistics getPatientVisitStatistics(UUID patientId, UUID familyMemberId, Integer year) {
        if (patientId != null && familyMemberId != null) {
            throw new BadRequestException("Cannot specify both patientId and familyMemberId.");
        }
        if (patientId == null && familyMemberId == null) {
            throw new BadRequestException("Either patientId or familyMemberId must be provided.");
        }
        if (year == null) {
            year = LocalDate.now().getYear();
        }

        UUID patientIdentifier = patientId != null ? patientId : familyMemberId;

        Long normalVisits = visitRepository.countNormalVisitsByPatientAndYear(
                patientId != null ? patientIdentifier : null,
                familyMemberId != null ? patientIdentifier : null,
                year
        );

        List<Visit> allVisits = visitRepository.findVisitsByPatientAndYear(
                patientId != null ? patientIdentifier : null,
                familyMemberId != null ? patientIdentifier : null,
                year
        );

        long followUpVisits = allVisits.stream()
                .filter(v -> v.getVisitType() == Visit.VisitType.FOLLOW_UP)
                .count();

        return VisitDTO.VisitStatistics.builder()
                .year(year)
                .normalVisits(normalVisits.intValue())
                .followUpVisits((int) followUpVisits)
                .totalVisits(allVisits.size())
                .remainingVisits(MAX_YEARLY_VISITS - normalVisits.intValue())
                .maxYearlyVisits(MAX_YEARLY_VISITS)
                .build();
    }

    /**
     * Convert Visit entity to VisitDTO
     */
    private VisitDTO convertToDTO(Visit visit) {
        UUID patientId = visit.getPatient() != null ? visit.getPatient().getId() : null;
        String patientName = visit.getPatient() != null ? visit.getPatient().getFullName() : null;
        String employeeId = visit.getPatient() != null ? visit.getPatient().getEmployeeId() : null;

        UUID familyMemberId = visit.getFamilyMember() != null ? visit.getFamilyMember().getId() : null;
        String familyMemberName = visit.getFamilyMember() != null ? visit.getFamilyMember().getFullName() : null;

        UUID doctorId = visit.getDoctor().getId();
        String doctorName = visit.getDoctor().getFullName();

        UUID previousVisitId = visit.getPreviousVisit() != null ? visit.getPreviousVisit().getId() : null;

        // Get yearly visit count for this patient
        UUID patientIdentifier = patientId != null ? patientId : familyMemberId;
        Long yearlyVisitCount = visitRepository.countNormalVisitsByPatientAndYear(
                patientId != null ? patientIdentifier : null,
                familyMemberId != null ? patientIdentifier : null,
                visit.getVisitYear()
        );

        return VisitDTO.builder()
                .id(visit.getId())
                .patientId(patientId)
                .patientName(patientName)
                .employeeId(employeeId)
                .familyMemberId(familyMemberId)
                .familyMemberName(familyMemberName)
                .doctorId(doctorId)
                .doctorName(doctorName)
                .doctorSpecialization(visit.getDoctorSpecialization())
                .visitDate(visit.getVisitDate())
                .visitType(visit.getVisitType())
                .visitYear(visit.getVisitYear())
                .previousVisitId(previousVisitId)
                .notes(visit.getNotes())
                .createdAt(visit.getCreatedAt())
                .updatedAt(visit.getUpdatedAt())
                .yearlyVisitCount(yearlyVisitCount)
                .remainingVisits((long)(MAX_YEARLY_VISITS - yearlyVisitCount.intValue()))
                .build();
    }

}

