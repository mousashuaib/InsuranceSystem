package com.insurancesystem.Model.Dto;

import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class RejectClaimDTO {
    private String reason;
}
