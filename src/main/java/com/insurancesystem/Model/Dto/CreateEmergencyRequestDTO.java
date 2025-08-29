package com.insurancesystem.Model.Dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class CreateEmergencyRequestDTO {
    private String description;
    private String location;
    private String contactPhone;
    private LocalDate incidentDate;
    private String notes;
}
