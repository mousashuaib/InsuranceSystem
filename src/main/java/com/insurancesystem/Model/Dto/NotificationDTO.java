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
}
