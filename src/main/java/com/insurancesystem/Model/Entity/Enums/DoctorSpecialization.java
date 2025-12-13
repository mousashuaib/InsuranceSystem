package com.insurancesystem.Model.Entity.Enums;

import lombok.Getter;

@Getter
public enum DoctorSpecialization {

    GENERAL_PRACTICE("General Practice", 30.00),
    CARDIOLOGY("Cardiology", 80.00),
    DERMATOLOGY("Dermatology", 60.00),
    ORTHOPEDICS("Orthopedics", 70.00),
    GYNECOLOGY("Gynecology", 70.00),
    PEDIATRICS("Pediatrics", 50.00),
    NEUROLOGY("Neurology", 90.00),
    OPHTHALMOLOGY("Ophthalmology", 60.00),
    ENT("ENT (Ear, Nose, Throat)", 60.00),
    PSYCHIATRY("Psychiatry", 100.00),
    DENTISTRY("Dentistry", 50.00),
    RADIOLOGY("Radiology", 40.00),
    EMERGENCY_MEDICINE("Emergency Medicine", 40.00);

    private final String displayName;
    private final double consultationPrice;

    DoctorSpecialization(String displayName, double consultationPrice) {
        this.displayName = displayName;
        this.consultationPrice = consultationPrice;
    }
}
