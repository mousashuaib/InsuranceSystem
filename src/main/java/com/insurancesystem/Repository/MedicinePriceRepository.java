package com.insurancesystem.Repository;

import com.insurancesystem.Model.Entity.MedicinePrice;
import com.insurancesystem.Model.Entity.Enums.CoverageStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MedicinePriceRepository extends JpaRepository<MedicinePrice, UUID> {

    List<MedicinePrice> findByActiveTrue();

    Page<MedicinePrice> findByActiveTrue(Pageable pageable);

    List<MedicinePrice> findByCoverageStatus(CoverageStatus coverageStatus);

    Page<MedicinePrice> findByCoverageStatusAndActiveTrue(CoverageStatus coverageStatus, Pageable pageable);

    Optional<MedicinePrice> findByDrugNameIgnoreCase(String drugName);

    @Query("SELECT m FROM MedicinePrice m WHERE m.active = true AND " +
           "(LOWER(m.drugName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(m.genericName) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<MedicinePrice> searchByName(@Param("search") String search, Pageable pageable);

    boolean existsByDrugNameIgnoreCase(String drugName);
}
