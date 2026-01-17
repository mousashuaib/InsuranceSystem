package com.insurancesystem.Model.Entity;

import com.insurancesystem.Model.Entity.Enums.ProviderType;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "price_list")
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class PriceList {

    @Id
    @GeneratedValue
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProviderType providerType;
    // PHARMACY / LAB / RADIOLOGY / DOCTOR

    @Column(nullable = false)
    private String serviceName;

    private String serviceCode;

    @Column(nullable = false)
    private Double price;

    /**
     * Quantity of medicine in the package
     * For Tablet: number of pills in the package
     * For Syrup: volume in ml
     * For Injection: number of injections in the package
     * For Cream: weight in grams
     * For Drops: volume in ml
     */
    private Integer quantity;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(columnDefinition = "jsonb")
    private String serviceDetails;

    /**
     * Many-to-Many relationship with DoctorSpecializationEntity
     * If this list is empty, the service is available to ALL specializations
     * If it contains specializations, only those specializations can use this service
     */
    @ManyToMany
    @JoinTable(
            name = "price_list_allowed_specializations",
            joinColumns = @JoinColumn(name = "price_list_id"),
            inverseJoinColumns = @JoinColumn(name = "specialization_id")
    )
    private List<DoctorSpecializationEntity> allowedSpecializations;

    /**
     * List of allowed genders for this service (e.g., "MALE", "FEMALE")
     * If null or empty, the service is available to ALL genders
     * If it contains genders, only those genders can use this service
     */
    @ElementCollection
    @CollectionTable(
            name = "price_list_allowed_genders",
            joinColumns = @JoinColumn(name = "price_list_id")
    )
    @Column(name = "gender")
    private List<String> allowedGenders;

    /**
     * Minimum age required to use this service
     * If null, there is no minimum age restriction
     */
    private Integer minAge;

    /**
     * Maximum age allowed to use this service
     * If null, there is no maximum age restriction
     */
    private Integer maxAge;

    private boolean active = true;

    private Instant createdAt;

    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}

