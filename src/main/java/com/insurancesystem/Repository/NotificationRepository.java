package com.insurancesystem.Repository;

import com.insurancesystem.Model.Entity.Client;
import com.insurancesystem.Model.Entity.Enums.NotificationType;
import com.insurancesystem.Model.Entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    List<Notification> findByRecipientOrderByCreatedAtDesc(Client recipient);
    long countByRecipientAndReadFalse(Client recipient);
    long countByRecipientAndTypeAndReadFalse(Client recipient, NotificationType type);


}
