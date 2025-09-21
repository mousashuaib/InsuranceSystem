package com.insurancesystem.Services;

import com.insurancesystem.Exception.NotFoundException;
import com.insurancesystem.Model.Dto.SearchProfileDto;
import com.insurancesystem.Model.Entity.Client;
import com.insurancesystem.Model.Entity.Enums.SearchProfileType;
import com.insurancesystem.Model.Entity.Enums.ProfileStatus;
import com.insurancesystem.Model.Entity.SearchProfile;
import com.insurancesystem.Model.MapStruct.SearchProfileMapper;
import com.insurancesystem.Repository.ClientRepository;
import com.insurancesystem.Repository.SearchProfileRepository;
import com.insurancesystem.Security.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchProfileService {

    private final SearchProfileRepository repository;
    private final SearchProfileMapper searchProfileMapper;
    private final ClientRepository clientRepository;
    private final CustomUserDetailsService userDetailsService;

    // إنشاء بروفايل جديد
    public SearchProfileDto createProfile(SearchProfileDto dto) {
        SearchProfile entity = searchProfileMapper.toEntity(dto);

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Client currentUser = clientRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        entity.setOwner(currentUser);
        entity.setStatus(ProfileStatus.PENDING);
        entity.setRejectionReason(null); // عند الإنشاء لا يوجد سبب رفض

        return searchProfileMapper.toDto(repository.save(entity));
    }

    // البحث بالاسم (الموافق عليهم فقط)
    public List<SearchProfileDto> searchByName(String name) {
        return repository.findByNameContainingIgnoreCaseAndStatus(name, ProfileStatus.APPROVED)
                .stream().map(searchProfileMapper::toDto).toList();
    }

    // البحث بالاسم + النوع (موافق عليهم فقط)
    public List<SearchProfileDto> searchByNameAndType(String name, SearchProfileType type) {
        return repository.findByNameContainingIgnoreCaseAndTypeAndStatus(name, type, ProfileStatus.APPROVED)
                .stream().map(searchProfileMapper::toDto).toList();
    }

    // البحث بالنوع فقط (موافق عليهم فقط)
    public List<SearchProfileDto> getAllByType(SearchProfileType type) {
        return repository.findByTypeAndStatus(type, ProfileStatus.APPROVED)
                .stream().map(searchProfileMapper::toDto).toList();
    }

    // البحث بالـ ID
    public SearchProfileDto getById(UUID id) {
        SearchProfile entity = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Profile not found"));
        return searchProfileMapper.toDto(entity);
    }



    // تحديث الحالة
    public SearchProfileDto updateStatus(UUID id, ProfileStatus status, String rejectionReason) {
        SearchProfile profile = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Profile not found"));

        profile.setStatus(status);

        if (status == ProfileStatus.REJECTED) {
            profile.setRejectionReason(rejectionReason);
        } else {
            profile.setRejectionReason(null);
        }

        return searchProfileMapper.toDto(repository.save(profile));
    }
    // ✅ جلب كل البروفايلات
    public List<SearchProfileDto> getAllProfiles() {
        return repository.findAll()
                .stream().map(searchProfileMapper::toDto).toList();
    }


    // ✅ كل البروفايلات الموافق عليها
    public List<SearchProfileDto> getApprovedProfiles() {
        return repository.findByStatus(ProfileStatus.APPROVED)
                .stream()
                .map(searchProfileMapper::toDto)
                .collect(Collectors.toList());
    }



}
