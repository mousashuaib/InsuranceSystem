package com.insurancesystem.Services;

import com.insurancesystem.Exception.NotFoundException;
import com.insurancesystem.Model.Dto.NotificationDTO;
import com.insurancesystem.Model.Entity.Client;
import com.insurancesystem.Model.Entity.Notification;
import com.insurancesystem.Model.Entity.Enums.RoleName;
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

    // ➕ إرسال إشعار لمستخدم واحد
    public void sendToUser(UUID recipientId, String message) {
        Client recipient = clientRepo.findById(recipientId)
                .orElseThrow(() -> new NotFoundException("Recipient not found"));

        Notification notification = Notification.builder()
                .recipient(recipient)
                .message(message)
                .read(false)
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
                        .message(message)
                        .read(false)
                        .build())
                .toList();

        notificationRepo.saveAll(notifications);
    }


    //  استرجاع إشعارات مستخدم
    public List<NotificationDTO> getUserNotifications(UUID recipientId) {
        Client recipient = clientRepo.findById(recipientId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        return notificationRepo.findByRecipientOrderByCreatedAtDesc(recipient)
                .stream()
                .map(notificationMapper::toDto)
                .toList();
    }


    public void markAllAsRead(UUID recipientId) {
        Client recipient = clientRepo.findById(recipientId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        List<Notification> notifications = notificationRepo.findByRecipientOrderByCreatedAtDesc(recipient);
        notifications.forEach(n -> n.setRead(true));
        notificationRepo.saveAll(notifications);
    }

    //  تعليم كل الإشعارات كمقروء عند كل المدراء (أو أي Role تختاره)
    public void markAllAsReadForRole(RoleName roleName) {
        List<Client> clients = clientRepo.findAll().stream()
                .filter(c -> c.getRoles().stream().anyMatch(r -> r.getName() == roleName))
                .toList();

        clients.forEach(c -> markAllAsRead(c.getId()));
    }

    public void markNotificationAsReadByMessage(RoleName roleName, String message) {
        List<Client> clients = clientRepo.findAll().stream()
                .filter(c -> c.getRoles().stream().anyMatch(r -> r.getName() == roleName))
                .toList();

        for (Client client : clients) {
            List<Notification> notifications = notificationRepo.findByRecipientOrderByCreatedAtDesc(client);
            notifications.stream()
                    .filter(n -> n.getMessage().equals(message)) // أو contains إذا بدك نص مشابه
                    .forEach(n -> n.setRead(true));

            notificationRepo.saveAll(notifications);
        }
    }



}
