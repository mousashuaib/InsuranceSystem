package com.insurancesystem.Model.Dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResetPasswordRequest {
    @NotBlank
    private String token; // التوكن اللي يوصله على الإيميل

    @NotBlank
    private String newPassword;
}
