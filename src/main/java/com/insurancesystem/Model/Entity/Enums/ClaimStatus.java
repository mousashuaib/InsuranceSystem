package com.insurancesystem.Model.Entity.Enums;

public enum ClaimStatus {
<<<<<<< HEAD
    PENDING, // بانتظار المراجعة الطبية
    PENDING_MEDICAL, // New: Pending medical review
    APPROVED_BY_MEDICAL, // تمت الموافقة الطبية
    APPROVED_MEDICAL, // New: Approved by medical (alias for APPROVED_BY_MEDICAL)
    REJECTED_BY_MEDICAL, // تم الرفض الطبي
    REJECTED_MEDICAL, // New: Rejected by medical (alias for REJECTED_BY_MEDICAL)
    AWAITING_ADMIN_REVIEW, // بانتظار المراجعة الإدارية
    AWAITING_COORDINATION_REVIEW, // New: Awaiting coordination review
    PENDING_COORDINATION, // New: Pending coordination review
    APPROVED, // تمت الموافقة النهائية
    APPROVED_FINAL, // New: Final approval (alias for APPROVED)
    REJECTED, // الرفض الإداري النهائي
    REJECTED_FINAL, // New: Final rejection (alias for REJECTED)
    RETURNED_FOR_REVIEW, // New: Returned from coordinator for medical re-review
    RETURNED_TO_PROVIDER, // New: Returned to provider
    PAYMENT_PENDING, // New: Payment pending
    PAID // New: Paid
=======
    PENDING_MEDICAL,   // أول مرة
    AWAITING_COORDINATION_REVIEW,  // موافقة طبية - في انتظار مراجعة المنسق
    APPROVED_FINAL,           // موافقة نهائية (بعد موافقة المنسق)
    REJECTED_FINAL,           // رفض نهائي
    RETURNED_FOR_REVIEW       // أُعيدت من المنسق للطبيب

>>>>>>> 59fc73de7f549007a5658aab4146b5707a8a4bd8
}
