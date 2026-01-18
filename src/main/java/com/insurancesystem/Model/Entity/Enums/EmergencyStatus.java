package com.insurancesystem.Model.Entity.Enums;

public enum EmergencyStatus {
    APPROVED_BY_MEDICAL, // تمت الموافقة الطبية
    REJECTED_BY_MEDICAL,
    PENDING_MEDICAL,
    // Legacy values for backward compatibility
    PENDING,
    APPROVED,
    REJECTED
}
