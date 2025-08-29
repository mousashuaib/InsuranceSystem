package com.insurancesystem.Services;

import com.insurancesystem.Exception.NotFoundException;
import com.insurancesystem.Model.Dto.SearchProfileDto;
import com.insurancesystem.Model.Entity.Client;
import com.insurancesystem.Model.Entity.Enums.SearchProfileType;
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

@Service
@RequiredArgsConstructor
public class SearchProfileService {

    private final SearchProfileRepository repository;
    private final SearchProfileMapper searchProfileMapper;
    private final ClientRepository clientRepository; // جدول المستخدمين
    private final CustomUserDetailsService userDetailsService;

    public SearchProfileDto createProfile(SearchProfileDto dto) {
        SearchProfile entity = searchProfileMapper.toEntity(dto);

        // جلب المستخدم الحالي من الـ SecurityContext
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Client currentUser = clientRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        entity.setOwner(currentUser); // تعيين المالك

        return searchProfileMapper.toDto(repository.save(entity));
    }


    // البحث بالاسم
    public List<SearchProfileDto> searchByName(String name) {
        return repository.findByNameContainingIgnoreCase(name)
                .stream()
                .map(searchProfileMapper::toDto)
                .toList();
    }

    // البحث بالاسم + النوع
    public List<SearchProfileDto> searchByNameAndType(String name, SearchProfileType type) {
        return repository.findByNameContainingIgnoreCaseAndType(name, type)
                .stream()
                .map(searchProfileMapper::toDto)
                .toList();
    }

    // البحث بالنوع فقط
    public List<SearchProfileDto> getAllByType(SearchProfileType type) {
        return repository.findByType(type)
                .stream()
                .map(searchProfileMapper::toDto)
                .toList();
    }

    // البحث بالـ ID
    public SearchProfileDto getById(UUID id) {
        SearchProfile entity = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Profile not found"));
        return searchProfileMapper.toDto(entity);
    }
}
