package com.insurancesystem.Repository;

import com.insurancesystem.Model.Entity.Enums.ProfileStatus;
import com.insurancesystem.Model.Entity.FamilyMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FamilyMemberRepository extends JpaRepository<FamilyMember, UUID> {

    // 🔒 منع تكرار رقم الهوية الوطنية
    boolean existsByNationalId(String nationalId);

    // 🔒 منع تكرار رقم التأمين (12443.01)
    boolean existsByInsuranceNumber(String insuranceNumber);

    // 🔢 حساب عدد أفراد العائلة للمؤمن (لاستخراج .01 .02 ...)
    long countByClient_Id(UUID clientId);

    // 📋 جلب جميع أفراد عائلة المؤمن
    List<FamilyMember> findByClient_Id(UUID clientId);

    // 🔍 جلب فرد معين للمؤمن (حماية إضافية)
    Optional<FamilyMember> findByIdAndClient_Id(UUID id, UUID clientId);

    // 🔍 البحث عن فرد عائلة بالاسم والعلاقة
    Optional<FamilyMember> findByClient_IdAndFullNameAndRelation(UUID clientId, String fullName, com.insurancesystem.Model.Entity.Enums.FamilyRelation relation);

    // ❌ حذف فرد عائلة للمؤمن (مع التحقق من الملكية)
    void deleteByIdAndClient_Id(UUID id, UUID clientId);

    
    // 🔍 البحث عن فرد عائلة بالاسم الكامل
    Optional<FamilyMember> findByFullName(String fullName);


    List<FamilyMember> findByStatus(ProfileStatus status);

}
