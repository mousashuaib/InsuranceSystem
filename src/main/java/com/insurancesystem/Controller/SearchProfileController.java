package com.insurancesystem.Controller;

import com.insurancesystem.Model.Dto.SearchProfileDto;
import com.insurancesystem.Model.Entity.Enums.SearchProfileType;
import com.insurancesystem.Model.Entity.Enums.ProfileStatus;
import com.insurancesystem.Services.SearchProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/search-profiles")
@RequiredArgsConstructor
public class SearchProfileController {

    private final SearchProfileService service;

    // إنشاء بروفايل جديد
    @PostMapping("create")
    @PreAuthorize("hasAnyRole('INSURANCE_MANAGER','DOCTOR','PHARMACIST','LAB_TECH','EMERGENCY_MANAGER')")
    public SearchProfileDto create(@RequestBody SearchProfileDto dto) {
        return service.createProfile(dto);
    }

    // جلب بروفايل بالـ ID
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('INSURANCE_MANAGER','INSURANCE_CLIENT','DOCTOR','PHARMACIST','LAB_TECH','EMERGENCY_MANAGER')")
    public SearchProfileDto getById(@PathVariable UUID id) {
        return service.getById(id);
    }

    // البحث بالاسم
    @GetMapping("/by-name")
    @PreAuthorize("hasAnyRole('INSURANCE_MANAGER','INSURANCE_CLIENT','DOCTOR','PHARMACIST','LAB_TECH','EMERGENCY_MANAGER')")
    public List<SearchProfileDto> searchByName(@RequestParam String name) {
        return service.searchByName(name);
    }

    // البحث بالاسم + النوع
    @GetMapping("/by-name-type")
    @PreAuthorize("hasAnyRole('INSURANCE_MANAGER','INSURANCE_CLIENT','DOCTOR','PHARMACIST','LAB_TECH','EMERGENCY_MANAGER')")
    public List<SearchProfileDto> searchByNameAndType(
            @RequestParam String name,
            @RequestParam SearchProfileType type) {
        return service.searchByNameAndType(name, type);
    }

    // البحث بالنوع فقط
    @GetMapping("/by-type")
    @PreAuthorize("hasAnyRole('INSURANCE_MANAGER','INSURANCE_CLIENT','DOCTOR','PHARMACIST','LAB_TECH','EMERGENCY_MANAGER')")
    public List<SearchProfileDto> getAllByType(@RequestParam SearchProfileType type) {
        return service.getAllByType(type);
    }

    // ✅ Endpoint للمدير: جلب كل البروفايلات بأي حالة
    @GetMapping("/all")
    @PreAuthorize("hasRole('INSURANCE_MANAGER')")
    public List<SearchProfileDto> getAllProfiles() {
        return service.getAllProfiles(); // يرجع Pending + Approved + Rejected
    }


    // ✅ موافقة
    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('INSURANCE_MANAGER')")
    public SearchProfileDto approve(@PathVariable UUID id) {
        return service.updateStatus(id, ProfileStatus.APPROVED, null);
    }

    // ✅ رفض مع السبب
    @PutMapping("/{id}/reject")
    @PreAuthorize("hasRole('INSURANCE_MANAGER')")
    public SearchProfileDto reject(@PathVariable UUID id, @RequestBody String reason) {
        return service.updateStatus(id, ProfileStatus.REJECTED, reason);
    }

    @GetMapping("/approved")
    @PreAuthorize("hasAnyRole('INSURANCE_CLIENT','DOCTOR','PHARMACIST','LAB_TECH','EMERGENCY_MANAGER','INSURANCE_MANAGER')")
    public List<SearchProfileDto> getApprovedProfiles() {
        return service.getApprovedProfiles();
    }

}
