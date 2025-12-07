package com.insurancesystem.Repository;

import com.insurancesystem.Model.Entity.PrescriptionItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface PrescriptionItemRepository extends JpaRepository<PrescriptionItem, UUID> {

    // 🔍 التحقق: هل المريض طلب نفس الدواء مؤخراً ولسه ما خلص؟
    @Query("""
    SELECT pi 
    FROM PrescriptionItem pi
    WHERE pi.prescription.member.id = :memberId
      AND pi.priceList.id = :medicineId
      AND pi.expiryDate > :now
      AND pi.prescription.status = 'VERIFIED'
""")
    List<PrescriptionItem> findActiveByMemberAndMedicine(
            @Param("memberId") UUID memberId,
            @Param("medicineId") UUID medicineId,
            @Param("now") Instant now
    );


    // جميع الأدوية في وصفة معينة
    List<PrescriptionItem> findByPrescriptionId(UUID prescriptionId);
}