package com.insurancesystem.Repository;

import com.insurancesystem.Model.Entity.Client;
import com.insurancesystem.Model.Entity.EmergencyRequest;
import com.insurancesystem.Model.Entity.Enums.EmergencyStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EmergencyRequestRepository extends JpaRepository<EmergencyRequest, UUID> {
    List<EmergencyRequest> findByMember(Client member);
    List<EmergencyRequest> findByStatus(EmergencyStatus status);

}
