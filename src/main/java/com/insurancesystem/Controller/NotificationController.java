package com.insurancesystem.Controller;

import com.insurancesystem.Model.Dto.CreateNotificationManualDTO;
import com.insurancesystem.Model.Dto.NotificationDTO;
import com.insurancesystem.Model.Entity.Client;
import com.insurancesystem.Repository.ClientRepository;
import com.insurancesystem.Services.NotificationService;
import lombok.RequiredArgsConstructor;
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


    // ➕ إرسال استفسار/إشعار يدوي
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
    @PatchMapping("/{id}/read")
    @PreAuthorize("isAuthenticated()")
    public void markAsRead(@PathVariable UUID id, Authentication auth) {
        String username = auth.getName();
        Client user = clientRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        notificationService.markAsRead(user.getId(), id);
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



}
