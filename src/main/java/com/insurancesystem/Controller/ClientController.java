package com.insurancesystem.Controller;

import com.insurancesystem.Model.Dto.ClientDto;
import com.insurancesystem.Model.Dto.RejectReasonDTO;
import com.insurancesystem.Model.Dto.UpdateUserDTO;
import com.insurancesystem.Services.ClientServices;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
public class ClientController {

    private final ClientServices clientServices;

    @PreAuthorize("hasRole('INSURANCE_MANAGER')")
    @GetMapping("/list")
    public ResponseEntity<List<ClientDto>> list() {
        return ResponseEntity.ok(clientServices.list());
    }

    @PreAuthorize("hasRole('INSURANCE_MANAGER')")
    @GetMapping("/get/{id}")
    public ResponseEntity<ClientDto> getUserById(@PathVariable UUID id) {
        return ResponseEntity.ok(clientServices.getById(id));
    }

    @PreAuthorize("hasAnyRole('INSURANCE_MANAGER', 'EMERGENCY_MANAGER')")
    @PatchMapping(value = "/update/{id}", consumes = {"multipart/form-data"})
    public ResponseEntity<ClientDto> updateUserById(
            @PathVariable UUID id,
            @RequestPart("data") @Valid UpdateUserDTO dto,
            @RequestPart(value = "universityCard", required = false) MultipartFile universityCard
    ) {
        return ResponseEntity.ok(clientServices.update(id, dto, universityCard));
    }

    @PreAuthorize("hasRole('INSURANCE_MANAGER')")
    @PatchMapping("/{id}/approve")
    public ResponseEntity<ClientDto> approve(@PathVariable UUID id) {
        return ResponseEntity.ok(clientServices.approveRequestedRole(id));
    }

    @PreAuthorize("hasRole('INSURANCE_MANAGER')")
    @GetMapping("/role-requests/pending")
    public ResponseEntity<List<ClientDto>> listPendingRoleRequests() {
        return ResponseEntity.ok(clientServices.listUsersWithPendingRole());
    }

    @PreAuthorize("hasRole('INSURANCE_MANAGER')")
    @PatchMapping("/{id}/role-requests/approve")
    public ResponseEntity<ClientDto> approveRole(@PathVariable UUID id) {
        return ResponseEntity.ok(clientServices.approveRequestedRole(id));
    }

    @PreAuthorize("hasRole('INSURANCE_MANAGER')")
    @PatchMapping("/{id}/reject")
    public ResponseEntity<Void> rejectAndDelete(@PathVariable UUID id, @Valid @RequestBody RejectReasonDTO dto) {
        clientServices.rejectRoleRequest(id, dto.getReason());
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('INSURANCE_CLIENT')")
    @PatchMapping(value = "/me/update", consumes = "multipart/form-data")
    public ResponseEntity<ClientDto> updateMyProfile(
            Authentication auth,
            @RequestPart("data") @Valid UpdateUserDTO dto,
            @RequestPart(value = "universityCard", required = false) MultipartFile universityCard
    ) {
        String username = auth.getName();
        ClientDto updated = clientServices.updateByUsername(username, dto, universityCard);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateClient(
            @PathVariable UUID id,
            @RequestBody(required = false) Map<String, String> body
    ) {
        String reason = (body != null) ? body.getOrDefault("reason", "No reason provided") : "No reason provided";
        clientServices.deactivateClient(id, reason);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('INSURANCE_MANAGER')")
    @PatchMapping("/{id}/reactivate")
    public ResponseEntity<Void> reactivateClient(@PathVariable UUID id) {
        clientServices.reactivateClient(id);
        return ResponseEntity.noContent().build();
    }

    // ============= NEW ENDPOINTS FOR EMPLOYEE ID LOOKUP =============

    /**
     * 🆔 Search client by employee ID
     * Used by doctors to auto-populate patient information in medical forms
     *
     * @param employeeId The employee ID to search for
     * @return Client information (fullName, department, faculty, specialization, etc.)
     */
    @GetMapping("/search/employeeId/{employeeId}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN', 'INSURANCE_MANAGER', 'INSURANCE_CLIENT')")
    public ResponseEntity<?> findByEmployeeId(@PathVariable String employeeId) {
        try {
            if (employeeId == null || employeeId.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Employee ID cannot be empty"));
            }

            ClientDto clientDto = clientServices.findByEmployeeId(employeeId);

            Map<String, Object> response = new HashMap<>();
            response.put("id", clientDto.getId());
            response.put("fullName", clientDto.getFullName());
            response.put("department", clientDto.getDepartment());
            response.put("faculty", clientDto.getFaculty());
            response.put("specialization", clientDto.getSpecialization());
            response.put("employeeId", clientDto.getEmployeeId());
            response.put("email", clientDto.getEmail());
            response.put("phone", clientDto.getPhone());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Employee ID not found: " + employeeId));
        }
    }

    /**
     * 👤 Search client by full name
     * Fallback search method for patient lookup
     *
     * @param fullName The full name to search for
     * @return Client information
     */
    @GetMapping("/search/name/{fullName}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN', 'INSURANCE_MANAGER', 'INSURANCE_CLIENT')")
    public ResponseEntity<?> findByFullName(@PathVariable String fullName) {
        try {
            if (fullName == null || fullName.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Full name cannot be empty"));
            }

            ClientDto clientDto = clientServices.findByFullName(fullName);

            Map<String, Object> response = new HashMap<>();
            response.put("id", clientDto.getId());
            response.put("fullName", clientDto.getFullName());
            response.put("department", clientDto.getDepartment());
            response.put("faculty", clientDto.getFaculty());
            response.put("employeeId", clientDto.getEmployeeId());
            response.put("email", clientDto.getEmail());
            response.put("phone", clientDto.getPhone());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Client not found: " + fullName));
        }
    }
}

