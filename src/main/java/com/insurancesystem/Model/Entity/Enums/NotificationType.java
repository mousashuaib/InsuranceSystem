package com.insurancesystem.Model.Entity.Enums;

public enum NotificationType {
    MANUAL_MESSAGE, // رسالة يدوية بين المستخدم والمدير
    CLAIM,          // مطالبة
    EMERGENCY,      // طوارئ
    SYSTEM          // إشعار نظام (تسجيل مستخدم جديد مثلاً)
}
