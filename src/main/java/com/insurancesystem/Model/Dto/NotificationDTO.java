package com.insurancesystem.Model.Dto;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {
    private UUID id;
    private String message;
    private boolean read;
    private Instant createdAt;
    private String recipientName; // اسم المستفيد (اختياري للعرض)
    private UUID recipientId;     // ✅ أضف هذا الحقل
    private UUID senderId;        // ✅ المرسل
    private String senderName;    // ✅ اسم المرسل

    private String type;          // ✅ نوع الإشعار

}
