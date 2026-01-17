package com.insurancesystem.Model.Dto;

import lombok.*;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChronicDiseaseViewDTO {
    private String name;
    private List<String> documentPaths;
}
