package com.insurancesystem.Controller;

import com.insurancesystem.Exception.NotFoundException;
import com.insurancesystem.Model.Dto.CreateEmergencyRequestDTO;
import com.insurancesystem.Model.Dto.EmergencyRequestDTO;
import com.insurancesystem.Model.Dto.RejectEmergencyDTO;
import com.insurancesystem.Model.Entity.Client;
import com.insurancesystem.Repository.ClientRepository;
import com.insurancesystem.Repository.EmergencyRequestRepository;
import com.insurancesystem.Services.EmergencyRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/emergencies")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class EmergencyRequestController {

    private final EmergencyRequestService emergencyService;
    private final ClientRepository clientRepo;
    private final EmergencyRequestRepository emergencyRepo;

    // ✅ Doctor creates emergency request for a client
    @PostMapping("/create")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<?> createEmergencyRequest(Authentication auth, @Valid @RequestBody CreateEmergencyRequestDTO dto) {
        try {

            String username = auth.getName();



            String email = auth.getName().toLowerCase();
            Client doctor = clientRepo.findByEmail(email)

                    .orElseThrow(() -> new NotFoundException("DOCTOR_NOT_FOUND"));


            return ResponseEntity.ok(emergencyService.createEmergencyRequest(doctor.getId(), dto));
        } catch (NotFoundException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(404).body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage() != null ? e.getMessage() : "Failed to create emergency request");
            return ResponseEntity.status(400).body(errorResponse);
        }
    }

    // ✅ Client gets their emergency requests
    @GetMapping
    @PreAuthorize("hasRole('INSURANCE_CLIENT')")
    public ResponseEntity<?> getMemberEmergencyRequests(Authentication auth) {
        try {

            String username = auth.getName();



            String email = auth.getName().toLowerCase();
            Client client = clientRepo.findByEmail(email)

                    .orElseThrow(() -> new NotFoundException("CLIENT_NOT_FOUND"));


            return ResponseEntity.ok(emergencyService.getMemberEmergencyRequests(client.getId()));
        } catch (NotFoundException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(404).body(errorResponse);
        }
    }

    // ✅ Doctor gets their created emergency requests
    @GetMapping("/doctor/my-requests")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<?> getDoctorEmergencyRequests(Authentication auth) {
        try {

            String username = auth.getName();



            String email = auth.getName().toLowerCase();
            Client doctor = clientRepo.findByEmail(email)

                    .orElseThrow(() -> new NotFoundException("DOCTOR_NOT_FOUND"));


            return ResponseEntity.ok(emergencyService.getDoctorEmergencyRequests(doctor.getId()));
        } catch (NotFoundException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(404).body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    // ✅ Doctor gets a specific emergency request by ID
    @GetMapping("/doctor/my-requests/{id}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<?> getDoctorEmergencyRequest(
            Authentication auth,
            @PathVariable UUID id
    ) {
        try {

            String username = auth.getName();



            String email = auth.getName().toLowerCase();
            Client doctor = clientRepo.findByEmail(email)

                    .orElseThrow(() -> new NotFoundException("DOCTOR_NOT_FOUND"));


            return ResponseEntity.ok(emergencyService.getDoctorEmergencyRequest(doctor.getId(), id));
        } catch (NotFoundException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(404).body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    // ✅ Emergency Manager/Medical Admin gets all requests
    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('EMERGENCY_MANAGER', 'MEDICAL_ADMIN')")
    public ResponseEntity<?> getAllEmergencyRequests() {
        try {
            return ResponseEntity.ok(emergencyService.getAllEmergencyRequests());
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    // ✅ Emergency Manager/Medical Admin approves emergency request
    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('EMERGENCY_MANAGER', 'MEDICAL_ADMIN')")
    public ResponseEntity<?> approveEmergencyRequest(@PathVariable UUID id) {
        try {
            return ResponseEntity.ok(emergencyService.approveEmergencyRequest(id));
        } catch (NotFoundException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(404).body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(400).body(errorResponse);
        }
    }

    // ✅ Emergency Manager/Medical Admin rejects emergency request
    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('EMERGENCY_MANAGER', 'MEDICAL_ADMIN')")
    public ResponseEntity<?> rejectEmergencyRequest(
            @PathVariable UUID id,
            @Valid @RequestBody RejectEmergencyDTO dto
    ) {
        try {
            return ResponseEntity.ok(emergencyService.rejectEmergencyRequest(id, dto));
        } catch (NotFoundException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(404).body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(400).body(errorResponse);
        }
    }

}

