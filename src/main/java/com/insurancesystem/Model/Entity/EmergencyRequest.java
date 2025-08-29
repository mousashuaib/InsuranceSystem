package com.insurancesystem.Model.Entity;

import com.insurancesystem.Model.Entity.Enums.EmergencyStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "emergency_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmergencyRequest {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client member;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private String location;

    @Column(nullable = false)
    private String contactPhone;

    @Column(nullable = false)
    private LocalDate incidentDate;

    private String notes;

    @Enumerated(EnumType.STRING)
    private EmergencyStatus status = EmergencyStatus.PENDING;

    private Instant submittedAt = Instant.now();
    private Instant approvedAt;
    private Instant rejectedAt;
    private String rejectionReason;
}
