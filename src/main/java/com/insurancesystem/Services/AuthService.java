package com.insurancesystem.Services;

import com.insurancesystem.Exception.BadRequestException;
import com.insurancesystem.Exception.NotFoundException;
import com.insurancesystem.Model.Dto.ClientDto;
import com.insurancesystem.Model.Dto.RegisterFamilyMemberDTO;
import com.insurancesystem.Model.Dto.auth.AuthResponse;
import com.insurancesystem.Model.Dto.auth.LoginRequest;
import com.insurancesystem.Model.Dto.auth.RegisterRequest;
import com.insurancesystem.Model.Dto.auth.RegisterResponse;
import com.insurancesystem.Model.Entity.Enums.MemberStatus;
import com.insurancesystem.Model.Entity.Enums.ProfileStatus;
import com.insurancesystem.Model.Entity.Enums.RoleName;
import com.insurancesystem.Model.Entity.Enums.RoleRequestStatus;
import com.insurancesystem.Model.Entity.Client;
import com.insurancesystem.Model.Entity.FamilyMember;
import com.insurancesystem.Model.MapStruct.ClientMapper;
import com.insurancesystem.Repository.ClientRepository;
import com.insurancesystem.Repository.FamilyMemberRepository;
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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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
    private final FamilyMemberRepository familyRepo;


    // ✅ مكان بسيط لتخزين reset tokens (للتجربة فقط)
    private final Map<String, String> resetTokens = new ConcurrentHashMap<>();

    public RegisterResponse register(
            String reqJson,
            MultipartFile[] universityCard,
            MultipartFile[] familyDocuments,
            MultipartFile[] chronicDocuments,
            String familyDocumentsOwnersJson,
            boolean isAdminRegister
    )


    {

        RegisterRequest req;
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

            req = mapper.readValue(reqJson, RegisterRequest.class);
        } catch (Exception e) {
            e.printStackTrace(); // مهم أثناء التطوير
            throw new BadRequestException("Invalid registration data: " + e.getMessage());
        }


        // ===============================
        // 🔹 Normalize inputs
        // ===============================
        String email = req.getEmail() == null ? null : req.getEmail().trim().toLowerCase();

        // ===============================
