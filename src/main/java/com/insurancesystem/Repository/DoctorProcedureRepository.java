package com.insurancesystem.Repository;

import com.insurancesystem.Model.Entity.DoctorProcedure;
import com.insurancesystem.Model.Entity.Enums.CoverageStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DoctorProcedureRepository extends JpaRepository<DoctorProcedure, UUID> {

    List<DoctorProcedure> findByActiveTrue();

    Page<DoctorProcedure> findByActiveTrue(Pageable pageable);

    List<DoctorProcedure> findByCategoryAndActiveTrue(String category);

    Page<DoctorProcedure> findByCategoryAndActiveTrue(String category, Pageable pageable);

    List<DoctorProcedure> findByCoverageStatus(CoverageStatus coverageStatus);

    Page<DoctorProcedure> findByCoverageStatusAndActiveTrue(CoverageStatus coverageStatus, Pageable pageable);

    Optional<DoctorProcedure> findByProcedureNameIgnoreCaseAndCategory(String procedureName, String category);

    @Query("SELECT d FROM DoctorProcedure d WHERE d.active = true AND " +
           "LOWER(d.procedureName) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<DoctorProcedure> searchByName(@Param("search") String search, Pageable pageable);

    @Query("SELECT d FROM DoctorProcedure d WHERE d.active = true AND d.category = :category AND " +
           "LOWER(d.procedureName) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<DoctorProcedure> searchByNameAndCategory(@Param("search") String search, @Param("category") String category, Pageable pageable);

    boolean existsByProcedureNameIgnoreCaseAndCategory(String procedureName, String category);
}
