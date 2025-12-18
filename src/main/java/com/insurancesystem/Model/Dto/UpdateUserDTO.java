package com.insurancesystem.Model.Dto;

import com.insurancesystem.Model.Entity.DoctorSpecializationEntity;
import com.insurancesystem.Model.Entity.Enums.MemberStatus;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserDTO {

    @Size(min = 3, max = 150)
    private String fullName;

    @Email
    @Size(max = 150)
    private String email;

    @Size(max = 40)
    private String phone;

    private MemberStatus status; // للأدمن فقط
    List<String> universityCardImages;
    // 🟢 الحقول الجديدة (ضرورية لتطابق ClientMapper)
    private String employeeId;
    private String department;
    private String faculty;
    private String specialization;
    private String clinicLocation;
    private String pharmacyCode;
    private String pharmacyName;
    private String pharmacyLocation;
    private String labCode;
    private String labName;
    private String labLocation;
    private DoctorSpecializationEntity doctorSpecialization;
}