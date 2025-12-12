package com.insurancesystem.Model.Dto;

import lombok.Data;
import java.util.List;
import java.util.UUID;

@Data
public class AdminBatchApproveDTO {
    private List<UUID> claimIds;
}
