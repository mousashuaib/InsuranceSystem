package com.insurancesystem.Controller;

import com.insurancesystem.Model.Dto.MembersActivityReportDto;
import com.insurancesystem.Services.MembersActivityReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports/members-activity")
@RequiredArgsConstructor
public class MembersActivityReportController {

    private final MembersActivityReportService service;

    @GetMapping
    @PreAuthorize("hasRole('INSURANCE_MANAGER')")
    public MembersActivityReportDto getMembersActivityReport() {
        return service.generateReport();
    }
}
