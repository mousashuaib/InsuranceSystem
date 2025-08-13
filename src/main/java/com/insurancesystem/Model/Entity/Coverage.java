package com.insurancesystem.Model.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "coverages",
        uniqueConstraints = @UniqueConstraint(columnNames = {"policy_id", "service_name"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Coverage {

    @Id @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "policy_id")
    private Policy policy;

    @Column(name = "service_name", nullable = false, length = 160)
    private String serviceName;

    @Column(columnDefinition = "text")
    private String description;

    @Builder.Default
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "emergency_eligible", nullable = false)
    private boolean emergencyEligible = false;

}
