package com.insurancesystem.Repository;

import com.insurancesystem.Model.Entity.Client;
import com.insurancesystem.Model.Entity.DoctorMedicineAssignment;
import com.insurancesystem.Model.Entity.MedicinePrice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DoctorMedicineAssignmentRepository extends JpaRepository<DoctorMedicineAssignment, UUID> {

    // Find all assignments for a specific doctor
    List<DoctorMedicineAssignment> findByDoctorAndActiveTrue(Client doctor);

    Page<DoctorMedicineAssignment> findByDoctorAndActiveTrue(Client doctor, Pageable pageable);

    // Find all assignments for a specific medicine
    List<DoctorMedicineAssignment> findByMedicineAndActiveTrue(MedicinePrice medicine);

    // Find by specialization
    List<DoctorMedicineAssignment> findBySpecializationAndActiveTrue(String specialization);

    Page<DoctorMedicineAssignment> findBySpecializationAndActiveTrue(String specialization, Pageable pageable);

    // Check if doctor can prescribe a specific medicine
    boolean existsByDoctorAndMedicineAndActiveTrue(Client doctor, MedicinePrice medicine);

    // Find specific assignment
    Optional<DoctorMedicineAssignment> findByDoctorAndMedicine(Client doctor, MedicinePrice medicine);

    // Find all active assignments
    Page<DoctorMedicineAssignment> findByActiveTrue(Pageable pageable);

    // Search by doctor name or medicine name
    @Query("SELECT dma FROM DoctorMedicineAssignment dma " +
           "WHERE dma.active = true " +
           "AND (LOWER(dma.doctor.fullName) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(dma.medicine.drugName) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<DoctorMedicineAssignment> searchByDoctorOrMedicine(@Param("search") String search, Pageable pageable);

    // Get all medicines assigned to doctors with a specific specialization
    @Query("SELECT DISTINCT dma.medicine FROM DoctorMedicineAssignment dma " +
           "WHERE dma.specialization = :specialization AND dma.active = true")
    List<MedicinePrice> findMedicinesBySpecialization(@Param("specialization") String specialization);

    // Get all doctors who can prescribe a specific medicine
    @Query("SELECT DISTINCT dma.doctor FROM DoctorMedicineAssignment dma " +
           "WHERE dma.medicine.id = :medicineId AND dma.active = true")
    List<Client> findDoctorsByMedicine(@Param("medicineId") UUID medicineId);

    // Count assignments by specialization
    long countBySpecializationAndActiveTrue(String specialization);

    // Find assignments by doctor ID
    @Query("SELECT dma FROM DoctorMedicineAssignment dma " +
           "WHERE dma.doctor.id = :doctorId AND dma.active = true")
    List<DoctorMedicineAssignment> findByDoctorId(@Param("doctorId") UUID doctorId);

    // Bulk check: Get all medicine IDs assigned to a doctor
    @Query("SELECT dma.medicine.id FROM DoctorMedicineAssignment dma " +
           "WHERE dma.doctor.id = :doctorId AND dma.active = true")
    List<UUID> findMedicineIdsByDoctorId(@Param("doctorId") UUID doctorId);
}
