package com.insurancesystem.Controller;

import com.insurancesystem.Model.Dto.ClientDto;
import com.insurancesystem.Model.Dto.CoordinatorClientLookupDTO;
import com.insurancesystem.Model.Dto.RejectReasonDTO;
import com.insurancesystem.Model.Dto.UpdateUserDTO;
import com.insurancesystem.Model.Entity.Client;
import com.insurancesystem.Model.Entity.Enums.ChronicDisease;
import com.insurancesystem.Model.Entity.Enums.RoleName;
import com.insurancesystem.Repository.ClientRepository;
import com.insurancesystem.Services.ClientServices;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
public class ClientController {

    private final ClientServices clientServices;
    private final ClientRepository clientRepository;

    @PreAuthorize("hasAnyRole('COORDINATION_ADMIN', 'MEDICAL_ADMIN')")
    @GetMapping("/list")
    public ResponseEntity<List<ClientDto>> list() {
        return ResponseEntity.ok(clientServices.list());
    }

    @PreAuthorize("hasAnyRole('INSURANCE_MANAGER','COORDINATION_ADMIN')")
    @GetMapping("/get/{id}")
    public ResponseEntity<ClientDto> getUserById(@PathVariable UUID id) {
        return ResponseEntity.ok(clientServices.getById(id));
    }

    // ✅ تعديل: MultipartFile -> MultipartFile[]
    @PreAuthorize("hasAnyRole('INSURANCE_MANAGER', 'EMERGENCY_MANAGER','MEDICAL_ADMIN', 'RADIOLOGIST' , 'LAB_TECH' , 'PHARMACIST' , 'DOCTOR' , 'COORDINATION_ADMIN')")
    @PatchMapping(value = "/update/{id}", consumes = {"multipart/form-data"})
    public ResponseEntity<ClientDto> updateUserById(
            @PathVariable UUID id,
            @RequestPart("data") @Valid UpdateUserDTO dto,
            @RequestPart(value = "universityCard", required = false) MultipartFile[] universityCard
    ) {
        return ResponseEntity.ok(clientServices.update(id, dto, universityCard));
    }

    @PreAuthorize("hasAnyRole('COORDINATION_ADMIN', 'MEDICAL_ADMIN')")
    @PatchMapping("/{id}/approve")
    public ResponseEntity<ClientDto> approve(@PathVariable UUID id) {
        ClientDto clientDto = clientServices.getById(id);

        if (clientDto.getRequestedRole() == RoleName.INSURANCE_CLIENT) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            // تحقق إذا كان المستخدم الحالي هو MEDICAL_ADMIN
            if (!authentication.getAuthorities().stream()
                    .anyMatch(authority -> authority.getAuthority().equals("ROLE_MEDICAL_ADMIN"))) {
                throw new AccessDeniedException("You do not have permission to approve this request.");
            }
        }

