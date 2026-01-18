package com.insurancesystem.Model.Dto.auth;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
public class LoginRequest {
    private String email;  // بدل username
    private String password;
}
