package com.insurancesystem.Model.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import com.insurancesystem.Model.Entity.Enums.MemberStatus;
import com.insurancesystem.Model.Entity.Enums.RoleName;
import com.insurancesystem.Model.Entity.Enums.RoleRequestStatus;

import com.insurancesystem.Model.Entity.Enums.*;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.util.*;

@Entity
@Table(
        name = "clients",
        indexes = {
                @Index(name = "idx_clients_email", columnList = "email")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Client {

    @Id
    @GeneratedValue
    private UUID id;



    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Column(unique = true, length = 150)
    private String email;

    @Column(length = 40)
    private String phone;

    @Column(name = "employee_id", unique = true, length = 50)
    private String employeeId;


    @Column(name = "department", length = 150)
    private String department;

    @Column(name = "faculty", length = 150)
    private String faculty;

    @Column(name = "specialization", length = 150)
    private String specialization;

    @Column(name = "clinic_location", length = 200)
    private String clinicLocation;

    @Column(name = "pharmacy_code", length = 50)
    private String pharmacyCode;

    @Column(name = "pharmacy_name", length = 150)
    private String pharmacyName;

    @Column(name = "pharmacy_location", length = 200)
    private String pharmacyLocation;

    @Column(name = "lab_code", length = 50)
    private String labCode;

    @Column(name = "lab_name", length = 150)
    private String labName;

    @Column(name = "lab_location", length = 200)
    private String labLocation;
    @Column(name = "radiology_code", length = 50)
    private String radiologyCode;

    @Column(name = "radiology_name", length = 150)
    private String radiologyName;

    @Column(name = "radiology_location", length = 200)
    private String radiologyLocation;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MemberStatus status = MemberStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(name = "requested_role", length = 40)
    private RoleName requestedRole;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "role_request_status", length = 20, nullable = false)
    private RoleRequestStatus roleRequestStatus = RoleRequestStatus.NONE;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "client_university_cards",
            joinColumns = @JoinColumn(name = "client_id")
    )
    @Column(name = "image_path")
    private List<String> universityCardImages = new ArrayList<>();


    // ✅ منع إعادة تحميل كامل الـ Policy → Clients → Policy loop
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id")
    private Policy policy;

    // ✅ منع الدورات التكرارية
    @JsonIgnore
    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SearchProfile> searchProfiles = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "recipient", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Notification> notifications = new ArrayList<>();

    // داخل Client entity

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;


    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    @Column(name = "national_id", unique = true, length = 20)
    private String nationalId;



    @JsonIgnore
    @Builder.Default
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "client_roles",
            joinColumns = @JoinColumn(name = "client_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.roleRequestStatus == null) this.roleRequestStatus = RoleRequestStatus.NONE;
    }

    @PreUpdate
    void preUpdate() {
        this.updatedAt = Instant.now();
    }
    // ✅ التحقق من أن المستخدم يمتلك دور معين
    public boolean hasRole(RoleName roleName) {
        if (roles == null) return false;
        return roles.stream().anyMatch(r -> r.getName() == roleName);
    }


    @Transient
    public Integer getAge() {
        if (this.dateOfBirth == null) return null;
        return Period.between(this.dateOfBirth, LocalDate.now()).getYears();
    }

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(
            name = "client_chronic_diseases",
            joinColumns = @JoinColumn(name = "client_id")
    )
    @Column(name = "disease")
    private Set<ChronicDisease> chronicDiseases = new HashSet<>();


    @Builder.Default
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "client_chronic_documents", joinColumns = @JoinColumn(name = "client_id"))
    @Column(name = "document_path")
    private List<String> chronicDocumentPaths = new ArrayList<>();

    @Column(name = "gender", length = 10)
    private String gender; // حقل الجنس
    @Column(
            name = "email_verified",
            nullable = false,
            columnDefinition = "boolean default false"
    )
    @Builder.Default
    private boolean emailVerified = false;


    @Column(name = "email_verification_code", length = 10)
    private String emailVerificationCode;

    @Column(name = "email_verification_expiry")
    private Instant emailVerificationExpiry;

}
