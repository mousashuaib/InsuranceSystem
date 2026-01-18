package com.insurancesystem.Services;

import com.insurancesystem.Exception.BadRequestException;
import com.insurancesystem.Exception.NotFoundException;
import com.insurancesystem.Model.Entity.Client;
import com.insurancesystem.Model.Entity.DoctorMedicineAssignment;
import com.insurancesystem.Model.Entity.Enums.RoleName;
import com.insurancesystem.Model.Entity.MedicinePrice;
import com.insurancesystem.Repository.ClientRepository;
import com.insurancesystem.Repository.DoctorMedicineAssignmentRepository;
import com.insurancesystem.Repository.MedicinePriceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DoctorMedicineService {

    private final DoctorMedicineAssignmentRepository assignmentRepository;
    private final ClientRepository clientRepository;
    private final MedicinePriceRepository medicinePriceRepository;

    /**
     * Assign a medicine to a doctor
     */
    public DoctorMedicineAssignment assignMedicineToDoctor(
            UUID doctorId,
            UUID medicineId,
            Client assignedBy,
            String specialization,
            Integer maxDailyPrescriptions,
            Integer maxQuantityPerPrescription,
            String notes
    ) {
        Client doctor = clientRepository.findById(doctorId)
                .orElseThrow(() -> new NotFoundException("Doctor not found"));

        // Verify the client is a doctor
        if (!doctor.hasRole(RoleName.DOCTOR)) {
            throw new BadRequestException("User is not a doctor");
        }

        MedicinePrice medicine = medicinePriceRepository.findById(medicineId)
                .orElseThrow(() -> new NotFoundException("Medicine not found"));

        // Check if assignment already exists
        if (assignmentRepository.existsByDoctorAndMedicineAndActiveTrue(doctor, medicine)) {
            throw new BadRequestException("Medicine is already assigned to this doctor");
        }

        // Check if there's an inactive assignment to reactivate
        var existingAssignment = assignmentRepository.findByDoctorAndMedicine(doctor, medicine);
        if (existingAssignment.isPresent()) {
            DoctorMedicineAssignment assignment = existingAssignment.get();
            assignment.setActive(true);
            assignment.setAssignedBy(assignedBy);
            assignment.setSpecialization(specialization != null ? specialization : doctor.getSpecialization());
            assignment.setMaxDailyPrescriptions(maxDailyPrescriptions);
            assignment.setMaxQuantityPerPrescription(maxQuantityPerPrescription);
            assignment.setNotes(notes);
            return assignmentRepository.save(assignment);
        }

        // Create new assignment
        DoctorMedicineAssignment assignment = DoctorMedicineAssignment.builder()
                .doctor(doctor)
                .medicine(medicine)
                .assignedBy(assignedBy)
                .specialization(specialization != null ? specialization : doctor.getSpecialization())
                .maxDailyPrescriptions(maxDailyPrescriptions)
                .maxQuantityPerPrescription(maxQuantityPerPrescription)
                .notes(notes)
                .active(true)
                .build();

        log.info("Assigning medicine {} to doctor {} by manager {}",
                medicine.getDrugName(), doctor.getFullName(), assignedBy.getFullName());

        return assignmentRepository.save(assignment);
    }

    /**
     * Bulk assign medicines to a doctor
     */
    public int bulkAssignMedicinesToDoctor(
            UUID doctorId,
            List<UUID> medicineIds,
            Client assignedBy,
            String specialization
    ) {
        Client doctor = clientRepository.findById(doctorId)
                .orElseThrow(() -> new NotFoundException("Doctor not found"));

        if (!doctor.hasRole(RoleName.DOCTOR)) {
            throw new BadRequestException("User is not a doctor");
        }

        int assigned = 0;
        for (UUID medicineId : medicineIds) {
            try {
                MedicinePrice medicine = medicinePriceRepository.findById(medicineId).orElse(null);
                if (medicine == null) continue;

                if (!assignmentRepository.existsByDoctorAndMedicineAndActiveTrue(doctor, medicine)) {
                    var existing = assignmentRepository.findByDoctorAndMedicine(doctor, medicine);
                    if (existing.isPresent()) {
                        existing.get().setActive(true);
                        existing.get().setAssignedBy(assignedBy);
                        assignmentRepository.save(existing.get());
                    } else {
                        DoctorMedicineAssignment assignment = DoctorMedicineAssignment.builder()
                                .doctor(doctor)
                                .medicine(medicine)
                                .assignedBy(assignedBy)
                                .specialization(specialization != null ? specialization : doctor.getSpecialization())
                                .active(true)
                                .build();
                        assignmentRepository.save(assignment);
                    }
                    assigned++;
                }
            } catch (Exception e) {
                log.warn("Failed to assign medicine {} to doctor {}: {}", medicineId, doctorId, e.getMessage());
            }
        }

        log.info("Bulk assigned {} medicines to doctor {}", assigned, doctor.getFullName());
        return assigned;
    }

    /**
     * Assign all medicines with a specific specialization to all doctors with that specialization
     */
    public int assignMedicinesBySpecialization(String specialization, Client assignedBy) {
        // Get all doctors with this specialization
        List<Client> doctors = clientRepository.findAll().stream()
                .filter(c -> c.hasRole(RoleName.DOCTOR))
                .filter(c -> specialization.equalsIgnoreCase(c.getSpecialization()))
                .toList();

        // Get all active medicines
        List<MedicinePrice> medicines = medicinePriceRepository.findByActiveTrue();

        int assigned = 0;
        for (Client doctor : doctors) {
            for (MedicinePrice medicine : medicines) {
                if (!assignmentRepository.existsByDoctorAndMedicineAndActiveTrue(doctor, medicine)) {
                    DoctorMedicineAssignment assignment = DoctorMedicineAssignment.builder()
                            .doctor(doctor)
                            .medicine(medicine)
                            .assignedBy(assignedBy)
                            .specialization(specialization)
                            .active(true)
                            .build();
                    assignmentRepository.save(assignment);
                    assigned++;
                }
            }
        }

        log.info("Assigned {} medicine-doctor pairs for specialization {}", assigned, specialization);
        return assigned;
    }

    /**
     * Revoke a medicine assignment from a doctor
     */
    public void revokeMedicineFromDoctor(UUID assignmentId) {
        DoctorMedicineAssignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new NotFoundException("Assignment not found"));

        assignment.setActive(false);
        assignmentRepository.save(assignment);

        log.info("Revoked medicine {} from doctor {}",
                assignment.getMedicine().getDrugName(), assignment.getDoctor().getFullName());
    }

    /**
     * Bulk revoke medicines from a doctor
     */
    public int bulkRevokeMedicinesFromDoctor(UUID doctorId, List<UUID> medicineIds) {
        Client doctor = clientRepository.findById(doctorId)
                .orElseThrow(() -> new NotFoundException("Doctor not found"));

        int revoked = 0;
        for (UUID medicineId : medicineIds) {
            MedicinePrice medicine = medicinePriceRepository.findById(medicineId).orElse(null);
            if (medicine == null) continue;

            var assignment = assignmentRepository.findByDoctorAndMedicine(doctor, medicine);
            if (assignment.isPresent() && assignment.get().isActive()) {
                assignment.get().setActive(false);
                assignmentRepository.save(assignment.get());
                revoked++;
            }
        }

        log.info("Revoked {} medicines from doctor {}", revoked, doctor.getFullName());
        return revoked;
    }

    /**
     * Get all medicines assigned to a doctor
     */
    @Transactional(readOnly = true)
    public List<DoctorMedicineAssignment> getDoctorMedicines(UUID doctorId) {
        Client doctor = clientRepository.findById(doctorId)
                .orElseThrow(() -> new NotFoundException("Doctor not found"));

        return assignmentRepository.findByDoctorAndActiveTrue(doctor);
    }

    /**
     * Get paginated medicines assigned to a doctor
     */
    @Transactional(readOnly = true)
    public Page<DoctorMedicineAssignment> getDoctorMedicines(UUID doctorId, Pageable pageable) {
        Client doctor = clientRepository.findById(doctorId)
                .orElseThrow(() -> new NotFoundException("Doctor not found"));

        return assignmentRepository.findByDoctorAndActiveTrue(doctor, pageable);
    }

    /**
     * Check if a doctor can prescribe a specific medicine
     */
    @Transactional(readOnly = true)
    public boolean canDoctorPrescribeMedicine(UUID doctorId, UUID medicineId) {
        Client doctor = clientRepository.findById(doctorId).orElse(null);
        MedicinePrice medicine = medicinePriceRepository.findById(medicineId).orElse(null);

        if (doctor == null || medicine == null) return false;

        return assignmentRepository.existsByDoctorAndMedicineAndActiveTrue(doctor, medicine);
    }

    /**
     * Get all assignments (for manager view)
     */
    @Transactional(readOnly = true)
    public Page<DoctorMedicineAssignment> getAllAssignments(Pageable pageable) {
        return assignmentRepository.findByActiveTrue(pageable);
    }

    /**
     * Search assignments
     */
    @Transactional(readOnly = true)
    public Page<DoctorMedicineAssignment> searchAssignments(String search, Pageable pageable) {
        return assignmentRepository.searchByDoctorOrMedicine(search, pageable);
    }

    /**
     * Get assignments by specialization
     */
    @Transactional(readOnly = true)
    public Page<DoctorMedicineAssignment> getAssignmentsBySpecialization(String specialization, Pageable pageable) {
        return assignmentRepository.findBySpecializationAndActiveTrue(specialization, pageable);
    }

    /**
     * Get all doctors (for dropdown)
     */
    @Transactional(readOnly = true)
    public List<Client> getAllDoctors() {
        return clientRepository.findAll().stream()
                .filter(c -> c.hasRole(RoleName.DOCTOR))
                .toList();
    }

    /**
     * Get all active medicines (for dropdown)
     */
    @Transactional(readOnly = true)
    public List<MedicinePrice> getAllMedicines() {
        return medicinePriceRepository.findByActiveTrue();
    }

    /**
     * Get distinct specializations
     */
    @Transactional(readOnly = true)
    public List<String> getDistinctSpecializations() {
        return clientRepository.findAll().stream()
                .filter(c -> c.hasRole(RoleName.DOCTOR))
                .map(Client::getSpecialization)
                .filter(s -> s != null && !s.isEmpty())
                .distinct()
                .sorted()
                .toList();
    }

    /**
     * Update assignment restrictions
     */
    public DoctorMedicineAssignment updateAssignment(
            UUID assignmentId,
            Integer maxDailyPrescriptions,
            Integer maxQuantityPerPrescription,
            String notes
    ) {
        DoctorMedicineAssignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new NotFoundException("Assignment not found"));

        if (maxDailyPrescriptions != null) {
            assignment.setMaxDailyPrescriptions(maxDailyPrescriptions);
        }
        if (maxQuantityPerPrescription != null) {
            assignment.setMaxQuantityPerPrescription(maxQuantityPerPrescription);
        }
        if (notes != null) {
            assignment.setNotes(notes);
        }

        return assignmentRepository.save(assignment);
    }
}
