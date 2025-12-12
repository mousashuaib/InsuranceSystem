package com.insurancesystem.Services;

import com.insurancesystem.Exception.NotFoundException;
import com.insurancesystem.Model.Dto.SearchProfileDto;
import com.insurancesystem.Model.Entity.Client;
import com.insurancesystem.Model.Entity.Enums.RoleName;
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
    private final NotificationService notificationService;
    public SearchProfileDto createProfile(SearchProfileDto dto) {
        SearchProfile entity = searchProfileMapper.toEntity(dto);

        // ✅ Copy document fields (MapStruct already maps them, but this ensures clarity)
        entity.setMedicalLicense(dto.getMedicalLicense());
        entity.setUniversityDegree(dto.getUniversityDegree());
        entity.setClinicRegistration(dto.getClinicRegistration());
        entity.setIdOrPassportCopy(dto.getIdOrPassportCopy());

        // 🧑‍💻 المستخدم الحالي (صاحب الطلب)
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Client currentUser = clientRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        entity.setOwner(currentUser);
        entity.setStatus(ProfileStatus.PENDING);
        entity.setRejectionReason(null);

        SearchProfile savedProfile = repository.save(entity);

        notificationService.sendToUser(
                currentUser.getId(),
                "تم استلام طلبك لإنشاء بروفايل جديد وهو الآن قيد المراجعة"
        );

        notificationService.sendToRole(
                RoleName.INSURANCE_MANAGER,
                "يوجد طلب جديد لإنشاء بروفايل من المستخدم: " + currentUser.getUsername()
        );

        return searchProfileMapper.toDto(savedProfile);
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



    public SearchProfileDto updateStatus(UUID id, ProfileStatus status, String rejectionReason) {
        SearchProfile profile = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Profile not found"));

        profile.setStatus(status);

        if (status == ProfileStatus.REJECTED) {
            profile.setRejectionReason(rejectionReason);

            // 🔔 إشعار لصاحب البروفايل أنه تم الرفض
            notificationService.sendToUser(
                    profile.getOwner().getId(),
                    "❌ تم رفض طلب إنشاء البروفايل الخاص بك. السبب: " + rejectionReason
            );

        } else if (status == ProfileStatus.APPROVED) {
            profile.setRejectionReason(null);

            // 🔔 إشعار لصاحب البروفايل أنه تم القبول
            notificationService.sendToUser(
                    profile.getOwner().getId(),
                    "✅ تم قبول طلب إنشاء البروفايل الخاص بك!"
            );
        } else {
            // حالة PENDING
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



    public List<SearchProfileDto> getMyProfiles() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Client currentUser = clientRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found"));

        return repository.findAllByOwnerId(currentUser.getId())
                .stream()
                .map(searchProfileMapper::toDto)
                .toList();
    }

    public SearchProfileDto updateProfileById(UUID id, SearchProfileDto dto) {
        SearchProfile profile = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Profile not found"));

        // ✅ Update basic fields
        profile.setName(dto.getName());
        profile.setAddress(dto.getAddress());
        profile.setContactInfo(dto.getContactInfo());
        profile.setDescription(dto.getDescription());
        profile.setLocationLat(dto.getLocationLat());
        profile.setLocationLng(dto.getLocationLng());

        // ✅ Update document fields
        profile.setMedicalLicense(dto.getMedicalLicense());
        profile.setUniversityDegree(dto.getUniversityDegree());
        profile.setClinicRegistration(dto.getClinicRegistration());
        profile.setIdOrPassportCopy(dto.getIdOrPassportCopy());

        // ✅ Any update resets status to PENDING
        profile.setStatus(ProfileStatus.PENDING);
        profile.setRejectionReason(null);

        SearchProfile updatedProfile = repository.save(profile);

        notificationService.sendToUser(
                profile.getOwner().getId(),
                "✏️ تم تعديل البروفايل الخاص بك، وهو الآن قيد المراجعة من الإدارة."
        );

        notificationService.sendToRole(
                RoleName.INSURANCE_MANAGER,
                "📢 يوجد تعديل جديد على بروفايل من المستخدم: "
                        + profile.getOwner().getUsername()
                        + " (اسم البروفايل: " + profile.getName() + ")"
        );

        return searchProfileMapper.toDto(updatedProfile);
    }


    public void deleteProfileById(UUID id) {
        SearchProfile profile = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Profile not found"));
        repository.delete(profile);
    }


}