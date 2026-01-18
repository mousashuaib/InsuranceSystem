package com.insurancesystem.Repository;

import com.insurancesystem.Model.Entity.MedicalTest;
import com.insurancesystem.Model.Entity.Enums.CoverageStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MedicalTestRepository extends JpaRepository<MedicalTest, UUID> {

    List<MedicalTest> findByActiveTrue();

    Page<MedicalTest> findByActiveTrue(Pageable pageable);

    List<MedicalTest> findByCategoryAndActiveTrue(String category);

    Page<MedicalTest> findByCategoryAndActiveTrue(String category, Pageable pageable);

    List<MedicalTest> findByCoverageStatus(CoverageStatus coverageStatus);

    Page<MedicalTest> findByCoverageStatusAndActiveTrue(CoverageStatus coverageStatus, Pageable pageable);

    Optional<MedicalTest> findByTestNameIgnoreCaseAndCategory(String testName, String category);

    @Query("SELECT m FROM MedicalTest m WHERE m.active = true AND " +
           "LOWER(m.testName) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<MedicalTest> searchByName(@Param("search") String search, Pageable pageable);

    @Query("SELECT m FROM MedicalTest m WHERE m.active = true AND m.category = :category AND " +
           "LOWER(m.testName) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<MedicalTest> searchByNameAndCategory(@Param("search") String search, @Param("category") String category, Pageable pageable);

    boolean existsByTestNameIgnoreCaseAndCategory(String testName, String category);
}
