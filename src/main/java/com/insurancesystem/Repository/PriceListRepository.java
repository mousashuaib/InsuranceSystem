package com.insurancesystem.Repository;

import com.insurancesystem.Model.Entity.PriceList;
import com.insurancesystem.Model.Entity.Enums.ProviderType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface PriceListRepository extends JpaRepository<PriceList, UUID> {

    @EntityGraph(attributePaths = {"allowedSpecializations"})
    @Query("SELECT p FROM PriceList p WHERE p.providerType = :type")
    List<PriceList> findByProviderType(@Param("type") ProviderType type);


    // فقط العناصر الفعالة
    List<PriceList> findByProviderTypeAndActive(ProviderType providerType, boolean active);

    // ✅ البحث عن خدمة بالاسم والنوع
    @Query("SELECT p FROM PriceList p WHERE p.providerType = :type AND p.serviceName = :serviceName")
    List<PriceList> findByProviderTypeAndServiceName(@Param("type") ProviderType type, @Param("serviceName") String serviceName);

}
