package com.insurancesystem.Repository;

import com.insurancesystem.Model.Entity.Claim;
import com.insurancesystem.Model.Entity.Client;
import com.insurancesystem.Model.Entity.Enums.ClaimStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ClaimRepository extends JpaRepository<Claim, UUID> {
    List<Claim> findByMember(Client member);
    long countByStatus(ClaimStatus status);

    @Query("SELECT COALESCE(SUM(c.amount),0) FROM Claim c WHERE c.status = 'APPROVED'")
    double sumApprovedAmounts();

    List<Claim> findByStatus(ClaimStatus status);

    // مجموع المبالغ للمطالبات الموافق عليها
    @Query("SELECT SUM(c.amount) FROM Claim c WHERE c.status = 'APPROVED'")
    Double getTotalApprovedAmount();

    // أعلى 5 مزودين
    @Query("SELECT c.providerName, SUM(c.amount) as total " +
            "FROM Claim c WHERE c.status = 'APPROVED' " +
            "GROUP BY c.providerName " +
            "ORDER BY total DESC")
    List<Object[]> getTopProviders();

    @Query("SELECT COALESCE(SUM(c.amount),0) FROM Claim c WHERE c.status = :status")
    double sumAmountByStatus(@Param("status") ClaimStatus status);



    // أعلى مزودين (group by)
    @Query("SELECT c.providerName, SUM(c.amount) " +
            "FROM Claim c " +
            "WHERE c.status = 'APPROVED' " +
            "GROUP BY c.providerName " +
            "ORDER BY SUM(c.amount) DESC")
    List<Object[]> findTopProviders();
}
