package com.insurancesystem.Model.Entity;

import com.insurancesystem.Model.Entity.Enums.ProfileStatus;
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
    private String contactInfo; // ✅ رقم الهاتف أو الإيميل

    @Column(length = 500)
    private String description; // وصف مختصر

    @ManyToOne(optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private Client owner;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProfileStatus status; // PENDING, APPROVED, REJECTED

    @Column(length = 300)
    private String rejectionReason; // ✅ سبب الرفض (إن وجد)

    // ✅ New Document Fields
    @Column(nullable = true, length = 300)
    private String medicalLicense;

    @Column(nullable = true, length = 300)
    private String universityDegree;

    @Column(nullable = true, length = 300)
    private String idOrPassportCopy;
    @Column(nullable = true, length = 300)

    private String clinicRegistration; // تسجيل العيادة (Optional)


    public String getPhone() {
        return contactInfo;
    }
}
