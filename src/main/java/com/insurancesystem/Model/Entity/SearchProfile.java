package com.insurancesystem.Model.Entity;

import com.insurancesystem.Model.Entity.Enums.SearchProfileType;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "search_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchProfile {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, length = 150)
    private String name; // اسم العيادة / الصيدلية / المختبر / الدكتور

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SearchProfileType type; // CLINIC / PHARMACY / LAB / DOCTOR

    @Column(length = 255)
    private String address;

    private Double locationLat;  // خط العرض
    private Double locationLng;  // خط الطول

    @Column(length = 100)
    private String contactInfo; // رقم الهاتف أو الإيميل

    @Column(length = 500)
    private String description; // وصف مختصر

    @ManyToOne(optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private Client owner;


    public String getPhone() {
        return owner.getPhone();
    }
}
