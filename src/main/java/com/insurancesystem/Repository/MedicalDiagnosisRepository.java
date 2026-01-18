package com.insurancesystem.Repository;

import com.insurancesystem.Model.Entity.MedicalDiagnosis;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MedicalDiagnosisRepository extends JpaRepository<MedicalDiagnosis, UUID> {

    List<MedicalDiagnosis> findByActiveTrue();

    Page<MedicalDiagnosis> findByActiveTrue(Pageable pageable);

    Optional<MedicalDiagnosis> findByEnglishNameIgnoreCase(String englishName);

    Optional<MedicalDiagnosis> findByArabicName(String arabicName);

    @Query("SELECT m FROM MedicalDiagnosis m WHERE m.active = true AND " +
           "(LOWER(m.englishName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "m.arabicName LIKE CONCAT('%', :search, '%'))")
    Page<MedicalDiagnosis> searchByName(@Param("search") String search, Pageable pageable);

    boolean existsByEnglishNameIgnoreCase(String englishName);
}
