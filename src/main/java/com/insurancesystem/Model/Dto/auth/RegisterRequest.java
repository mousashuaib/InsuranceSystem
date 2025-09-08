package com.insurancesystem.Model.Dto.auth;

import com.insurancesystem.Model.Entity.Enums.RoleName;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;
import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class RegisterRequest {

    @NotBlank @Size(min = 3, max = 64)
    private String username;

    @NotBlank @Size(min = 8, max = 72)
    private String password;

    @NotBlank @Size(min = 3, max = 150)
    private String fullName;

    @Email @Size(max = 150)
    private String email;

    @Size(max = 40)
    private String phone;

    // RegisterRequest.java
    private RoleName desiredRole; // OPTIONAL
    private boolean agreeToPolicy;



}
