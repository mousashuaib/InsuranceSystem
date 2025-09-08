package com.insurancesystem.Model.Dto;

import com.insurancesystem.Model.Entity.Enums.RoleName;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateClientDto {

    @NotBlank @Size(min = 3, max = 64)
    private String username;

    @NotBlank @Size(min = 8, max = 72)
    private String password; // سيُشفّر في Service

    @NotBlank @Size(min = 3, max = 150)
    private String fullName;

    @Email @Size(max = 150)
    private String email;

    @Size(max = 40)
    private String phone;

    @NotEmpty
    private Set<RoleName> roles;


}
