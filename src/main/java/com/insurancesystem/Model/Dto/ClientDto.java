package com.insurancesystem.Model.Dto;

import com.insurancesystem.Model.Entity.Enums.MemberStatus;
import com.insurancesystem.Model.Entity.Enums.RoleName;
import com.insurancesystem.Model.Entity.Enums.RoleRequestStatus;
import lombok.*;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ClientDto {
    private UUID id;
    private String username;
    private String fullName;
    private String email;
    private String phone;
    private MemberStatus status;
    private Set<RoleName> roles;
    private Instant createdAt;
    private Instant updatedAt;
    private String universityCardImage; // ✅ هذا السطر مهم

    private RoleName requestedRole;
    private RoleRequestStatus roleRequestStatus; // ← Enum وليس String
}
