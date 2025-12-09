package com.insurancesystem.Model.Entity.Enums;

public enum ClaimStatus {
    APPROVED_BY_MEDICAL, // تمت الموافقة الطبية
    REJECTED_BY_MEDICAL, // تم الرفض الطبي
    AWAITING_ADMIN_REVIEW, // بانتظار المراجعة الإدارية
    PENDING_MEDICAL,

    PENDING, // بانتظار المراجعة الطبية
    APPROVED, // تمت الموافقة النهائية
    REJECTED ,// الرفض الإداري النهائي
    }
