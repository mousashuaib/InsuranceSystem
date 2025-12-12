package com.insurancesystem.Controller;

import com.insurancesystem.Model.Dto.SearchProfileDto;
import com.insurancesystem.Model.Entity.Enums.ProfileStatus;
import com.insurancesystem.Model.Entity.Enums.SearchProfileType;
import com.insurancesystem.Services.SearchProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/search-profiles")
@RequiredArgsConstructor
public class SearchProfileController {

    private final SearchProfileService service;

    // ✅ إنشاء بروفايل جديد مع رفع الملفات
    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('INSURANCE_MANAGER','DOCTOR','PHARMACIST','LAB_TECH','RADIOLOGIST','EMERGENCY_MANAGER')")
    public SearchProfileDto create(
            @RequestPart("data") SearchProfileDto dto,
            @RequestPart("medicalLicense") MultipartFile medicalLicense,
            @RequestPart("universityDegree") MultipartFile universityDegree,
            @RequestPart(value = "clinicRegistration", required = false) MultipartFile clinicRegistration,
            @RequestPart("idOrPassportCopy") MultipartFile idOrPassportCopy
    ) {

        // ✅ Save uploaded files
        String medicalLicensePath = saveFile(medicalLicense);
        String universityDegreePath = saveFile(universityDegree);
        String clinicRegistrationPath = clinicRegistration != null ? saveFile(clinicRegistration) : null;
        String idOrPassportCopyPath = saveFile(idOrPassportCopy);

        // ✅ Set file paths into DTO
        dto.setMedicalLicense(medicalLicensePath);
        dto.setUniversityDegree(universityDegreePath);
        dto.setClinicRegistration(clinicRegistrationPath);
        dto.setIdOrPassportCopy(idOrPassportCopyPath);

        return service.createProfile(dto);
    }

    // ✅ Helper method to save files locally
    private String saveFile(MultipartFile file) {
        try {
            String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path uploadPath = Paths.get("uploads/search-profiles");
            Files.createDirectories(uploadPath);

            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            return filename; // ✅ Return ONLY filename
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file: " + file.getOriginalFilename(), e);
        }
    }

