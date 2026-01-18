package com.insurancesystem.Controller;

import com.insurancesystem.Repository.ClientRepository;
import com.insurancesystem.Repository.HealthcareProviderClaimRepository;
import com.insurancesystem.Repository.PolicyRepository;
import com.insurancesystem.Model.Entity.Enums.ClaimStatus;
import com.insurancesystem.Model.Entity.Enums.RoleName;
import com.insurancesystem.Model.Entity.Enums.RoleRequestStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final ClientRepository clientRepository;
    private final PolicyRepository policyRepository;
    private final HealthcareProviderClaimRepository claimRepository;

    @GetMapping("/manager/stats")
    @PreAuthorize("hasRole('INSURANCE_MANAGER')")
    public ResponseEntity<Map<String, Object>> getManagerStats() {
        Map<String, Object> stats = new HashMap<>();

        // Count only approved insurance clients (not all users in the system)
        long totalClients = clientRepository.findAll().stream()
                .filter(c -> c.getRequestedRole() == RoleName.INSURANCE_CLIENT)
                .filter(c -> c.getRoleRequestStatus() == RoleRequestStatus.APPROVED)
                .count();
        stats.put("totalClients", totalClients);

        stats.put("totalPolicies", policyRepository.count());

        // Count all pending claims (medical review + coordination review + returned)
        long pendingMedical = claimRepository.countByStatus(ClaimStatus.PENDING_MEDICAL);
        long awaitingCoordination = claimRepository.countByStatus(ClaimStatus.AWAITING_COORDINATION_REVIEW);
        long returned = claimRepository.countByStatus(ClaimStatus.RETURNED_FOR_REVIEW);
        stats.put("pendingClaims", pendingMedical + awaitingCoordination + returned);

        return ResponseEntity.ok(stats);
    }
}
