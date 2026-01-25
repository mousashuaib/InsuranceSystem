package com.insurancesystem.Model.Entity.Enums;

public enum ClaimStatus {
    // Legacy statuses (for backward compatibility with existing data)
    PENDING,                  // Legacy: Initial submission
    APPROVED,                 // Legacy: Final approval
    REJECTED,                 // Legacy: Final rejection
    APPROVED_BY_MEDICAL,      // Legacy: Approved by medical admin

    // New workflow statuses
    PENDING_MEDICAL,          // New claims awaiting medical review
    PENDING_COORDINATION,     // Alias for awaiting coordination (frontend compatibility)
    AWAITING_COORDINATION_REVIEW,  // Medical approved - awaiting coordination review
    APPROVED_MEDICAL,         // Approved by medical admin (new workflow)
    REJECTED_MEDICAL,         // Rejected by medical admin (new workflow)
    APPROVED_FINAL,           // Final approval (after coordination approval)
    REJECTED_FINAL,           // Final rejection
    RETURNED_FOR_REVIEW,      // Returned from coordinator to medical admin
    RETURNED_TO_PROVIDER,     // Returned to provider for corrections
    PAYMENT_PENDING,          // Approved - awaiting payment
    PAID                      // Payment completed
}
