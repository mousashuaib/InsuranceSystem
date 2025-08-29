package com.insurancesystem.Controller;

import com.insurancesystem.Model.Dto.SearchProfileDto;
import com.insurancesystem.Model.Entity.Enums.SearchProfileType;
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

    // إنشاء بروفايل جديد عيادة او صدلية او مختبر
    @PostMapping("Create")
    @PreAuthorize("hasAnyRole('DOCTOR','PHARMACIST','LAB_TECH')")
    public SearchProfileDto create(@RequestBody SearchProfileDto dto) {
        return service.createProfile(dto);
    }

    // جلب بروفايل بالـ ID
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('INSURANCE_CLIENT','DOCTOR','PHARMACIST','LAB_TECH')")
    public SearchProfileDto getById(@PathVariable UUID id) {
        return service.getById(id);
    }

    // البحث بالاسم
    @GetMapping("/by-name")
    @PreAuthorize("hasAnyRole('INSURANCE_CLIENT','DOCTOR','PHARMACIST','LAB_TECH')")
    public List<SearchProfileDto> searchByName(@RequestParam String name) {
        return service.searchByName(name);
    }

    // البحث بالاسم + النوع
    @GetMapping("/by-name-type")
    @PreAuthorize("hasAnyRole('INSURANCE_CLIENT','DOCTOR','PHARMACIST','LAB_TECH')")
    public List<SearchProfileDto> searchByNameAndType(
            @RequestParam String name,
            @RequestParam SearchProfileType type) {
        return service.searchByNameAndType(name, type);
    }

    // البحث بالنوع فقط
    @GetMapping("/by-type")
    @PreAuthorize("hasAnyRole('INSURANCE_CLIENT','DOCTOR','PHARMACIST','LAB_TECH')")
    public List<SearchProfileDto> getAllByType(@RequestParam SearchProfileType type) {
        return service.getAllByType(type);
    }
}
