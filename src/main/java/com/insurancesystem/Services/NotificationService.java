package com.insurancesystem.Services;

import com.insurancesystem.Exception.NotFoundException;
import com.insurancesystem.Exception.UnauthorizedException;
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

import java.time.Instant;
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
            original.setReplied(true);
            notificationRepo.save(original);
        }

        Notification notification = Notification.builder()
                .recipient(recipient)
                .sender(sender)
                .message(sender.getFullName() + ": " + message)
                .read(false)
                .type(NotificationType.MANUAL_MESSAGE)
                .build();

        notificationRepo.save(notification);
    }

    public List<NotificationDTO> getUserNotifications(UUID recipientId) {
        Client recipient = clientRepo.findById(recipientId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        List<Notification> notifications = notificationRepo.findByRecipientOrderByCreatedAtDesc(recipient);
        return notifications.stream()
                .map(notificationMapper::toDto)
                .toList();
    }

    public void sendToUser(UUID recipientId, String message) {
        Client recipient = clientRepo.findById(recipientId)
                .orElseThrow(() -> new NotFoundException("Recipient not found"));

        Notification notification = Notification.builder()
                .recipient(recipient)
                .sender(null)
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

    public void markAsRead(UUID notificationId, Client currentUser) {
        Notification notification = notificationRepo.findById(notificationId)
                .orElseThrow(() -> new NotFoundException("Notification not found with id: " + notificationId));

        if (notification.getRecipient() == null ||
                !notification.getRecipient().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("❌ This notification is not yours");
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

    public void deleteNotification(UUID userId, UUID notificationId) {
        Notification notification = notificationRepo.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        if (!notification.getRecipient().getId().equals(userId)) {
            throw new RuntimeException("❌ You are not authorized to delete this notification");
        }

        notificationRepo.delete(notification);
    }

    public void createNotificationByEmail(
            String senderEmail,
            String recipientEmail,
            String message,
            NotificationType type,
            UUID repliedNotificationId
    ) {
        Client sender = clientRepo.findByEmail(senderEmail.toLowerCase())
                .orElseThrow(() -> new NotFoundException("Sender not found"));

        Client recipient = clientRepo.findByEmail(recipientEmail.toLowerCase())
                .orElseThrow(() -> new NotFoundException("Recipient not found"));

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
                .type(type != null ? type : NotificationType.MANUAL_MESSAGE)
                .build();

        notificationRepo.save(notification);
    }

    public void clientDeleteNotification(UUID clientId, UUID notificationId) {
        Notification notification = notificationRepo.findById(notificationId)
                .orElseThrow(() -> new NotFoundException("Notification not found"));

        if (notification.getType() == NotificationType.SYSTEM) {
            notificationRepo.delete(notification);
            return;
        }

        if (!notification.getRecipient().getId().equals(clientId)) {
            throw new RuntimeException("Unauthorized: This notification is not yours");
        }

        notificationRepo.delete(notification);
    }

    public void createNotificationByFullName(String senderFullName, String recipientFullName,
                                             String message, UUID parentId) {
        Client sender = clientRepo.findByFullName(senderFullName)
                .orElseThrow(() -> new RuntimeException("Sender not found with name: " + senderFullName));

        Client recipient = clientRepo.findByFullName(recipientFullName)
                .orElseThrow(() -> new RuntimeException("Recipient not found with name: " + recipientFullName));

        Notification notification = Notification.builder()
                .sender(sender)
                .recipient(recipient)
                .message(message)
                .type(NotificationType.MANUAL_MESSAGE)
                .read(false)
                .replied(false)
                .createdAt(Instant.now())
                .build();

        notificationRepo.save(notification);
    }
}