        return ResponseEntity.ok(clientServices.approveRequestedRole(id));
    }


    @PreAuthorize("hasAnyRole('INSURANCE_MANAGER','COORDINATION_ADMIN','MEDICAL_ADMIN')")
    @PatchMapping("/{id}/reject")
    public ResponseEntity<Void> rejectAndDelete(@PathVariable UUID id, @Valid @RequestBody RejectReasonDTO dto) {
        clientServices.rejectRoleRequest(id, dto.getReason());
        return ResponseEntity.noContent().build();
    }


    @PreAuthorize("hasAnyRole('COORDINATION_ADMIN', 'MEDICAL_ADMIN')")
    @GetMapping("/role-requests/pending")
    public ResponseEntity<List<ClientDto>> listPendingRoleRequests() {
        return ResponseEntity.ok(clientServices.listUsersWithPendingRole());
    }

    @PreAuthorize("hasAnyRole('COORDINATION_ADMIN', 'MEDICAL_ADMIN')")
    @PatchMapping("/{id}/role-requests/approve")
    public ResponseEntity<Void> approveRole(@PathVariable UUID id) {
        clientServices.approveClientRoleRequest(id);
        return ResponseEntity.noContent().build();
    }



    // ✅ تعديل: MultipartFile -> MultipartFile[]
    @PreAuthorize("hasRole('INSURANCE_CLIENT')")
    @PatchMapping(value = "/me/update", consumes = "multipart/form-data")
    public ResponseEntity<ClientDto> updateMyProfile(
            Authentication auth,
            @RequestPart("data") @Valid UpdateUserDTO dto,
            @RequestPart(value = "universityCard", required = false) MultipartFile[] universityCard
    ) {
        String email = auth.getName().toLowerCase();
        ClientDto updated = clientServices.updateByEmail(email, dto, universityCard);
        return ResponseEntity.ok(updated);

    }
    @PreAuthorize("hasAnyRole('COORDINATION_ADMIN', 'MEDICAL_ADMIN')")
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateClient(
            @PathVariable UUID id,
            @RequestBody(required = false) Map<String, String> body
    ) {
        String reason = (body != null) ? body.getOrDefault("reason", "No reason provided") : "No reason provided";
        clientServices.deactivateClient(id, reason);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('COORDINATION_ADMIN', 'MEDICAL_ADMIN')")
    @PatchMapping("/{id}/reactivate")
    public ResponseEntity<Void> reactivateClient(@PathVariable UUID id) {
        clientServices.reactivateClient(id);
        return ResponseEntity.noContent().build();
    }

    // ============= NEW ENDPOINTS FOR EMPLOYEE ID LOOKUP =============

    @GetMapping("/search/employeeId/{employeeId}")
<<<<<<< HEAD
    @PreAuthorize("hasAnyRole('DOCTOR', 'PHARMACIST', 'LAB_TECH', 'RADIOLOGIST', 'ADMIN', 'INSURANCE_MANAGER', 'INSURANCE_CLIENT')")
=======



    @PreAuthorize("hasAnyRole('RADIOLOGIST','LAB_TECH','PHARMACIST','DOCTOR', 'ADMIN', 'INSURANCE_MANAGER', 'MEDICAL_ADMIN','COORDINATION_ADMIN')")

>>>>>>> 59fc73de7f549007a5658aab4146b5707a8a4bd8
    public ResponseEntity<?> findByEmployeeId(@PathVariable String employeeId) {
        try {
            if (employeeId == null || employeeId.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Employee ID cannot be empty"));
            }

            Client client = clientRepository.findByEmployeeId(employeeId)
                    .orElseThrow(() -> new RuntimeException("Employee ID not found: " + employeeId));

            boolean hasInsuranceClientRole = client.getRoles().stream()
                    .anyMatch(role -> role.getName() == RoleName.INSURANCE_CLIENT);

            if (!hasInsuranceClientRole) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of(
                                "error", "INVALID_ROLE",
                                "message", "This Employee ID belongs to a user with a different role. Only INSURANCE_CLIENT role is allowed.",
                                "employeeId", employeeId
                        ));
            }

            ClientDto clientDto = clientServices.findByEmployeeId(employeeId);

            // Calculate age from dateOfBirth if available
            String age = "";
            if (clientDto.getDateOfBirth() != null) {
                try {
                    java.time.LocalDate birthDate = clientDto.getDateOfBirth();
                    java.time.LocalDate today = java.time.LocalDate.now();
                    int years = today.getYear() - birthDate.getYear();
                    if (today.getMonthValue() < birthDate.getMonthValue() ||
                        (today.getMonthValue() == birthDate.getMonthValue() && today.getDayOfMonth() < birthDate.getDayOfMonth())) {
                        years--;
                    }
                    age = years > 0 ? years + " years" : "";
                } catch (Exception e) {
                    // If calculation fails, age remains empty
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("id", clientDto.getId());
            response.put("fullName", clientDto.getFullName());
            response.put("department", clientDto.getDepartment());
            response.put("faculty", clientDto.getFaculty());
            response.put("specialization", clientDto.getSpecialization());
            response.put("employeeId", clientDto.getEmployeeId());
            response.put("nationalId", clientDto.getNationalId());
            response.put("email", clientDto.getEmail());
            response.put("phone", clientDto.getPhone());
            response.put("gender", clientDto.getGender());
            response.put("dateofbirth", clientDto.getDateOfBirth());
            response.put("dateOfBirth", clientDto.getDateOfBirth()); // Also add camelCase for consistency
            response.put("age", age); // Add calculated age
            // Add chronic diseases information
            response.put("hasChronicDiseases", clientDto.isHasChronicDiseases());
            if (clientDto.getChronicDiseases() != null && !clientDto.getChronicDiseases().isEmpty()) {
                response.put("chronicDiseases", clientDto.getChronicDiseases().stream()
                        .map(Enum::name)
                        .collect(java.util.stream.Collectors.toList()));
            } else {
                response.put("chronicDiseases", java.util.Collections.emptyList());
            }
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "NOT_FOUND", "message", "Employee ID not found: " + employeeId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "INTERNAL_ERROR", "message", "An error occurred while searching for employee ID"));
        }
    }

    @GetMapping("/search/name/{fullName}")
<<<<<<< HEAD
    @PreAuthorize("hasAnyRole('DOCTOR', 'PHARMACIST', 'LAB_TECH', 'RADIOLOGIST', 'ADMIN', 'INSURANCE_MANAGER', 'INSURANCE_CLIENT')")
=======



    @PreAuthorize("hasAnyRole('RADIOLOGIST','LAB_TECH','PHARMACIST','DOCTOR', 'INSURANCE_MANAGER', 'INSURANCE_CLIENT','MEDICAL_ADMIN','COORDINATION_ADMIN')")

>>>>>>> 59fc73de7f549007a5658aab4146b5707a8a4bd8
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
            response.put("gender",clientDto.getGender());
            response.put("dateofbirth",clientDto.getDateOfBirth());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Client not found: " + fullName));
        }
    }

    @PreAuthorize("hasRole('INSURANCE_CLIENT')")
    @PatchMapping("/me/university-cards/clear")
    public ResponseEntity<Void> clearMyUniversityCards(Authentication auth) {
        String email = auth.getName().toLowerCase();
        clientServices.clearUniversityCardsByEmail(email);
        return ResponseEntity.noContent().build();
    }
    @PreAuthorize("hasAnyRole('COORDINATION_ADMIN', 'MEDICAL_ADMIN')")
    @PostMapping("/coordinator/lookup-for-claim")
    public ResponseEntity<ClientDto> lookupClientForCoordinatorClaim(
            @RequestBody CoordinatorClientLookupDTO dto
    ) {
        return ResponseEntity.ok(
                clientServices.findClientForCoordinatorClaim(dto)
        );
    }




}
