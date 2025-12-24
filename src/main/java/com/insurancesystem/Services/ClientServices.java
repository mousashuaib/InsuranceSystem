package com.insurancesystem.Services;

import com.insurancesystem.Exception.BadRequestException;
import com.insurancesystem.Exception.NotFoundException;
import com.insurancesystem.Model.Dto.ClientDto;
import com.insurancesystem.Model.Dto.CoordinatorClientLookupDTO;
import com.insurancesystem.Model.Dto.UpdateUserDTO;
import com.insurancesystem.Model.Entity.Client;
import com.insurancesystem.Model.Entity.Enums.MemberStatus;
import com.insurancesystem.Model.Entity.Enums.ProfileStatus;
import com.insurancesystem.Model.Entity.Enums.RoleName;
import com.insurancesystem.Model.Entity.Enums.RoleRequestStatus;
import com.insurancesystem.Model.Entity.FamilyMember;
import com.insurancesystem.Model.MapStruct.ClientMapper;
import com.insurancesystem.Repository.ClientRepository;
import com.insurancesystem.Repository.FamilyMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ClientServices {

    private final ClientRepository clientRepo;
    private final FamilyMemberRepository familyMemberRepository;
    private final RoleService roleService;
    private final ClientMapper clientMapper;
    private final EmailService emailService;
    private final PolicyService policyService;
    private final NotificationService notificationService;

    @Transactional(readOnly = true)
    public List<ClientDto> list() {
        return clientRepo.findAll().stream().map(clientMapper::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public ClientDto getById(UUID id) {
        Client user = clientRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
        return clientMapper.toDTO(user);
    }

    public ClientDto update(UUID id, UpdateUserDTO dto, MultipartFile[] universityCards)
    {
        Client user = clientRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));

        clientMapper.updateEntityFromDTO(dto, user);
        // تحديث الحقول الجديدة
        if (dto.getNationalId() != null) {
            user.setNationalId(dto.getNationalId());
        }

        if (dto.getDateOfBirth() != null) {
            user.setDateOfBirth(dto.getDateOfBirth());
        }
        if (universityCards != null && universityCards.length > 0) {
            Path uploadPath = Paths.get("uploads/cards");
            try {
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                for (MultipartFile file : universityCards) {
                    if (file == null || file.isEmpty()) continue;

                    String original = file.getOriginalFilename();
                    String safeName = original == null ? "file" : original.replaceAll("[^a-zA-Z0-9._-]", "_");
                    String fileName = UUID.randomUUID() + "_" + safeName;
                    Path filePath = uploadPath.resolve(fileName);

                    Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                    // ✅ أضف المسار للقائمة
                    user.getUniversityCardImages().add("/uploads/cards/" + fileName);
                }
            } catch (IOException e) {
                throw new RuntimeException("❌ Failed to save university card images", e);
            }
        }

        user.setUpdatedAt(Instant.now());
        clientRepo.save(user);
        return clientMapper.toDTO(user);
    }



    @Transactional(readOnly = true)
    public List<ClientDto> listUsersWithPendingRole() {
        return clientRepo.findAll().stream()
                .filter(u -> u.getRoleRequestStatus() == RoleRequestStatus.PENDING)
                .map(clientMapper::toDTO)
                .toList();
    }

    public ClientDto approveRequestedRole(UUID userId) {
        Client u = clientRepo.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (u.getRoleRequestStatus() != RoleRequestStatus.PENDING || u.getRequestedRole() == null)
            throw new BadRequestException("No pending role request");

        var role = roleService.getByNameOrThrow(u.getRequestedRole());
        u.getRoles().add(role);
        u.setStatus(MemberStatus.ACTIVE);
        u.setRoleRequestStatus(RoleRequestStatus.APPROVED);
        u.setRequestedRole(null);

        // تخصيص الأدوار بحيث:
        // إذا كان الدور هو "INSURANCE_CLIENT"، يتم إرسال إشعار للمسؤول الطبي
        if (role.getName() == RoleName.INSURANCE_CLIENT) {
            notificationService.sendToUser(u.getId(), "تمت الموافقة على حسابك كعميل. يمكنك تسجيل الدخول الآن.");
            // إرسال إشعار للمسؤول الطبي
            notificationService.sendToRole(RoleName.MEDICAL_ADMIN, "تمت الموافقة على طلبك كعميل: " + u.getFullName());
        } else {
            notificationService.sendToUser(u.getId(), "تمت الموافقة على طلب دورك.");
        }

        clientRepo.save(u);
        emailService.sendRoleApprovalEmail(u.getEmail(), u.getFullName(), role.getName());
        return clientMapper.toDTO(u);
    }


    @Transactional
    public void rejectRoleRequest(UUID clientId, String reason) {
        Client u = clientRepo.findById(clientId)
                .orElseThrow(() -> new NotFoundException("Client not found"));

        if (u.getRoleRequestStatus() == null || u.getRequestedRole() == null ||
                u.getRoleRequestStatus() != RoleRequestStatus.PENDING) {
            throw new BadRequestException("No pending role request for this client");
        }

        u.setRoleRequestStatus(RoleRequestStatus.REJECTED);
        u.setStatus(MemberStatus.INACTIVE);
        u.setUpdatedAt(Instant.now());
        clientRepo.save(u);

        emailService.sendRoleRejectionEmail(u.getEmail(), u.getFullName(), u.getRequestedRole(), reason);
        notificationService.sendToUser(u.getId(), "❌ تم رفض طلب حسابك. السبب: " + reason);
        notificationService.markNotificationAsReadByMessage(RoleName.INSURANCE_MANAGER,
                "مستخدم جديد (" + u.getFullName() + ") سجل وينتظر الموافقة.");
    }

    public ClientDto updateByEmail(String email, UpdateUserDTO dto, MultipartFile[] universityCards) {
        Client client = clientRepo.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new RuntimeException("Client not found"));



        if (dto.getFullName() != null && !dto.getFullName().isBlank()) {
            client.setFullName(dto.getFullName());
        }
        if (dto.getEmail() != null && !dto.getEmail().isBlank()) {
            client.setEmail(dto.getEmail());
        }
        if (dto.getPhone() != null && !dto.getPhone().isBlank()) {
            client.setPhone(dto.getPhone());
        }

        // تحديث الحقول الجديدة
        if (dto.getNationalId() != null) {
            client.setNationalId(dto.getNationalId());
        }

        if (dto.getDateOfBirth() != null) {
            client.setDateOfBirth(dto.getDateOfBirth());
        }
        if (universityCards != null && universityCards.length > 0) {
            try {
                Path uploadPath = Paths.get("uploads/profile");
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                // ✅ امسحي القديم قبل ما ترفعي الجديد (حل مشكلتك)
                client.getUniversityCardImages().clear();

                for (MultipartFile file : universityCards) {
                    if (file == null || file.isEmpty()) continue;

                    String original = file.getOriginalFilename();
                    String safeName = original == null ? "file" : original.replaceAll("[^a-zA-Z0-9._-]", "_");
                    String fileName = UUID.randomUUID() + "_" + safeName;
                    Path filePath = uploadPath.resolve(fileName);

                    Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                    // ✅ خزّني فقط المسار الصحيح
                    client.getUniversityCardImages().add("/uploads/profile/" + fileName);
                }

            } catch (IOException e) {
                throw new RuntimeException("❌ Failed to save profile images", e);
            }
        }


        client.setUpdatedAt(Instant.now());
        Client updated = clientRepo.save(client);
        return clientMapper.toDTO(updated);
    }

    @Transactional
    public void addRoleToClient(UUID clientId, RoleName roleName) {
        Client client = clientRepo.findById(clientId)
                .orElseThrow(() -> new NotFoundException("Client not found"));

        var role = roleService.getByNameOrThrow(roleName);
        if (!client.getRoles().contains(role)) {
            client.getRoles().add(role);
        }

        client.setStatus(MemberStatus.ACTIVE);
        client.setRoleRequestStatus(RoleRequestStatus.APPROVED);
        client.setRequestedRole(null);
        client.setUpdatedAt(Instant.now());
        clientRepo.save(client);
    }

    @Transactional
    public void deactivateClient(UUID id, String reason) {
        Client client = clientRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (client.getStatus() == MemberStatus.DEACTIVATED) {
            throw new BadRequestException("الحساب معطل بالفعل.");
        }

        client.setStatus(MemberStatus.DEACTIVATED);
        client.setUpdatedAt(Instant.now());
        clientRepo.save(client);
        notificationService.sendToUser(client.getId(), "🚫 تم تعطيل حسابك من قبل الإدارة. السبب: " + reason);
    }

    @Transactional
    public void reactivateClient(UUID id) {
        Client client = clientRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (client.getStatus() != MemberStatus.DEACTIVATED) {
            throw new BadRequestException("الحساب ليس معطلًا.");
        }

        client.setStatus(MemberStatus.ACTIVE);
        client.setUpdatedAt(Instant.now());
        clientRepo.save(client);
        notificationService.sendToUser(client.getId(), "✅ تم إعادة تفعيل حسابك بنجاح.");
    }



    /**
     * 🆔 Find client by employee ID
     * Used by doctors to auto-populate patient information in medical forms
     *
     * @param employeeId The employee ID to search for
     * @return ClientDto with patient details (fullName, department, faculty, etc.)
     */
    @Transactional(readOnly = true)
    public ClientDto findByEmployeeId(String employeeId) {
        Client client = clientRepo.findByEmployeeId(employeeId)
                .orElseThrow(() -> new NotFoundException("Employee ID not found: " + employeeId));
        return clientMapper.toDTO(client);
    }

    /**
     * 👤 Find client by full name
     * Fallback method for patient search by name
     *
     * @param fullName The full name to search for
     * @return ClientDto with patient details
     */
    @Transactional(readOnly = true)
    public ClientDto findByFullName(String fullName) {
        Client client = clientRepo.findByFullName(fullName)
                .orElseThrow(() -> new NotFoundException("Client not found with name: " + fullName));
        return clientMapper.toDTO(client);
    }
    @Transactional(readOnly = true)
    public ClientDto getByEmail(String email) {
        Client client = clientRepo.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new NotFoundException("User not found"));
        return clientMapper.toDTO(client);
    }
    public void clearUniversityCardsByEmail(String email) {
        Client client = clientRepo.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new RuntimeException("Client not found"));
        for (String pathStr : client.getUniversityCardImages()) {
            try {
                if (pathStr != null && pathStr.startsWith("/uploads/")) {
                    // pathStr مثل: /uploads/profile/abc.png
                    Path filePath = Paths.get(pathStr.substring(1)); // remove leading "/"
                    Files.deleteIfExists(filePath);
                }
            } catch (Exception ignored) {}
        }
        client.getUniversityCardImages().clear();

        client.getUniversityCardImages().clear();
        client.setUpdatedAt(Instant.now());
        clientRepo.save(client);
    }
    public ClientDto findClientForCoordinatorClaim(
            CoordinatorClientLookupDTO dto
    ) {

        if (
                (dto.getFullName() == null || dto.getFullName().isBlank()) &&
                        (dto.getEmployeeId() == null || dto.getEmployeeId().isBlank()) &&
                        (dto.getNationalId() == null || dto.getNationalId().isBlank()) &&
                        (dto.getPhone() == null || dto.getPhone().isBlank())
        ) {
            throw new BadRequestException("At least one search field is required");
        }

        Client client = clientRepo.findForCoordinatorClaim(
                dto.getFullName(),
                dto.getEmployeeId(),
                dto.getNationalId(),
                dto.getPhone()
        ).orElseThrow(() ->
                new NotFoundException("No matching client found")
        );

        // تأكيد أن المستخدم مؤمن
        boolean isInsuranceClient = client.getRoles().stream()
                .anyMatch(r -> r.getName() == RoleName.INSURANCE_CLIENT);

        if (!isInsuranceClient) {
            throw new BadRequestException("User is not an insurance client");
        }

        return clientMapper.toDTO(client);
    }

    @Transactional
    public void approveClientRoleRequest(UUID clientId) {

        Client client = clientRepo.findById(clientId)
                .orElseThrow(() -> new NotFoundException("Client not found"));

        // 1️⃣ تفعيل الكلاينت
        client.setStatus(MemberStatus.ACTIVE);
        client.setRoleRequestStatus(RoleRequestStatus.APPROVED);

        // 2️⃣ إعطاؤه الدور المطلوب
        addRoleToClient(client.getId(), client.getRequestedRole());

        // 3️⃣ 🔥 قبول كل أفراد العائلة
        List<FamilyMember> family = familyMemberRepository.findByClient_Id(clientId);

        for (FamilyMember member : family) {
            if (member.getStatus() == ProfileStatus.PENDING) {
                member.setStatus(ProfileStatus.APPROVED);
            }
        }

        clientRepo.save(client);
    }

}

