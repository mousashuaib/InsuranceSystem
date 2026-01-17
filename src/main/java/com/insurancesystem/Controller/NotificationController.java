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
        String senderEmail = auth.getName().toLowerCase();
        Client sender = clientRepo.findByEmail(senderEmail)
                .orElseThrow(() -> new RuntimeException("Sender not found"));


        notificationService.createNotification(sender.getId(), dto.getRecipientId(), dto.getMessage(), null);
    }

    // ➕ الرد على إشعار سابق
    @PostMapping("/{notificationId}/reply")
    @PreAuthorize("isAuthenticated()")
    public void replyNotification(@PathVariable UUID notificationId,
                                  @RequestBody CreateNotificationManualDTO dto,
                                  Authentication auth) {
        String senderEmail = auth.getName().toLowerCase();
        Client sender = clientRepo.findByEmail(senderEmail)
                .orElseThrow(() -> new RuntimeException("Sender not found"));


        notificationService.createNotification(sender.getId(), dto.getRecipientId(), dto.getMessage(), notificationId);
    }

    // 📖 عرض إشعارات المستخدم الحالي
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<NotificationDTO> getMyNotifications(Authentication auth) {
        String senderEmail = auth.getName().toLowerCase();
        Client user = clientRepo.findByEmail(senderEmail)
                .orElseThrow(() -> new RuntimeException("user not found"));

        return notificationService.getUserNotifications(user.getId());
    }

    @PatchMapping("/read-all")
    @PreAuthorize("isAuthenticated()")
    public void markAllAsRead(Authentication auth) {
        String senderEmail = auth.getName().toLowerCase();
        Client user = clientRepo.findByEmail(senderEmail)
                .orElseThrow(() -> new RuntimeException("user not found"));

        notificationService.markAllAsRead(user.getId());
    }

    // ✅ نسخة موحدة للقراءة
    @PatchMapping("/{id}/read")
    @PreAuthorize("isAuthenticated()")
    public void markAsRead(@PathVariable UUID id, Authentication auth) {
        String senderEmail = auth.getName().toLowerCase();
        Client user = clientRepo.findByEmail(senderEmail)
                .orElseThrow(() -> new RuntimeException("user not found"));
        notificationService.markAsRead(id, user);
    }

    @GetMapping("/unread-count")
    @PreAuthorize("isAuthenticated()")
    public long getUnreadCount(Authentication auth) {
        String senderEmail = auth.getName().toLowerCase();
        Client user = clientRepo.findByEmail(senderEmail)
                .orElseThrow(() -> new RuntimeException("user not found"));
        return notificationService.countUnreadNotifications(user.getId());
    }


    @GetMapping("/unread-count/emergency")
    @PreAuthorize("hasRole('EMERGENCY_MANAGER')")
    public long getUnreadEmergencyCount(Authentication auth) {
        String senderEmail = auth.getName().toLowerCase();
        Client user = clientRepo.findByEmail(senderEmail)
                .orElseThrow(() -> new RuntimeException("user not found"));
        return notificationService.countUnreadEmergencyNotifications(user.getId());
    }

    // 🗑️ حذف إشعار
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> deleteNotification(@PathVariable UUID id, Authentication auth) {
        String senderEmail = auth.getName().toLowerCase();
        Client user = clientRepo.findByEmail(senderEmail)
                .orElseThrow(() -> new RuntimeException("user not found"));

        notificationService.deleteNotification(user.getId(), id);
        return ResponseEntity.ok("✅ Notification deleted successfully");
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/by-name")
    public void sendByName(@RequestBody CreateNotificationManualDTO dto, Authentication auth) {
        notificationService.createNotificationByEmail(
                auth.getName(),              // senderEmail
                dto.getRecipientName(),      // لازم تكون recipientEmail (عدّل اسم الحقل بالـ DTO)
                dto.getMessage(),
                dto.getType(),
                null
        );

    }

    @DeleteMapping("/{id}/client")
    @PreAuthorize("isAuthenticated()")
    public void clientDelete(@PathVariable UUID id, Authentication auth) {
        String senderEmail = auth.getName().toLowerCase();
        Client client = clientRepo.findByEmail(senderEmail)
                .orElseThrow(() -> new RuntimeException("client not found"));
        notificationService.clientDeleteNotification(client.getId(), id);
    }

    @GetMapping("/debug")
    public String debug(Authentication auth) {
        return "User: " + auth.getName() + " | Authorities: " + auth.getAuthorities();
    }

    @PostMapping("/by-fullname")
    @PreAuthorize("isAuthenticated()")
    public void sendByFullName(@RequestBody CreateNotificationManualDTO dto, Authentication auth) {
        String senderEmail = auth.getName().toLowerCase();
        Client sender = clientRepo.findByEmail(senderEmail)
                .orElseThrow(() -> new RuntimeException("Sender not found"));


        notificationService.createNotificationByFullName(
                sender.getFullName(),
                dto.getRecipientName(),
                dto.getMessage(),
                null
        );
    }
    @GetMapping("/recipients")
    @PreAuthorize("isAuthenticated()")
    public List<RecipientDto> getRecipients() {
        return clientRepo.findAll().stream()
                // 🔹 استبعاد العملاء فقط
                .filter(c -> c.getRoles().stream()
                        .noneMatch(r -> r.getName() == RoleName.INSURANCE_CLIENT))
                // 🔹 تحويلهم إلى DTO (id + fullName)
                .map(c -> new RecipientDto(c.getId(), c.getFullName()))
                .toList();
    }



}
