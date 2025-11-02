package com.insurancesystem.Controller;

import com.insurancesystem.Model.Dto.ClientDto;
import com.insurancesystem.Model.Dto.CreateNotificationManualDTO;
import com.insurancesystem.Model.Dto.NotificationDTO;
import com.insurancesystem.Model.Dto.RecipientDto;
import com.insurancesystem.Model.Entity.Client;
import com.insurancesystem.Model.Entity.Enums.RoleName;
import com.insurancesystem.Repository.ClientRepository;
import com.insurancesystem.Services.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final ClientRepository clientRepo;

    // إرسال استفسار/إشعار يدوي
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public void sendNotification(@RequestBody CreateNotificationManualDTO dto, Authentication auth) {
        String senderUsername = auth.getName();
        Client sender = clientRepo.findByUsername(senderUsername)
                .orElseThrow(() -> new RuntimeException("Sender not found"));

        notificationService.createNotification(sender.getId(), dto.getRecipientId(), dto.getMessage(), null);
    }

    // ➕ الرد على إشعار سابق
    @PostMapping("/{notificationId}/reply")
    @PreAuthorize("isAuthenticated()")
    public void replyNotification(@PathVariable UUID notificationId,
                                  @RequestBody CreateNotificationManualDTO dto,
                                  Authentication auth) {
        String senderUsername = auth.getName();
        Client sender = clientRepo.findByUsername(senderUsername)
                .orElseThrow(() -> new RuntimeException("Sender not found"));

        notificationService.createNotification(sender.getId(), dto.getRecipientId(), dto.getMessage(), notificationId);
    }

    // 📖 عرض إشعارات المستخدم الحالي
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<NotificationDTO> getMyNotifications(Authentication auth) {
        String username = auth.getName();
        Client user = clientRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return notificationService.getUserNotifications(user.getId());
    }

    @PatchMapping("/read-all")
    @PreAuthorize("isAuthenticated()")
    public void markAllAsRead(Authentication auth) {
        String username = auth.getName();
        Client user = clientRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        notificationService.markAllAsRead(user.getId());
    }

    // ✅ نسخة موحدة للقراءة
    @PatchMapping("/{id}/read")
    @PreAuthorize("isAuthenticated()")
    public void markAsRead(@PathVariable UUID id, Authentication auth) {
        String username = auth.getName();
        Client user = clientRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        notificationService.markAsRead(id, user);
    }

    @GetMapping("/unread-count")
    @PreAuthorize("isAuthenticated()")
    public long getUnreadCount(Authentication auth) {
        String username = auth.getName();
        Client user = clientRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return notificationService.countUnreadNotifications(user.getId());
    }


    @GetMapping("/unread-count/emergency")
    @PreAuthorize("hasRole('EMERGENCY_MANAGER')")
    public long getUnreadEmergencyCount(Authentication auth) {
        String username = auth.getName();
        Client user = clientRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return notificationService.countUnreadEmergencyNotifications(user.getId());
    }

    // 🗑️ حذف إشعار
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> deleteNotification(@PathVariable UUID id, Authentication auth) {
        String username = auth.getName();
        Client user = clientRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        notificationService.deleteNotification(user.getId(), id);
        return ResponseEntity.ok("✅ Notification deleted successfully");
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/by-name")
    public void sendByName(@RequestBody CreateNotificationManualDTO dto, Authentication auth) {
        notificationService.createNotificationByName(
                auth.getName(), dto.getRecipientName(), dto.getMessage(), dto.getType(), null
        );
    }

    @DeleteMapping("/{id}/client")
    @PreAuthorize("isAuthenticated()")
    public void clientDelete(@PathVariable UUID id, Authentication auth) {
        String username = auth.getName();
        Client client = clientRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Client not found"));
        notificationService.clientDeleteNotification(client.getId(), id);
    }

    @GetMapping("/debug")
    public String debug(Authentication auth) {
        return "User: " + auth.getName() + " | Authorities: " + auth.getAuthorities();
    }

    @PostMapping("/by-fullname")
    @PreAuthorize("isAuthenticated()")
    public void sendByFullName(@RequestBody CreateNotificationManualDTO dto, Authentication auth) {
        String username = auth.getName();
        Client sender = clientRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Sender not found"));

        notificationService.createNotificationByFullName(
                sender.getFullName(),
                dto.getRecipientName(),
                dto.getMessage(),
                null
        );
    }
    @GetMapping("/recipients")
    @PreAuthorize("isAuthenticated()")    public List<RecipientDto> getRecipients() {
        return clientRepo.findAll().stream()
                .filter(c -> c.getRoles().stream().anyMatch(r ->
                        r.getName() == RoleName.INSURANCE_MANAGER ||
                                r.getName() == RoleName.EMERGENCY_MANAGER ||
                                r.getName() == RoleName.DOCTOR ||
                                r.getName() == RoleName.PHARMACIST ||
                                r.getName() == RoleName.LAB_TECH
                ))
                .map(c -> new RecipientDto(c.getId(), c.getFullName()))
                .toList();
    }


}
