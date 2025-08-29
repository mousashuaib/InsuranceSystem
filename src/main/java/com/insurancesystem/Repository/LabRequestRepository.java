package com.insurancesystem.Repository;

import com.insurancesystem.Model.Entity.LabRequest;
import com.insurancesystem.Model.Entity.Enums.LabRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LabRequestRepository extends JpaRepository<LabRequest, UUID> {
    List<LabRequest> findByMemberId(UUID memberId);
    List<LabRequest> findByStatus(LabRequestStatus status);
}
