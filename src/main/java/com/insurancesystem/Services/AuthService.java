package com.insurancesystem.Services;

import com.insurancesystem.Exception.BadRequestException;
import com.insurancesystem.Exception.NotFoundException;
import com.insurancesystem.Model.Dto.ClientDto;
import com.insurancesystem.Model.Dto.auth.AuthResponse;
import com.insurancesystem.Model.Dto.auth.LoginRequest;
import com.insurancesystem.Model.Dto.auth.RegisterRequest;
import com.insurancesystem.Model.Dto.auth.RegisterResponse;
import com.insurancesystem.Model.Entity.Enums.MemberStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.insurancesystem.Model.Entity.Enums.RoleName;
import com.insurancesystem.Model.Entity.Enums.RoleRequestStatus;
import com.insurancesystem.Model.Entity.Role;
import com.insurancesystem.Model.Entity.Client;
import com.insurancesystem.Model.MapStruct.ClientMapper;
import com.insurancesystem.Repository.ClientRepository;
import com.insurancesystem.Security.CustomUserDetailsService;
import com.insurancesystem.Security.JwtService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

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

    public RegisterResponse register(String reqJson, MultipartFile universityCard) {
        RegisterRequest req;
        try {
            req = new ObjectMapper().readValue(reqJson, RegisterRequest.class);
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
                Path path = Path.of("uploads/cards/" + filename);
                Files.createDirectories(path.getParent());
                Files.write(path, universityCard.getBytes());
                imagePath = "cards/" + filename;
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
                .universityCardImage(imagePath)
                .status(MemberStatus.INACTIVE)
                .roleRequestStatus(RoleRequestStatus.PENDING)
                .requestedRole(req.getDesiredRole() == null ? RoleName.INSURANCE_CLIENT : req.getDesiredRole())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        Client saved = clientRepo.save(client);
        ClientDto dto = clientMapper.toDTO(saved);

        return RegisterResponse.builder()
                .user(dto)
                .message("Registration submitted. Waiting for Insurance Manager approval.")
                .build();
    }


    public AuthResponse login(LoginRequest req) {
        String username = req.getUsername().trim().toLowerCase();

        var authToken = new UsernamePasswordAuthenticationToken(username, req.getPassword());
        authenticationManager.authenticate(authToken);

        clientRepo.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found"));

        var ud = customUserDetailsService.loadUserByUsername(username);
        String token = jwtService.generateToken(ud.getUsername());

        ClientDto clientDTO = clientServices.getByUsername(username);
        return AuthResponse.builder()
                .token(token)
                .user(clientDTO)
                .build();
    }
}
