package com.insurancesystem.Controller;

import com.insurancesystem.Model.Dto.*;
import com.insurancesystem.Model.Entity.Enums.ProviderType;
import com.insurancesystem.Services.PriceListService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/pricelist")
@RequiredArgsConstructor
public class PriceListController {

    private final PriceListService priceListService;

    @PostMapping
    @PreAuthorize("hasRole('INSURANCE_MANAGER')")
    public PriceListResponseDTO create(@RequestBody CreatePriceListDTO dto) {
        return priceListService.create(dto);
    }

    @GetMapping("/{type}")
    @PreAuthorize("hasAnyRole('INSURANCE_MANAGER','DOCTOR','PHARMACIST')")
    public List<PriceListResponseDTO> getByType(@PathVariable ProviderType type) {
        return priceListService.getByType(type);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('INSURANCE_MANAGER')")
    public PriceListResponseDTO update(
            @PathVariable UUID id,
            @RequestBody UpdatePriceListDTO dto
    ) {
        return priceListService.updatePrice(id, dto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('INSURANCE_MANAGER')")
    public void delete(@PathVariable UUID id) {
        priceListService.deletePrice(id);
    }

    // 🧪 Lab Tests
    @GetMapping("/lab/tests")
    @PreAuthorize("hasAnyRole('DOCTOR','LAB_TECH','INSURANCE_MANAGER','MANAGER')")
    public List<PriceListResponseDTO> getLabTests() {
        return priceListService.getByProviderType(ProviderType.LAB);
    }

    // 🩻 Radiology Tests
    @GetMapping("/radiology/tests")
    @PreAuthorize("hasAnyRole('DOCTOR','RADIOLOGIST','INSURANCE_MANAGER','MANAGER')")
    public List<PriceListResponseDTO> getRadiologyTests() {
        return priceListService.getByProviderType(ProviderType.RADIOLOGY);
    }
}
