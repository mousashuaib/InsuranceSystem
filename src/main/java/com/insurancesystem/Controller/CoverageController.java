package com.insurancesystem.Controller;

import com.insurancesystem.Model.Dto.CoverageDTO;
import com.insurancesystem.Model.Dto.UpdateCoverageDTO;
import com.insurancesystem.Services.CoverageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.UUID;

@RestController
@RequestMapping("/api/coverages")
@RequiredArgsConstructor
public class CoverageController {

    private final CoverageService coverageService;

    // 1️⃣ تحديث Coverage بالـ ID
    // PATCH http://localhost:8080/api/coverages/update/{coverageId}
    @PreAuthorize("hasRole('INSURANCE_MANAGER')")
    @PatchMapping("/update/{id}")
    public ResponseEntity<CoverageDTO> update(@PathVariable UUID id,
                                              @Valid @RequestBody UpdateCoverageDTO dto) {
        CoverageDTO updated = coverageService.update(id, dto);


        return ResponseEntity.ok(updated);
    }

    // 2️⃣ حذف Coverage
    // DELETE http://localhost:8080/api/coverages/delete/{coverageId}
    @PreAuthorize("hasRole('INSURANCE_MANAGER')")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        coverageService.delete(id);


        return ResponseEntity.noContent().build();
    }
}
