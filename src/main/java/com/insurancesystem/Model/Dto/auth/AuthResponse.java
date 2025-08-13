package com.insurancesystem.Model.Dto.auth;
import com.insurancesystem.Model.Dto.ClientDto;
import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class AuthResponse {
    private String token;
    private ClientDto user;
}
