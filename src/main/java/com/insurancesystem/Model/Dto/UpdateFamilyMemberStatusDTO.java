package com.insurancesystem.Model.Dto;

import com.insurancesystem.Model.Entity.Enums.ProfileStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateFamilyMemberStatusDTO {
    @NotNull
    private ProfileStatus status;
}