    // ✅ Endpoint لتحميل الملفات - FIXED to handle + in filenames properly
    @GetMapping("/file/{filename:.+}")
    @PreAuthorize("hasAnyRole('INSURANCE_MANAGER','INSURANCE_CLIENT','DOCTOR','PHARMACIST','LAB_TECH','RADIOLOGIST','EMERGENCY_MANAGER')")
    public ResponseEntity<Resource> getFile(@PathVariable String filename) throws MalformedURLException {
        try {
            // 🔧 CRITICAL FIX: Use java.net.URI to properly decode the filename
            // URLDecoder.decode() converts + to space, which breaks filenames with + in them
            // Instead, we use the raw filename from the path variable (Spring decodes it)

            System.out.println("=== FILE REQUEST DEBUG ===");
            System.out.println("Raw filename from path: " + filename);

            // ✅ Try to use URLDecoder safely with proper handling of +
            String decodedFilename = filename;
            try {
                // Only decode if it looks like it's URL encoded (contains %)
                if (filename.contains("%")) {
                    // Use a safer approach - replace %2B with + before general decoding
                    String safeForDecoding = filename.replace("%2B", "\u0000PLUS\u0000");
                    decodedFilename = java.net.URLDecoder.decode(safeForDecoding, StandardCharsets.UTF_8);
                    decodedFilename = decodedFilename.replace("\u0000PLUS\u0000", "+");
                    System.out.println("Decoded filename: " + decodedFilename);
                }
            } catch (Exception e) {
                System.out.println("Could not decode, using as-is: " + e.getMessage());
                decodedFilename = filename;
            }

            Path filePath = Paths.get("uploads/search-profiles").resolve(decodedFilename).normalize();

            System.out.println("Looking for file at: " + filePath.toAbsolutePath());

            if (!Files.exists(filePath)) {
                System.out.println("❌ File NOT found");

                // Debug: List available files
                try {
                    System.out.println("Available files in directory:");
                    Files.list(Paths.get("uploads/search-profiles"))
                            .limit(10)
                            .forEach(p -> System.out.println("  - " + p.getFileName()));
                } catch (Exception ex) {
                    System.out.println("Could not list directory: " + ex.getMessage());
                }

                return ResponseEntity.notFound().build();
            }

            System.out.println("✅ File found!");

            // ✅ Security check - prevent directory traversal
            if (!filePath.toAbsolutePath().startsWith(Paths.get("uploads/search-profiles").toAbsolutePath())) {
                System.out.println("❌ Security check failed - path traversal attempt");
                return ResponseEntity.status(403).build();
            }

            Resource resource = new UrlResource(filePath.toUri());
            String contentType = Files.probeContentType(filePath);

            System.out.println("Content-Type: " + contentType);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType != null ? contentType : "application/octet-stream"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + decodedFilename + "\"")
                    .body(resource);

        } catch (Exception e) {
            System.out.println("❌ Error retrieving file: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    // ✅ جلب بروفايل بالـ ID
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('INSURANCE_MANAGER','INSURANCE_CLIENT','DOCTOR','PHARMACIST','LAB_TECH','RADIOLOGIST','EMERGENCY_MANAGER')")
    public SearchProfileDto getById(@PathVariable UUID id) {
        return service.getById(id);
    }

    // ✅ البحث بالاسم
    @GetMapping("/by-name")
    @PreAuthorize("hasAnyRole('INSURANCE_MANAGER','INSURANCE_CLIENT','DOCTOR','PHARMACIST','LAB_TECH','RADIOLOGIST','EMERGENCY_MANAGER')")
    public List<SearchProfileDto> searchByName(@RequestParam String name) {
        return service.searchByName(name);
    }

    // ✅ البحث بالاسم + النوع
    @GetMapping("/by-name-type")
    @PreAuthorize("hasAnyRole('INSURANCE_MANAGER','INSURANCE_CLIENT','DOCTOR','PHARMACIST','LAB_TECH','RADIOLOGIST','EMERGENCY_MANAGER')")
    public List<SearchProfileDto> searchByNameAndType(
            @RequestParam String name,
            @RequestParam SearchProfileType type) {
        return service.searchByNameAndType(name, type);
    }

    // ✅ البحث بالنوع فقط
    @GetMapping("/by-type")
    @PreAuthorize("hasAnyRole('INSURANCE_MANAGER','INSURANCE_CLIENT','DOCTOR','PHARMACIST','LAB_TECH','RADIOLOGIST','EMERGENCY_MANAGER')")
    public List<SearchProfileDto> getAllByType(@RequestParam SearchProfileType type) {
        return service.getAllByType(type);
    }

    // ✅ Endpoint للمدير: جلب كل البروفايلات
    @GetMapping("/all")
    @PreAuthorize("hasRole('INSURANCE_MANAGER')")
    public List<SearchProfileDto> getAllProfiles() {
        return service.getAllProfiles();
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

    // ✅ جلب البروفايلات الموافق عليها فقط
    @GetMapping("/approved")
    @PreAuthorize("hasAnyRole('INSURANCE_CLIENT','DOCTOR','PHARMACIST','LAB_TECH','RADIOLOGIST','EMERGENCY_MANAGER','INSURANCE_MANAGER')")
    public List<SearchProfileDto> getApprovedProfiles() {
        return service.getApprovedProfiles();
    }

    // ✅ تعديل بروفايل
    @PutMapping(value = "/{id}/edit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('DOCTOR','PHARMACIST','LAB_TECH','RADIOLOGIST')")
    public SearchProfileDto updateWithFiles(
            @PathVariable UUID id,
            @RequestPart("data") SearchProfileDto dto,
            @RequestPart(value = "medicalLicense", required = false) MultipartFile medicalLicense,
            @RequestPart(value = "universityDegree", required = false) MultipartFile universityDegree,
            @RequestPart(value = "clinicRegistration", required = false) MultipartFile clinicRegistration,
            @RequestPart(value = "idOrPassportCopy", required = false) MultipartFile idOrPassportCopy
    ) {
        // ✅ Update only if new file is uploaded
        if (medicalLicense != null) {
            dto.setMedicalLicense(saveFile(medicalLicense));
        }
        if (universityDegree != null) {
            dto.setUniversityDegree(saveFile(universityDegree));
        }
        if (clinicRegistration != null) {
            dto.setClinicRegistration(saveFile(clinicRegistration));
        }
        if (idOrPassportCopy != null) {
            dto.setIdOrPassportCopy(saveFile(idOrPassportCopy));
        }

        return service.updateProfileById(id, dto);
    }

    // ✅ حذف بروفايل
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('DOCTOR','PHARMACIST','LAB_TECH','RADIOLOGIST')")
    public void deleteById(@PathVariable UUID id) {
        service.deleteProfileById(id);
    }

    // ✅ جلب بروفايلات المستخدم الحالي
    @GetMapping("/my-profiles")
    @PreAuthorize("hasAnyRole('DOCTOR','PHARMACIST','LAB_TECH','RADIOLOGIST')")
    public List<SearchProfileDto> getMyProfiles() {
        return service.getMyProfiles();
    }
}
