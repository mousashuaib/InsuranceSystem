package com.insurancesystem.Repository;

import com.insurancesystem.Model.Entity.Claim;
import com.insurancesystem.Model.Entity.Client;
import com.insurancesystem.Model.Entity.Enums.ClaimStatus;
import com.insurancesystem.Model.Entity.Policy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ClaimRepository extends JpaRepository<Claim, UUID> {

    List<Claim> findByMember(Client member);

    long countByStatus(ClaimStatus status);

    List<Claim> findByStatus(ClaimStatus status);
    // 🩺 استرجاع المطالبات بانتظار المراجعة الطبية
    @Query("SELECT c FROM Claim c WHERE c.status = 'PENDING'")
    List<Claim> findPendingMedicalClaims();

    @Query("SELECT c FROM Claim c WHERE c.status = 'APPROVED_BY_MEDICAL' OR c.status = 'AWAITING_ADMIN_REVIEW'")
    List<Claim> findPendingAdminClaims();


    // مجموع المبالغ للمطالبات الموافق عليها
    @Query("SELECT COALESCE(SUM(c.amount),0) FROM Claim c WHERE c.status = 'APPROVED'")
    Double getTotalApprovedAmount();

    // 🔹 أعلى المزودين حسب المبلغ (Top Providers)
    @Query("SELECT c.providerName, SUM(c.amount) as total " +
            "FROM Claim c " +
            "WHERE c.status = 'APPROVED' " +
            "GROUP BY c.providerName " +
            "ORDER BY total DESC")
    List<Object[]> findTopProviders();

    // 🔹 أعلى الأطباء حسب عدد المطالبات
    @Query("SELECT c.doctorName, COUNT(c) " +
            "FROM Claim c " +
            "WHERE c.doctorName IS NOT NULL AND c.doctorName <> '' " +
            "GROUP BY c.doctorName " +
            "ORDER BY COUNT(c) DESC")
    List<Object[]> findTopDoctorsByClaims();

    // 🔹 مجموع المبالغ حسب الحالة
    @Query("SELECT COALESCE(SUM(c.amount),0) FROM Claim c WHERE c.status = :status")
    double sumAmountByStatus(@Param("status") ClaimStatus status);

    void deleteAllByPolicy(Policy policy);
}