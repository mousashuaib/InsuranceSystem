package com.insurancesystem.Controller;

import com.insurancesystem.Model.Dto.CreatePolicyDTO;
import com.insurancesystem.Model.Dto.PolicyDTO;
import com.insurancesystem.Model.Dto.UpdatePolicyDTO;
import com.insurancesystem.Model.Dto.CoverageDTO;
import com.insurancesystem.Model.Dto.CreateCoverageDTO;
import com.insurancesystem.Services.CoverageService;
import com.insurancesystem.Services.PolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/policies")
@RequiredArgsConstructor
public class PolicyController {

    private final PolicyService policyService;
    private final CoverageService coverageService;

    @PreAuthorize("hasRole('INSURANCE_MANAGER')")
    @PostMapping("/create")
    public ResponseEntity<PolicyDTO> create(@Valid @RequestBody CreatePolicyDTO dto) {
        PolicyDTO created = policyService.create(dto);

        // 🔔 إشعار

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PreAuthorize("hasRole('INSURANCE_MANAGER')")
    @GetMapping("/all")
    public ResponseEntity<List<PolicyDTO>> list() {
        return ResponseEntity.ok(policyService.list());
    }

    @PreAuthorize("hasAnyRole('INSURANCE_MANAGER', 'INSURANCE_CLIENT')")
    @GetMapping("/getByPolicyId/{id}")
    public ResponseEntity<PolicyDTO> get(@PathVariable UUID id) {
        return ResponseEntity.ok(policyService.get(id));
    }

    @PreAuthorize("hasAnyRole('INSURANCE_MANAGER', 'INSURANCE_CLIENT')")
    @GetMapping("/getByClientId/{userId}")
    public ResponseEntity<PolicyDTO> getByUserId(@PathVariable UUID userId) {
        return ResponseEntity.ok(policyService.getPolicyByUserId(userId));
    }

    @PreAuthorize("hasRole('INSURANCE_MANAGER')")
    @PatchMapping("/update/{id}")
    public ResponseEntity<PolicyDTO> update(@PathVariable UUID id,
                                            @Valid @RequestBody UpdatePolicyDTO dto) {
        PolicyDTO updated = policyService.update(id, dto);

        // 🔔 إشعار

        return ResponseEntity.ok(updated);
    }

    @PreAuthorize("hasRole('INSURANCE_MANAGER')")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        policyService.delete(id);

        // 🔔 إشعار


        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('INSURANCE_CLIENT')")
    @GetMapping("/my-policy")
    public ResponseEntity<PolicyDTO> getMyPolicy(Authentication authentication) {
        String username = authentication.getName();
        PolicyDTO policy = policyService.getPolicyByUsername(username);
        return ResponseEntity.ok(policy);
    }

    // ===================== Coverages تحت بوليصة =====================

    @PreAuthorize("hasRole('INSURANCE_MANAGER')")
    @PostMapping("/{id}/coverages/add")
    public ResponseEntity<CoverageDTO> addCoverage(@PathVariable UUID id,
                                                   @Valid @RequestBody CreateCoverageDTO dto) {
        dto.setPolicyId(id); // ربط الكفرج بالـ PolicyId
        CoverageDTO created = coverageService.add(dto);

        // 🔔 إشعار


        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{covId}").buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PreAuthorize("hasRole('INSURANCE_MANAGER')")
    @GetMapping("/{id}/coverages/all")
    public ResponseEntity<List<CoverageDTO>> listCoverages(@PathVariable UUID id) {
        return ResponseEntity.ok(coverageService.listByPolicy(id));
    }
}
