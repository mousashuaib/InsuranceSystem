package com.insurancesystem.Model.Dto;

import com.insurancesystem.Model.Entity.Enums.NotificationType;
import lombok.Data;
import java.util.UUID;

@Data
public class CreateNotificationManualDTO {

        private UUID recipientId;     // اختياري (ممكن يجي null)
        private String recipientName; // اختياري (ممكن يجي null)
        private String message;       // نص الإشعار (إجباري)
    private NotificationType type;      // نوع الإشعار (MANUAL_MESSAGE, CLAIM, EMERGENCY, SYSTEM)

}
