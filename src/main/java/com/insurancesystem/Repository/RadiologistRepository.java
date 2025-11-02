package com.insurancesystem.Repository;

import com.insurancesystem.Model.Entity.RadiologyRequest;
import com.insurancesystem.Model.Entity.Enums.LabRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RadiologistRepository extends JpaRepository<RadiologyRequest, UUID> {

    // Find all radiology requests assigned to a specific radiologist (using radiologist's ID)
    List<RadiologyRequest> findByRadiologistId(UUID radiologistId);

    // Count the number of requests with a specific status for a radiologist
    long countByStatusAndRadiologistId(LabRequestStatus status, UUID radiologistId);

    // Count the number of requests with a specific status and a non-null result URL (completed requests)
    long countByStatusAndRadiologistIdAndResultUrlNotNull(LabRequestStatus status, UUID radiologistId);

    // Count all radiology requests handled by a specific radiologist
    long countByRadiologistId(UUID radiologistId);

    // Find all radiology requests for a specific member (patient)
    List<RadiologyRequest> findByMemberId(UUID memberId);

    // Find all radiology requests for a specific doctor
    List<RadiologyRequest> findByDoctorId(UUID doctorId);

    // Find all radiology requests for a specific status (completed or pending) and assigned radiologist
    List<RadiologyRequest> findByStatusAndRadiologistIdAndResultUrlNotNull(LabRequestStatus status, UUID radiologistId);
}
