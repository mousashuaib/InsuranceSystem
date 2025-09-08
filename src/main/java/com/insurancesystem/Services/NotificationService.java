package com.insurancesystem.Services;

import com.insurancesystem.Exception.NotFoundException;
import com.insurancesystem.Model.Dto.NotificationDTO;
import com.insurancesystem.Model.Entity.Client;
import com.insurancesystem.Model.Entity.Notification;
import com.insurancesystem.Model.Entity.Enums.RoleName;
import com.insurancesystem.Model.Entity.Enums.NotificationType;
import com.insurancesystem.Model.MapStruct.NotificationMapper;
import com.insurancesystem.Repository.ClientRepository;
import com.insurancesystem.Repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepo;
    private final ClientRepository clientRepo;
    private final NotificationMapper notificationMapper;

    // ➕ إرسال إشعار يدوي (استفسار أو رد)
    public void createNotification(UUID senderId, UUID recipientId, String message, UUID repliedNotificationId) {
        Client recipient = clientRepo.findById(recipientId)
                .orElseThrow(() -> new NotFoundException("Recipient not found"));
        Client sender = clientRepo.findById(senderId)
                .orElseThrow(() -> new NotFoundException("Sender not found"));

        if (repliedNotificationId != null) {
            Notification original = notificationRepo.findById(repliedNotificationId)
                    .orElseThrow(() -> new NotFoundException("Original notification not found"));
            original.setRead(true);
            notificationRepo.save(original);
        }

        Notification notification = Notification.builder()
                .recipient(recipient)
                .sender(sender)
                .message(sender.getFullName() + ": " + message)
                .read(false)
                .type(NotificationType.MANUAL_MESSAGE) // ✅ نوعها رسالة يدوية
                .build();

        notificationRepo.save(notification);
    }

    public List<NotificationDTO> getUserNotifications(UUID recipientId) {
        Client recipient = clientRepo.findById(recipientId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        List<Notification> notifications = notificationRepo.findByRecipientOrderByCreatedAtDesc(recipient);

        // ❌ لا تغيّر حالة read هنا
        return notifications.stream()
                .map(notificationMapper::toDto)
                .toList();
    }


    public void sendToUser(UUID recipientId, String message) {
        Client recipient = clientRepo.findById(recipientId)
                .orElseThrow(() -> new NotFoundException("Recipient not found"));

        Notification notification = Notification.builder()
                .recipient(recipient)
                .sender(null) // إشعار نظامي بدون مرسل
                .message(message)
                .read(false)
                .type(NotificationType.SYSTEM)
                .build();

        notificationRepo.save(notification);
    }

    public void sendToRole(RoleName roleName, String message) {
        List<Client> clients = clientRepo.findAll().stream()
                .filter(c -> c.getRoles().stream().anyMatch(r -> r.getName() == roleName))
                .toList();

        List<Notification> notifications = clients.stream()
                .map(client -> Notification.builder()
                        .recipient(client)
                        .sender(null)
                        .message(message)
                        .read(false)
                        .type(NotificationType.SYSTEM)
                        .build())
                .toList();

        notificationRepo.saveAll(notifications);
    }

    public void markAllAsRead(UUID recipientId) {
        Client recipient = clientRepo.findById(recipientId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        List<Notification> notifications = notificationRepo.findByRecipientOrderByCreatedAtDesc(recipient);
        notifications.forEach(n -> n.setRead(true));
        notificationRepo.saveAll(notifications);
    }

    // ✅ نسخة متوافقة مع الكود القديم
    public void markNotificationAsReadByMessage(RoleName roleName, String message) {
        List<Client> clients = clientRepo.findAll().stream()
                .filter(c -> c.getRoles().stream().anyMatch(r -> r.getName() == roleName))
                .toList();

        for (Client client : clients) {
            List<Notification> notifications = notificationRepo.findByRecipientOrderByCreatedAtDesc(client);
            notifications.stream()
                    .filter(n -> n.getMessage().equals(message))
                    .forEach(n -> n.setRead(true));

            notificationRepo.saveAll(notifications);
        }
    }
    public void markAsRead(UUID recipientId, UUID notificationId) {
        Notification notification = notificationRepo.findById(notificationId)
                .orElseThrow(() -> new NotFoundException("Notification not found"));

        if (!notification.getRecipient().getId().equals(recipientId)) {
            throw new RuntimeException("Unauthorized to read this notification");
        }

        notification.setRead(true);
        notificationRepo.save(notification);
    }
    public long countUnreadNotifications(UUID recipientId) {
        Client recipient = clientRepo.findById(recipientId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        return notificationRepo.countByRecipientAndReadFalse(recipient);
    }
    public long countUnreadEmergencyNotifications(UUID recipientId) {
        Client recipient = clientRepo.findById(recipientId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        return notificationRepo.countByRecipientAndReadFalse(recipient);
    }



}
