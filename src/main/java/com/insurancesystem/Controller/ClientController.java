package com.insurancesystem.Controller;

import com.insurancesystem.Model.Dto.ClientDto;
import com.insurancesystem.Model.Dto.RejectReasonDTO;
import com.insurancesystem.Model.Dto.UpdateUserDTO;
import com.insurancesystem.Services.ClientServices;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/Clients")
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
        return ResponseEntity.noContent().build(); // 204
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
}