// 📧 Email validation (GMAIL ONLY)
// ===============================
        if (email == null || email.isBlank()) {
            throw new BadRequestException("Email is required");
        }

        if (!email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new BadRequestException("Email must be valid (example@domain.com)");
        }
        if (req.getPhone() == null || !req.getPhone().matches("^05\\d{8}$")) {
            throw new BadRequestException(
                    "Phone number must be 10 digits and start with 05"
            );
        }


        // ===============================
        // 🔒 Uniqueness checks
        // ===============================


        if (email != null && clientRepo.existsByEmail(email)) {
            throw new BadRequestException("Email already exists");
        }

        if (req.getNationalId() == null || req.getNationalId().isBlank()) {
            throw new BadRequestException("National ID is required");
        }

        if (clientRepo.existsByNationalId(req.getNationalId())) {
            throw new BadRequestException("National ID already exists");
        }

        // ===============================
        // 🔐 Password validation
        // ===============================
        if (!req.getPassword().matches("^(?=.*[A-Za-z])(?=.*\\d).{8,}$")) {
            throw new BadRequestException(
                    "Password must be at least 8 characters and contain letters and numbers"
            );
        }
        if (req.getEmployeeId() != null && !req.getEmployeeId().isBlank()) {
            if (clientRepo.existsByEmployeeId(req.getEmployeeId())) {
                throw new BadRequestException("Employee ID already exists");
            }
        }


        // ===============================
        // 🎂 Age validation (>= 18)
        // ===============================
        if (req.getDateOfBirth() == null) {
            throw new BadRequestException("Date of birth is required");
        }

        int age = java.time.Period
                .between(req.getDateOfBirth(), java.time.LocalDate.now())
                .getYears();

        if (age < 18) {
            throw new BadRequestException("User must be at least 18 years old");
        }

        // ===============================
        // 🎭 Role handling
        // ===============================
        RoleName role = req.getDesiredRole() == null
                ? RoleName.INSURANCE_CLIENT
                : req.getDesiredRole();

        switch (role) {

            case INSURANCE_CLIENT -> {
                if (req.getEmployeeId() == null ||
                        req.getDepartment() == null ||
                        req.getFaculty() == null) {
                    throw new BadRequestException(
                            "Insurance client must provide employee ID, department, and faculty"
                    );
                }
            }

            case DOCTOR -> {
                if (req.getSpecialization() == null ||
                        req.getClinicLocation() == null) {
                    throw new BadRequestException(
                            "Doctor must provide specialization and clinic location"
                    );
                }
            }

            case PHARMACIST -> {
                if (req.getPharmacyCode() == null ||
                        req.getPharmacyName() == null) {
                    throw new BadRequestException(
                            "Pharmacist must provide pharmacy code and name"
                    );
                }
            }

            case LAB_TECH -> {
                if (req.getLabCode() == null ||
                        req.getLabName() == null) {
                    throw new BadRequestException(
                            "Lab technician must provide lab code and name"
                    );
                }
            }

            case RADIOLOGIST -> {
                if (req.getRadiologyCode() == null ||
                        req.getRadiologyName() == null) {
                    throw new BadRequestException(
                            "Radiologist must provide radiology code and name"
                    );
                }
            }

            case MEDICAL_ADMIN -> {
                if (req.getEmployeeId() == null ||
                        req.getDepartment() == null ||
                        req.getFaculty() == null ||
                        req.getClinicLocation() == null) {
                    throw new BadRequestException(
                            "Medical admin must provide employee ID, department, faculty, and clinic location"
                    );
                }
            }

            case INSURANCE_MANAGER, EMERGENCY_MANAGER -> {
                throw new BadRequestException(
                        "This role can only be created by system administrators"
                );
            }

            case COORDINATION_ADMIN -> {
                if (!isAdminRegister) {
                    throw new BadRequestException(
                            "Coordination Admin can only be created by INSURANCE_MANAGER"
                    );
                }
                if (req.getEmployeeId() == null || req.getDepartment() == null) {
                    throw new BadRequestException(
                            "Coordination Admin must provide employee ID and department"
                    );
                }
            }
        }

        List<String> cardPaths = uploadUniversityCards(universityCard);



        // ===============================
        // 🧩 Status handling
        // ===============================
        MemberStatus status = isAdminRegister
                ? MemberStatus.ACTIVE
                : MemberStatus.INACTIVE;

        RoleRequestStatus roleStatus = isAdminRegister
                ? RoleRequestStatus.APPROVED
                : RoleRequestStatus.PENDING;

        if (role == RoleName.MEDICAL_ADMIN) {
            status = MemberStatus.ACTIVE;
            roleStatus = RoleRequestStatus.APPROVED;
        }

        // ===============================
        // 🟢 Create Client entity
        // ===============================
        Client client = Client.builder()
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .fullName(req.getFullName())
                .nationalId(req.getNationalId())
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
                .dateOfBirth(req.getDateOfBirth())
                .status(status)
                .roleRequestStatus(roleStatus)
                .requestedRole(role)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .universityCardImages(cardPaths)
                .build();

        Client saved = clientRepo.save(client);


        if (role == RoleName.INSURANCE_CLIENT && req.isHasChronicDiseases()) {

            if (req.getChronicDiseases() == null || req.getChronicDiseases().isEmpty()) {
                throw new BadRequestException("Please select at least one chronic disease");
            }

            // ✅ documents required
            if (!isAdminRegister && (chronicDocuments == null || chronicDocuments.length == 0)) {
                throw new BadRequestException("Please upload documents proving chronic diseases");
            }


            saved.setChronicDiseases(new HashSet<>(req.getChronicDiseases()));

            // ✅ upload docs
            List<String> chronicPaths = uploadChronicDocuments(chronicDocuments);
            saved.setChronicDocumentPaths(chronicPaths);

            clientRepo.save(saved);
        }


        if (role == RoleName.INSURANCE_CLIENT &&
                req.getFamilyMembers() != null &&
                !req.getFamilyMembers().isEmpty()) {

            Map<String, List<MultipartFile>> docsByNationalId = new HashMap<>();

            if (familyDocuments != null && familyDocuments.length > 0) {

                if (familyDocumentsOwnersJson == null || familyDocumentsOwnersJson.isBlank()) {
                    throw new BadRequestException("familyDocumentsOwners is required when uploading familyDocuments");
                }

                try {
                    ObjectMapper mapper = new ObjectMapper();
                    List<String> owners = mapper.readValue(
                            familyDocumentsOwnersJson,
                            mapper.getTypeFactory().constructCollectionType(List.class, String.class)
                    );

                    if (owners.size() != familyDocuments.length) {
                        throw new BadRequestException("familyDocumentsOwners size must match familyDocuments size");
                    }

                    // ✅ validate owners values (must belong to family members)
                    Set<String> memberNationalIds = new HashSet<>();
                    for (RegisterFamilyMemberDTO m : req.getFamilyMembers()) {
                        if (m.getNationalId() == null || m.getNationalId().isBlank()) {
                            throw new BadRequestException("Family member nationalId is required");
                        }
                        memberNationalIds.add(m.getNationalId());
                    }

                    for (String ownerId : owners) {
                        if (ownerId == null || ownerId.isBlank()) {
                            throw new BadRequestException("familyDocumentsOwners contains empty nationalId");
                        }
                        if (!memberNationalIds.contains(ownerId)) {
                            throw new BadRequestException("familyDocumentsOwners contains unknown nationalId: " + ownerId);
                        }
                    }

                    for (int i = 0; i < familyDocuments.length; i++) {
                        String ownerNationalId = owners.get(i); // صاحب الملف
                        MultipartFile file = familyDocuments[i];

                        if (file == null || file.isEmpty()) continue;

                        docsByNationalId
                                .computeIfAbsent(ownerNationalId, k -> new ArrayList<>())
                                .add(file);
                    }

                } catch (Exception e) {
                    throw new BadRequestException("Invalid familyDocumentsOwners: " + e.getMessage());
                }
            }

            createFamilyMembersOnRegister(saved, req.getFamilyMembers(), docsByNationalId);

        }


        // ===============================
        // 🔗 Assign role / policy
        // ===============================
        if (isAdminRegister) {
            clientServices.addRoleToClient(saved.getId(), role);
        }

        if (!isAdminRegister &&
                role == RoleName.INSURANCE_CLIENT &&
                req.isAgreeToPolicy()) {
            policyService.assignPolicyByName(
                    saved.getId(),
                    "Birzeit University Premium Plus Plan"
            );
        }

        ClientDto dto = clientMapper.toDTO(saved);

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
    private void createFamilyMembersOnRegister(
            Client client,
            List<RegisterFamilyMemberDTO> members,
            Map<String, List<MultipartFile>> docsByNationalId

    )
    {

        long index = familyRepo.countByClient_Id(client.getId());

        for (int i = 0; i < members.size(); i++) {

            var dto = members.get(i);

            // 🔒 منع تكرار الهوية (حتى مع الأب)
            if (dto.getNationalId().equals(client.getNationalId()) ||
                    familyRepo.existsByNationalId(dto.getNationalId())) {
                throw new BadRequestException("Duplicate national ID in family members");
            }

            // 🎂 تحقق العمر
            int age = java.time.Period
                    .between(dto.getDateOfBirth(), java.time.LocalDate.now())
                    .getYears();

            switch (dto.getRelation()) {
                case SON, DAUGHTER -> {
                    if (age > 25)
                        throw new BadRequestException("Child must be under 25 years old");
                }
                default -> {
                    if (age < 18)
                        throw new BadRequestException("Adult family member must be at least 18");
                }
            }

            // 🔢 توليد رقم التأمين
            index++;
            String insuranceNumber =
                    client.getEmployeeId() + "." + String.format("%02d", index);
            if (familyRepo.existsByInsuranceNumber(insuranceNumber)) {
                throw new BadRequestException("Insurance number already exists");
            }


            List<MultipartFile> memberFiles =
                    docsByNationalId.getOrDefault(dto.getNationalId(), List.of());



            List<String> docPaths = uploadFamilyDocuments(memberFiles);

// مؤقت لحين تعديل Entity لتخزين List

            FamilyMember member = FamilyMember.builder()
                    .client(client)
                    .fullName(dto.getFullName())
                    .nationalId(dto.getNationalId())
                    .insuranceNumber(insuranceNumber)
                    .relation(dto.getRelation())
                    .gender(dto.getGender())
                    .status(ProfileStatus.PENDING) // 🔥 هنا

                    .dateOfBirth(dto.getDateOfBirth())
                    .documentImages(docPaths)
                    .build();

            familyRepo.save(member);
        }
    }


    public AuthResponse login(LoginRequest req) {
        String email = req.getEmail().trim().toLowerCase();

        ClientDto clientDTO = clientServices.getByEmail(email); // تغيير لبحث باستخدام الإيميل
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



        var authToken =
                new UsernamePasswordAuthenticationToken(email, req.getPassword());

        authenticationManager.authenticate(authToken);

        var ud = customUserDetailsService.loadUserByUsername(email);

        String token = jwtService.generateToken(email);

        return AuthResponse.builder()
                .token(token)
                .user(clientDTO)
                .build();
    }

    public void initiatePasswordReset(String email, boolean isMobile) {
        Client client = clientRepo.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Email not found"));

        String token = UUID.randomUUID().toString();
        resetTokens.put(token, client.getEmail());

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
        String email = resetTokens.get(token);
        if (email == null) {
            throw new BadRequestException("Invalid or expired reset token");
        }

        Client client = clientRepo.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));


        client.setPasswordHash(passwordEncoder.encode(newPassword));
        clientRepo.save(client);

        resetTokens.remove(token);
    }

    private List<String> uploadChronicDocuments(MultipartFile[] files) {
        if (files == null || files.length == 0) {
            return List.of();
        }
        List<String> paths = new ArrayList<>();

        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) continue;

            String ext = FilenameUtils.getExtension(file.getOriginalFilename()).toLowerCase();
            if (!List.of("jpg", "jpeg", "png", "pdf").contains(ext)) {
                throw new BadRequestException("Invalid chronic document type");
            }

            try {
                String filename = UUID.randomUUID() + "." + ext;
                Path dir = Path.of("uploads/chronic/");
                Files.createDirectories(dir);
                Files.write(dir.resolve(filename), file.getBytes());
                paths.add("/uploads/chronic/" + filename);
            } catch (IOException e) {
                throw new RuntimeException("Failed to upload chronic documents", e);
            }
        }

        if (paths.isEmpty()) {
            throw new BadRequestException("Please upload at least one chronic document");
        }

        return paths;
    }
    private List<String> uploadUniversityCards(MultipartFile[] files) {
        List<String> paths = new ArrayList<>();
        if (files == null) return paths;

        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) continue;

            String ext = FilenameUtils.getExtension(file.getOriginalFilename()).toLowerCase();
            if (!List.of("jpg", "jpeg", "png", "pdf").contains(ext)) {
                throw new BadRequestException("Invalid university card type");
            }

            try {
                String filename = UUID.randomUUID() + "." + ext;
                Path dir = Path.of("uploads/cards/");
                Files.createDirectories(dir);
                Files.write(dir.resolve(filename), file.getBytes());
                paths.add("/uploads/cards/" + filename);
            } catch (IOException e) {
                throw new RuntimeException("Failed to upload university cards", e);
            }
        }
        return paths;
    }
    private List<String> uploadFamilyDocuments(List<MultipartFile> files) {
        List<String> paths = new ArrayList<>();
        if (files == null) return paths;

        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) continue;

            String ext = FilenameUtils.getExtension(file.getOriginalFilename()).toLowerCase();
            if (!List.of("jpg", "jpeg", "png", "pdf").contains(ext)) {
                throw new BadRequestException("Invalid family document type");
            }

            try {
                String filename = UUID.randomUUID() + "." + ext;
                Path dir = Path.of("uploads/family/");
                Files.createDirectories(dir);
                Files.write(dir.resolve(filename), file.getBytes());
                paths.add("/uploads/family/" + filename);
            } catch (IOException e) {
                throw new RuntimeException("Failed to upload family documents", e);
            }
        }
        return paths;
    }



}