package com.insurancesystem.Model.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "recipient_id", nullable = false)
    private Client recipient; // المستفيد من الإشعار

    @Column(nullable = false, length = 500)
    private String message;

    @Column(name = "is_read", nullable = false) // 👈 هذا يربطه بالعمود is_read
    private boolean read = false; // 👈 حقل الجافا صار اسمه read فقط

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void prePersist() {
        this.createdAt = Instant.now();
    }
}
