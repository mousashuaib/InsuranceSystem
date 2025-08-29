package com.insurancesystem.Repository;

import com.insurancesystem.Model.Entity.MedicalRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, UUID> {
    List<MedicalRecord> findByMemberId(UUID memberId);
}
