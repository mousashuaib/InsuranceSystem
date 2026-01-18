package com.insurancesystem.Model.Entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "coverage_usage", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"client_id", "provider_specialization", "usage_date"})
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoverageUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @Column(name = "provider_specialization", length = 100)
    private String providerSpecialization;

    @Column(name = "service_type", length = 50)
    private String serviceType;

    @Column(name = "usage_date", nullable = false)
    private LocalDate usageDate;

    @Column(name = "year", nullable = false)
    private Integer year;

    @Column(name = "visit_count")
    @Builder.Default
    private Integer visitCount = 1;

    @Column(name = "amount_used", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal amountUsed = BigDecimal.ZERO;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}
