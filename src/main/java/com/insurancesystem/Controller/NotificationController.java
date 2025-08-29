package com.insurancesystem.Controller;

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

}
