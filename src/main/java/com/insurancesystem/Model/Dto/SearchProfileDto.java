package com.insurancesystem.Model.Dto;

import com.insurancesystem.Model.Entity.Enums.SearchProfileType;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchProfileDto {

    private UUID id;
    private String name;
    private SearchProfileType type;
    private String address;
    private Double locationLat;
    private Double locationLng;
    private String contactInfo;
    private String description;
    private String ownerName; // اسم المالك (doctor/pharmacist/lab)

}
