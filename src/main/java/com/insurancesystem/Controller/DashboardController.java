package com.insurancesystem.Controller;

import com.insurancesystem.Repository.ClaimRepository;
import com.insurancesystem.Repository.ClientRepository;
import com.insurancesystem.Repository.PolicyRepository;
import com.insurancesystem.Model.Entity.Enums.ClaimStatus;
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
    private final ClaimRepository claimRepository;

    @GetMapping("/manager/stats")
    @PreAuthorize("hasRole('INSURANCE_MANAGER')")
    public ResponseEntity<Map<String, Object>> getManagerStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalClients", clientRepository.count());
        stats.put("totalPolicies", policyRepository.count());
        stats.put("pendingClaims", claimRepository.countByStatus(ClaimStatus.PENDING));
        return ResponseEntity.ok(stats);
    }
}
