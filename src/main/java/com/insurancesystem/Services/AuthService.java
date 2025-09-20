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

    public RegisterResponse register(String reqJson, MultipartFile universityCard) {
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

        String imagePath = null;
        if (universityCard != null && !universityCard.isEmpty()) {
            try {
                String ext = FilenameUtils.getExtension(universityCard.getOriginalFilename());
                String filename = UUID.randomUUID().toString() + "." + ext;

                Path uploadDir = Path.of("uploads/cards/");
                Files.createDirectories(uploadDir);

                Path path = uploadDir.resolve(filename);
                Files.write(path, universityCard.getBytes());

                imagePath = "/uploads/cards/" + filename;
            } catch (IOException e) {
                throw new RuntimeException("Failed to upload university card", e);
            }
        }

        Client client = Client.builder()
                .username(username)
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .fullName(req.getFullName())
                .email(email)
                .phone(req.getPhone())
                .status(MemberStatus.INACTIVE)
                .roleRequestStatus(RoleRequestStatus.PENDING)
                .requestedRole(req.getDesiredRole() == null ? RoleName.INSURANCE_CLIENT : req.getDesiredRole())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .universityCardImage(imagePath)
                .build();

        if (client.getRequestedRole() == RoleName.INSURANCE_MANAGER || client.getRequestedRole() == RoleName.EMERGENCY_MANAGER) {
            throw new BadRequestException("This role can only be created by system administrators");
        }

        Client saved = clientRepo.save(client);

        if (imagePath != null) {
            saved.setUniversityCardImage(imagePath);
            saved = clientRepo.save(saved);
        }

        if (client.getRequestedRole() == RoleName.INSURANCE_CLIENT && req.isAgreeToPolicy()) {
            policyService.assignPolicyByName(saved.getId(), "Birzeit University Premium Plus Plan");
        }

        ClientDto dto = clientMapper.toDTO(saved);

        notificationService.sendToRole(
                RoleName.INSURANCE_MANAGER,
                "مستخدم جديد (" + saved.getFullName() + ") سجل وينتظر الموافقة."
        );

        return RegisterResponse.builder()
                .user(dto)
                .message("Registration submitted. Waiting for Insurance Manager approval.")
                .build();
    }

    public AuthResponse login(LoginRequest req) {
        String username = req.getUsername().trim().toLowerCase();

        ClientDto clientDTO = clientServices.getByUsername(username);
        if (clientDTO == null) {
            throw new NotFoundException("User not found");
        }

        if (!"ACTIVE".equalsIgnoreCase(clientDTO.getStatus().name())) {
            throw new BadRequestException("Account is not active. Please wait for approval.");
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
            // 📱 نبعث بس للموبايل
            sendMobileResetEmail(client, mobileResetLink);
        } else {
            // 🌐 نبعث بس للويب
            sendWebResetEmail(client, webResetLink);
        }
    }

    // ======================
// 📌 إرسال لينك الويب فقط
// ======================
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

    // ======================
// 📌 إرسال لينك الموبايل فقط
// ======================
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
