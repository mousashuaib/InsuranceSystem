package com.insurancesystem.Repository;

import com.insurancesystem.Model.Entity.SearchProfile;
import com.insurancesystem.Model.Entity.Enums.SearchProfileType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SearchProfileRepository extends JpaRepository<SearchProfile, UUID> {

    // البحث بالاسم (جزئي)
    List<SearchProfile> findByNameContainingIgnoreCase(String name);

    // البحث بالاسم والنوع
    List<SearchProfile> findByNameContainingIgnoreCaseAndType(String name, SearchProfileType type);

    // البحث بالـ type فقط (مثلاً: جميع العيادات)
    List<SearchProfile> findByType(SearchProfileType type);
}
