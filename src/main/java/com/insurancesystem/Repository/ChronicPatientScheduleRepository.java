package com.insurancesystem.Repository;

import com.insurancesystem.Model.Entity.ChronicPatientSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface ChronicPatientScheduleRepository extends JpaRepository<ChronicPatientSchedule, UUID> {

    List<ChronicPatientSchedule> findByPatientIdAndIsActiveTrue(UUID patientId);

    @Query("SELECT s FROM ChronicPatientSchedule s WHERE s.isActive = true AND s.nextSendDate <= :date")
    List<ChronicPatientSchedule> findSchedulesDueForSending(LocalDate date);

    List<ChronicPatientSchedule> findByIsActiveTrueOrderByCreatedAtDesc();
}

