package com.insurancesystem.Services;

import com.insurancesystem.Exception.BadRequestException;
import com.insurancesystem.Exception.NotFoundException;
import com.insurancesystem.Model.Dto.ClientDto;
import com.insurancesystem.Model.Dto.auth.AuthResponse;
import com.insurancesystem.Model.Dto.auth.LoginRequest;
import com.insurancesystem.Model.Dto.auth.RegisterRequest;
import com.insurancesystem.Model.Dto.auth.RegisterResponse;
import com.insurancesystem.Model.Entity.Enums.MemberStatus;
import com.insurancesystem.Model.Entity.Enums.RoleName;
import com.insurancesystem.Model.Entity.Enums.RoleRequestStatus;
import com.insurancesystem.Model.Entity.Client;
import com.insurancesystem.Model.MapStruct.ClientMapper;
import com.insurancesystem.Repository.ClientRepository;
import com.insurancesystem.Security.CustomUserDetailsService;
import com.insurancesystem.Security.JwtService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final ClientRepository clientRepo;
    private final RoleService roleService;
    private final ClientMapper clientMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService customUserDetailsService;
    private final JwtService jwtService;
    private final ClientServices clientServices;
    private final NotificationService notificationService;
    private final PolicyService policyService;
    private final EmailService emailService;

    // ✅ مكان بسيط لتخزين reset tokens (للتجربة فقط)
    private final Map<String, String> resetTokens = new HashMap<>();

    public RegisterResponse register(String reqJson, MultipartFile universityCard, boolean isAdminRegister) {
        RegisterRequest req;
        try {
            req = new com.fasterxml.jackson.databind.ObjectMapper().readValue(reqJson, RegisterRequest.class);
        } catch (Exception e) {
            throw new BadRequestException("Invalid registration data");
        }

        String username = req.getUsername().trim().toLowerCase();
        String email = req.getEmail() == null ? null : req.getEmail().trim().toLowerCase();

        if (clientRepo.existsByUsername(username))
            throw new BadRequestException("Username already exists");
        if (email != null && clientRepo.existsByEmail(email))
            throw new BadRequestException("Email already exists");

        RoleName role = req.getDesiredRole() == null ? RoleName.INSURANCE_CLIENT : req.getDesiredRole();

        // ✅ التحقق من الحقول حسب الدور
        switch (role) {
            case INSURANCE_CLIENT -> {
                if (req.getEmployeeId() == null || req.getDepartment() == null || req.getFaculty() == null)
                    throw new BadRequestException("Insurance client must provide employee ID, department, and faculty");
            }
            case DOCTOR -> {
                if (req.getSpecialization() == null || req.getClinicLocation() == null)
                    throw new BadRequestException("Doctor must provide specialization and clinic location");
            }
            case PHARMACIST -> {
                if (req.getPharmacyCode() == null || req.getPharmacyName() == null)
                    throw new BadRequestException("Pharmacist must provide pharmacy code and name");
            }
            case LAB_TECH -> {
                if (req.getLabCode() == null || req.getLabName() == null)
                    throw new BadRequestException("Lab technician must provide lab code and name");
            }
            case RADIOLOGIST -> {
                if (req.getRadiologyCode() == null || req.getRadiologyName() == null)
                    throw new BadRequestException("Radiologist must provide radiology code and name");
            }
            case MEDICAL_ADMIN -> {
                if (req.getEmployeeId() == null || req.getDepartment() == null || req.getFaculty() == null || req.getClinicLocation() == null)
                    throw new BadRequestException("Medical admin must provide employee ID, department, faculty, and clinic location");
            }


            case INSURANCE_MANAGER, EMERGENCY_MANAGER -> {
                throw new BadRequestException("This role can only be created by system administrators");
            }
            case COORDINATION_ADMIN -> {
                if (!isAdminRegister)
                    throw new BadRequestException("Coordination Admin can only be created by INSURANCE_MANAGER");

                if (req.getEmployeeId() == null || req.getDepartment() == null)
                    throw new BadRequestException("Coordination Admin must provide employee ID and department");
            }

        }

        // ✅ رفع صورة البطاقة الجامعية (اختياري)
        String imagePath = null;
        if (universityCard != null && !universityCard.isEmpty()) {
            try {
                String ext = FilenameUtils.getExtension(universityCard.getOriginalFilename());
                String filename = UUID.randomUUID() + "." + ext;
                Path uploadDir = Path.of("uploads/cards/");
                Files.createDirectories(uploadDir);
                Path path = uploadDir.resolve(filename);
                Files.write(path, universityCard.getBytes());
                imagePath = "/uploads/cards/" + filename;
            } catch (IOException e) {
                throw new RuntimeException("Failed to upload university card", e);
            }
        }

        // 🧩 تحديد الحالة حسب من قام بالتسجيل
        MemberStatus status = isAdminRegister ? MemberStatus.ACTIVE : MemberStatus.INACTIVE;
        RoleRequestStatus roleStatus = isAdminRegister ? RoleRequestStatus.APPROVED : RoleRequestStatus.PENDING;
        if (role == RoleName.MEDICAL_ADMIN) {
            status = MemberStatus.ACTIVE;
            roleStatus = RoleRequestStatus.APPROVED;
        }

        // 🟢 إنشاء الكيان
        Client client = Client.builder()
                .username(username)
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .fullName(req.getFullName())
                .email(email)
                .phone(req.getPhone())
                .employeeId(req.getEmployeeId())
                .department(req.getDepartment())
                .faculty(req.getFaculty())
                .specialization(req.getSpecialization())
                .clinicLocation(req.getClinicLocation())
                .pharmacyCode(req.getPharmacyCode())
                .pharmacyName(req.getPharmacyName())
                .pharmacyLocation(req.getPharmacyLocation())
                .labCode(req.getLabCode())
                .labName(req.getLabName())
                .labLocation(req.getLabLocation())
                .radiologyCode(req.getRadiologyCode())
                .radiologyName(req.getRadiologyName())
                .radiologyLocation(req.getRadiologyLocation())

                .status(status)
                .roleRequestStatus(roleStatus)
                .radiologyCode(req.getRadiologyCode())        // ✅ أضف هذا
                .radiologyName(req.getRadiologyName())        // ✅ أضف هذا
                .radiologyLocation(req.getRadiologyLocation()) // ✅ أضف هذا
                .requestedRole(role)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .universityCardImage(imagePath)
                .build();

        Client saved = clientRepo.save(client);

        // لو كان المدير هو اللي أنشأ الحساب، نربط الدور مباشرة
        if (isAdminRegister) {
            clientServices.addRoleToClient(saved.getId(), role);
        }

        // لو عميل سجل بنفسه ووافق على السياسة، نربطه بالخطة تلقائيًا
        if (!isAdminRegister && role == RoleName.INSURANCE_CLIENT && req.isAgreeToPolicy()) {
            policyService.assignPolicyByName(saved.getId(), "Birzeit University Premium Plus Plan");
        }

        ClientDto dto = clientMapper.toDTO(saved);

        // إشعار للمدير عند التسجيل العام
        if (!isAdminRegister) {
            notificationService.sendToRole(
                    RoleName.INSURANCE_MANAGER,
                    "مستخدم جديد (" + saved.getFullName() + ") سجل وينتظر الموافقة."
            );
        }

        return RegisterResponse.builder()
                .user(dto)
                .message(isAdminRegister
                        ? "✅ Account created successfully by admin"
                        : "Registration submitted successfully. Awaiting manager approval.")
                .build();
    }

    public AuthResponse login(LoginRequest req) {
        String username = req.getUsername().trim().toLowerCase();

        ClientDto clientDTO = clientServices.getByUsername(username);
        if (clientDTO == null) {
            throw new NotFoundException("User not found");
        }

        MemberStatus status = clientDTO.getStatus();

        switch (status) {
            case ACTIVE -> { /* ✅ يسمح بالدخول */ }
            case INACTIVE -> throw new BadRequestException("⏳ حسابك بانتظار موافقة الإدارة.");
            case DEACTIVATED -> throw new BadRequestException("🚫 تم تعطيل حسابك من قبل الإدارة.");
            default -> throw new BadRequestException("❌ حالة الحساب غير معروفة.");
        }


        var authToken = new UsernamePasswordAuthenticationToken(username, req.getPassword());
        authenticationManager.authenticate(authToken);

        var ud = customUserDetailsService.loadUserByUsername(username);

        String token = jwtService.generateToken(ud.getUsername());

        return AuthResponse.builder()
                .token(token)
                .user(clientDTO)
                .build();
    }

    public void initiatePasswordReset(String email, boolean isMobile) {
        Client client = clientRepo.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Email not found"));

        String token = UUID.randomUUID().toString();
        resetTokens.put(token, client.getUsername());

        String webResetLink = "http://localhost:5173/reset-password?token=" + token;
        String mobileResetLink = "mobileinsurancesystem://Auth/ResetPassword?token=" + token;

        if (isMobile) {
            sendMobileResetEmail(client, mobileResetLink);
        } else {
            sendWebResetEmail(client, webResetLink);
        }
    }

    private void sendWebResetEmail(Client client, String webResetLink) {
        emailService.sendCustomEmail(
                client.getEmail(),
                "Password Reset Request (Web)",
                """
                Dear %s,<br><br>
                We received a request to reset your password.<br><br>

                🌐 <a href="%s">Reset your password via Web</a><br><br>

                If you didn’t request a password reset, you can safely ignore this email.<br><br>
                Best regards,<br>
                Insurance System Team
                """.formatted(client.getFullName(), webResetLink)
        );
    }

    private void sendMobileResetEmail(Client client, String mobileResetLink) {
        emailService.sendCustomEmail(
                client.getEmail(),
                "Password Reset Request (Mobile)",
                """
                Dear %s,<br><br>
                We received a request to reset your password.<br><br>

                📱 <a href="%s">Reset your password via Mobile</a><br><br>

                If you didn’t request a password reset, you can safely ignore this email.<br><br>
                Best regards,<br>
                Insurance System Team
                """.formatted(client.getFullName(), mobileResetLink)
        );
    }

    public void resetPassword(String token, String newPassword) {
        String username = resetTokens.get(token);
        if (username == null) {
            throw new BadRequestException("Invalid or expired reset token");
        }

        Client client = clientRepo.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found"));

        client.setPasswordHash(passwordEncoder.encode(newPassword));
        clientRepo.save(client);

        resetTokens.remove(token);
    }
}
