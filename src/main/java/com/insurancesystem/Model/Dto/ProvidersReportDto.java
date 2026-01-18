package com.insurancesystem.Model.Dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class ProvidersReportDto {
    private long totalProviders;
    private long doctorsCount;
    private long pharmaciesCount;
    private long labsCount;
    private List<String> doctors;
    private List<String> pharmacies;
    private List<String> labs;
}
