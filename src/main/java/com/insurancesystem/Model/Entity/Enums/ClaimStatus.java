package com.insurancesystem.Model.Entity.Enums;

public enum ClaimStatus {
    PENDING_MEDICAL,   // أول مرة
    AWAITING_COORDINATION_REVIEW,  // موافقة طبية - في انتظار مراجعة المنسق
    APPROVED_FINAL,           // موافقة نهائية (بعد موافقة المنسق)
    REJECTED_FINAL,           // رفض نهائي
    RETURNED_FOR_REVIEW       // أُعيدت من المنسق للطبيب

}
