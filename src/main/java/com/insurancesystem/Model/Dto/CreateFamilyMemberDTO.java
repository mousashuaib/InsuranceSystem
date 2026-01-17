package com.insurancesystem.Model.Dto;

import com.insurancesystem.Model.Entity.Enums.FamilyRelation;
import com.insurancesystem.Model.Entity.Enums.Gender;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateFamilyMemberDTO {

    @NotBlank
    @Size(min = 3, max = 150)
    private String fullName;

    @NotBlank
    @Size(min = 9, max = 20)
    private String nationalId;

    @NotNull
    private LocalDate dateOfBirth;

    @NotNull
    private Gender gender;

    @NotNull
    private FamilyRelation relation;
}
