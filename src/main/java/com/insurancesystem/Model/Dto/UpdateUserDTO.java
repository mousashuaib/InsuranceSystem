package com.insurancesystem.Model.Dto;

import com.insurancesystem.Model.Entity.Enums.MemberStatus;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserDTO {
    @Size(min = 3, max = 150)
    private String fullName;

    @Email @Size(max = 150)
    private String email;

    @Size(max = 40)
    private String phone;

    private MemberStatus status; // للأدمن فقط
}
