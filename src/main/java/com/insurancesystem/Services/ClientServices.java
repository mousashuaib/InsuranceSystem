package com.insurancesystem.Services;

import com.insurancesystem.Exception.BadRequestException;
import com.insurancesystem.Exception.NotFoundException;
import com.insurancesystem.Model.Dto.ClientDto;
import com.insurancesystem.Model.Dto.UpdateUserDTO;
import com.insurancesystem.Model.Entity.Client;
import com.insurancesystem.Model.Entity.Enums.MemberStatus;
import com.insurancesystem.Model.Entity.Enums.RoleName;
import com.insurancesystem.Model.Entity.Enums.RoleRequestStatus;
import com.insurancesystem.Model.MapStruct.ClientMapper;
import com.insurancesystem.Repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ClientServices {

    private final ClientRepository clientRepo;
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

    public ClientDto update(UUID id, UpdateUserDTO dto, MultipartFile universityCard) {
        Client user = clientRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));

        // ✅ تحديث البيانات الأساسية
        clientMapper.updateEntityFromDTO(dto, user);

        // ✅ تخزين الصورة إن وُجدت
        if (universityCard != null && !universityCard.isEmpty()) {
            String fileName = UUID.randomUUID() + "_" + universityCard.getOriginalFilename();
            Path uploadPath = Paths.get("uploads/cards");

            try {
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }
                Path filePath = uploadPath.resolve(fileName);
                Files.copy(universityCard.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                user.setUniversityCardImage("/uploads/cards/" + fileName);
            } catch (IOException e) {
                throw new RuntimeException("❌ Failed to save university card image", e);
            }
        }

        user.setUpdatedAt(Instant.now());
        clientRepo.save(user);

        return clientMapper.toDTO(user);
    }



    @Transactional(readOnly = true)
    public ClientDto getByUsername(String username) {
        var user = clientRepo.findByUsername(username.toLowerCase())
                .orElseThrow(() -> new NotFoundException("User not found"));
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

        clientRepo.save(u);

        emailService.sendRoleApprovalEmail(u.getEmail(), u.getFullName(), role.getName());

        notificationService.sendToUser(
                u.getId(),
                "تمت الموافقة على حسابك. يمكنك تسجيل الدخول الآن."
        );
        notificationService.markNotificationAsReadByMessage(
                RoleName.INSURANCE_MANAGER,
                "مستخدم جديد (" + u.getFullName() + ") سجل وينتظر الموافقة."
        );

        return clientMapper.toDTO(u);
    }

    @Transactional
    public void rejectRoleRequest(UUID clientId, String reason) {
        Client u = clientRepo.findById(clientId)
                .orElseThrow(() -> new NotFoundException("Client not found"));

        if (u.getRoleRequestStatus() == null || u.getRequestedRole() == null
                || u.getRoleRequestStatus() != RoleRequestStatus.PENDING) {
            throw new BadRequestException("No pending role request for this client");
        }

        // ✅ تحديث الحالة بدل الحذف
        u.setRoleRequestStatus(RoleRequestStatus.REJECTED);
        u.setStatus(MemberStatus.INACTIVE); // يبقى غير مفعل
        u.setUpdatedAt(Instant.now());
        clientRepo.save(u);

        // 🔹 إرسال الإشعارات والإيميل
        emailService.sendRoleRejectionEmail(u.getEmail(), u.getFullName(), u.getRequestedRole(), reason);

        notificationService.sendToUser(
                u.getId(),
                "❌ تم رفض طلب حسابك. السبب: " + reason
        );

        notificationService.markNotificationAsReadByMessage(
                RoleName.INSURANCE_MANAGER,
                "مستخدم جديد (" + u.getFullName() + ") سجل وينتظر الموافقة."
        );
    }



    // 🔹 تحديث البروفايل بناءً على Username
    public ClientDto updateByUsername(String username, UpdateUserDTO dto, MultipartFile universityCard) {
        Client client = clientRepo.findByUsername(username)
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

        // ✅ الصورة
        if (universityCard != null && !universityCard.isEmpty()) {
            try {
                String fileName = UUID.randomUUID() + "_" + universityCard.getOriginalFilename();
                Path uploadPath = Paths.get("uploads/profile");
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }
                Path filePath = uploadPath.resolve(fileName);
                Files.copy(universityCard.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                client.setUniversityCardImage("/uploads/profile/" + fileName);
            } catch (IOException e) {
                throw new RuntimeException("❌ Failed to save profile image", e);
            }
        }

        client.setUpdatedAt(Instant.now());
        Client updated = clientRepo.save(client);

        return clientMapper.toDTO(updated);
    }
    // ✅ يستخدمها المدير أو النظام لإضافة دور لمستخدم معين مباشرة
    @Transactional
    public void addRoleToClient(UUID clientId, RoleName roleName) {
        // 🔸 البحث عن المستخدم
        Client client = clientRepo.findById(clientId)
                .orElseThrow(() -> new NotFoundException("Client not found"));

        // 🔸 جلب الدور المطلوب من خدمة الأدوار
        var role = roleService.getByNameOrThrow(roleName);

        // 🔸 إضافة الدور للمستخدم (لو مش مضاف مسبقًا)
        if (!client.getRoles().contains(role)) {
            client.getRoles().add(role);
        }

        // 🔸 تحديث حالة الحساب كمفعّل
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

        // ✅ فقط غيّر الحالة إلى DEACTIVATED، ولا تلمس الموافقة
        client.setStatus(MemberStatus.DEACTIVATED);
        client.setUpdatedAt(Instant.now());
        clientRepo.save(client);

        notificationService.sendToUser(
                client.getId(),
                "🚫 تم تعطيل حسابك من قبل الإدارة. السبب: " + reason
        );
    }



    @Transactional
    public void reactivateClient(UUID id) {
        Client client = clientRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (client.getStatus() != MemberStatus.DEACTIVATED) {
            throw new BadRequestException("الحساب ليس معطلًا.");
        }

        // ✅ أعد تفعيله، وابقِ الـ RoleRequestStatus كما هو (APPROVED)
        client.setStatus(MemberStatus.ACTIVE);
        client.setUpdatedAt(Instant.now());
        clientRepo.save(client);

        notificationService.sendToUser(
                client.getId(),
                "✅ تم إعادة تفعيل حسابك بنجاح."
        );
    }

}