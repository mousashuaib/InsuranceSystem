package com.insurancesystem.Repository;

import com.insurancesystem.Model.Entity.PriceList;
import com.insurancesystem.Model.Entity.Enums.ProviderType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PriceListRepository extends JpaRepository<PriceList, UUID> {

    // قائمة الأسعار حسب نوع المزود فقط
    List<PriceList> findByProviderType(ProviderType providerType);

    // فقط العناصر الفعالة
    List<PriceList> findByProviderTypeAndActive(ProviderType providerType, boolean active);


}
