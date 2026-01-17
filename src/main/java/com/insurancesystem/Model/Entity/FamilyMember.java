package com.insurancesystem.Model.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.insurancesystem.Model.Entity.Enums.FamilyRelation;
import com.insurancesystem.Model.Entity.Enums.Gender;
import com.insurancesystem.Model.Entity.Enums.ProfileStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
        name = "family_members",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "national_id"),
                @UniqueConstraint(columnNames = "insurance_number")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FamilyMember {

    @Id
    @GeneratedValue
    private UUID id;

    // 🔗 المؤمن الأساسي
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    @JsonIgnore
    private Client client;

    // بيانات الشخص
    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Column(name = "national_id", nullable = false, length = 20)
    private String nationalId;

    // رقم التأمين (12443.01)
    @Column(name = "insurance_number", nullable = false, length = 30)
    private String insuranceNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FamilyRelation relation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Gender gender;

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "family_member_documents",
            joinColumns = @JoinColumn(name = "family_member_id")
    )
    @Column(name = "document_path")
    private List<String> documentImages = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProfileStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
        if (this.status == null) {
            this.status = ProfileStatus.PENDING; // 👈 افتراضياً Pending
        }
    }


}