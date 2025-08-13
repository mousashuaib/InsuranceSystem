package com.insurancesystem.Model.Entity;

import com.insurancesystem.Model.Entity.Enums.EnrollmentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(
        name = "member_enrollments",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_member_policy_start",
                columnNames = {"user_id", "policy_id", "start_date"}
        )
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MemberEnrollment {
    @Id @GeneratedValue
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "client_id")
    private Client member;


    @ManyToOne(optional = false)
    @JoinColumn(name = "policy_id")
    private Policy policy;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EnrollmentStatus status = EnrollmentStatus.ACTIVE;

}
