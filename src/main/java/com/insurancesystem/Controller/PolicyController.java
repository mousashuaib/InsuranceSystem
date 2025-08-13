package com.insurancesystem.Controller;

import com.insurancesystem.Model.Dto.CreatePolicyDTO;
import com.insurancesystem.Model.Dto.PolicyDTO;
import com.insurancesystem.Model.Dto.UpdatePolicyDTO;
import com.insurancesystem.Model.Dto.CoverageDTO;
import com.insurancesystem.Model.Dto.CreateCoverageDTO;
import com.insurancesystem.Services.CoverageService;
import com.insurancesystem.Services.PolicyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/policies")
@RequiredArgsConstructor
public class PolicyController {

    private final PolicyService policyService;
    private final CoverageService coverageService;

    // 1️⃣ إنشاء بوليصة جديدة
    // POST http://localhost:8080/api/policies/create
    @PostMapping("/create")
    public ResponseEntity<PolicyDTO> create(@Valid @RequestBody CreatePolicyDTO dto) {
        PolicyDTO created = policyService.create(dto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    // 2️⃣ جلب قائمة جميع البوالص
    // GET http://localhost:8080/api/policies/all
    @GetMapping("/all")
    public ResponseEntity<List<PolicyDTO>> list() {
        return ResponseEntity.ok(policyService.list());
    }

    // 3️⃣ جلب بوليصة واحدة بالـ ID
    // GET http://localhost:8080/api/policies/get/{policyId}
    @GetMapping("/get/{id}")
    public ResponseEntity<PolicyDTO> get(@PathVariable UUID id) {
        return ResponseEntity.ok(policyService.get(id));
    }

    // 4️⃣ تعديل بوليصة بالـ ID
    // PATCH http://localhost:8080/api/policies/update/{policyId}
    @PatchMapping("/update/{id}")
    public ResponseEntity<PolicyDTO> update(@PathVariable UUID id,
                                            @Valid @RequestBody UpdatePolicyDTO dto) {
        return ResponseEntity.ok(policyService.update(id, dto));
    }

    // 5️⃣ حذف بوليصة
    // DELETE http://localhost:8080/api/policies/delete/{policyId}
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        policyService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ===================== Coverages تحت بوليصة =====================

    // 6️⃣ إضافة Coverage تحت بوليصة معينة
    // POST http://localhost:8080/api/policies/{policyId}/coverages/add
    @PostMapping("/{id}/coverages/add")
    public ResponseEntity<CoverageDTO> addCoverage(@PathVariable UUID id,
                                                   @Valid @RequestBody CreateCoverageDTO dto) {
        dto.setPolicyId(id); // ربط الكفرج بالـ PolicyId
        CoverageDTO created = coverageService.add(dto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{covId}").buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    // 7️⃣ عرض جميع الـ Coverages الخاصة ببوليصة
    // GET http://localhost:8080/api/policies/{policyId}/coverages/all
    @GetMapping("/{id}/coverages/all")
    public ResponseEntity<List<CoverageDTO>> listCoverages(@PathVariable UUID id) {
        return ResponseEntity.ok(coverageService.listByPolicy(id));
    }
}
