package com.insurancesystem.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.insurancesystem.Exception.NotFoundException;
import com.insurancesystem.Model.Dto.CreateEmergencyRequestDTO;
import com.insurancesystem.Model.Dto.EmergencyRequestDTO;
import com.insurancesystem.Model.Dto.RejectEmergencyDTO;
import com.insurancesystem.Model.Entity.Client;
import com.insurancesystem.Repository.ClientRepository;
import com.insurancesystem.Services.EmergencyRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/emergencies")
@RequiredArgsConstructor
public class EmergencyRequestController {

    private final EmergencyRequestService emergencyService;
    private final ClientRepository clientRepo;
    private final ObjectMapper objectMapper;

    @PreAuthorize("hasRole('INSURANCE_CLIENT')")
    @PostMapping
    public ResponseEntity<EmergencyRequestDTO> createEmergencyRequest(Authentication auth, @Valid @RequestBody CreateEmergencyRequestDTO dto
    ) {
        String username = auth.getName();
        Client client = clientRepo.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Client not found"));

        return ResponseEntity.ok(emergencyService.createEmergencyRequest(client.getId(), dto));
    }

    @PreAuthorize("hasRole('INSURANCE_CLIENT')")
    @GetMapping
    public ResponseEntity<List<EmergencyRequestDTO>> getMemberEmergencyRequests(Authentication auth) {
        String username = auth.getName();
        Client client = clientRepo.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Client not found"));

        return ResponseEntity.ok(emergencyService.getMemberEmergencyRequests(client.getId()));
    }

    @PreAuthorize("hasAnyRole('INSURANCE_MANAGER', 'EMERGENCY_MANAGER')")
    @GetMapping("/all")
    public ResponseEntity<List<EmergencyRequestDTO>> getAllEmergencyRequests() {
        return ResponseEntity.ok(emergencyService.getAllPendingRequests());
    }

    @PreAuthorize("hasAnyRole('INSURANCE_MANAGER', 'EMERGENCY_MANAGER')")
    @PatchMapping("/{id}/approve")
    public ResponseEntity<EmergencyRequestDTO> approveEmergencyRequest(@PathVariable UUID id) {
        return ResponseEntity.ok(emergencyService.approveEmergencyRequest(id));
    }

    @PreAuthorize("hasAnyRole('INSURANCE_MANAGER', 'EMERGENCY_MANAGER')")
    @PatchMapping("/{id}/reject")
    public ResponseEntity<EmergencyRequestDTO> rejectEmergencyRequest(
            @PathVariable UUID id,
            @Valid @RequestBody RejectEmergencyDTO dto
    ) {
        return ResponseEntity.ok(emergencyService.rejectEmergencyRequest(id, dto));
    }
}
