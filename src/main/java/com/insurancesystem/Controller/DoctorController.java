package com.insurancesystem.Controller;

import com.insurancesystem.Model.Dto.ClientDto;
import com.insurancesystem.Model.Dto.UpdateUserDTO;
import com.insurancesystem.Services.DoctorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/medical-records")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService DoctorService;


    @PatchMapping(value = "/me/update", consumes = "multipart/form-data")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<ClientDto> updateDoctorProfile(
            Authentication auth,
            @RequestPart("data") @Valid UpdateUserDTO dto,
            @RequestPart(value = "universityCard", required = false) MultipartFile[] universityCard
    ) {
        String username = auth.getName();
        ClientDto updated = DoctorService.updateProfile(username, dto, universityCard);
        return ResponseEntity.ok(updated);
    }


    @GetMapping("/stats")
    @PreAuthorize("hasRole('DOCTOR')")
    public Map<String, Long> getDoctorStats(Authentication auth) {
        String username = auth.getName();
        return DoctorService.getDoctorStats(username);
    }

}

