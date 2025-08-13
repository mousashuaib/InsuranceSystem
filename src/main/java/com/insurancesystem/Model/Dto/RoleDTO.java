package com.insurancesystem.Model.Dto;

import com.insurancesystem.Model.Entity.Enums.RoleName;
import lombok.*;

import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class RoleDTO {
    private UUID id;
    private RoleName name;
}
