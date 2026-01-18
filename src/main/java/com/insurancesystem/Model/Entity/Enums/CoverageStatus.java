package com.insurancesystem.Model.Entity.Enums;

public enum CoverageStatus {
    COVERED,           // مغطى - auto approved
    REQUIRES_APPROVAL, // يحتاج الى موافقة - needs admin approval
    NOT_COVERED        // غير مغطى - not covered by insurance
}
