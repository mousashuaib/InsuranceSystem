package com.insurancesystem.Controller;

import com.insurancesystem.Model.Entity.Enums.ChronicDisease;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chronic-diseases")
@RequiredArgsConstructor
public class ChronicDiseaseController {

    @PreAuthorize("hasAnyRole('COORDINATION_ADMIN', 'MEDICAL_ADMIN','INSURANCE_MANAGER')")
    @GetMapping
    public List<Map<String, String>> list() {
        return Arrays.stream(ChronicDisease.values())
                .map(d -> Map.of(
                        "code", d.name(),
                        "name", d.name().replace("_", " ")
                ))
                .toList();
    }
}
