package com.insurancesystem.Model.Dto.auth;

import com.insurancesystem.Model.Dto.ClientDto;
import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class RegisterResponse {
    private ClientDto user;
    private String message; // اختياري: توضيح للمستخدم (e.g., "Registered successfully. Please login.")
}
