package com.insurancesystem.Controller;

import com.insurancesystem.Model.Dto.ClientDto;
import com.insurancesystem.Model.Entity.ChronicPatientSchedule;
import com.insurancesystem.Services.ChronicScheduleService;
import com.insurancesystem.Services.MedicalAdminServices;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/medical-admin")
@RequiredArgsConstructor
@Slf4j
public class MedicalAdminController {

    private final MedicalAdminServices medicalAdminService;
    private final ChronicScheduleService scheduleService;



    // ✅ تعطيل / تفعيل حساب مستخدم
    @PatchMapping("/toggle-status/{id}")
    @PreAuthorize("hasRole('MEDICAL_ADMIN')")
    public ResponseEntity<String> toggleUserStatus(@PathVariable UUID id) {
        medicalAdminService.toggleUserStatus(id);
        return ResponseEntity.ok("✅ User status updated successfully");
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('MEDICAL_ADMIN')")
    public ResponseEntity<Map<String, Object>> getFullDashboardStats() {
        return ResponseEntity.ok(medicalAdminService.getFullDashboardStats());
    }

    // ✅ جلب المرضى المزمنين
    @GetMapping("/chronic-patients")
    @PreAuthorize("hasRole('MEDICAL_ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> getChronicPatients() {
        return ResponseEntity.ok(medicalAdminService.getChronicPatients());
    }

    // ✅ إنشاء جدول تلقائي
    @PostMapping("/create-chronic-schedule")
    @PreAuthorize("hasRole('MEDICAL_ADMIN')")
    public ResponseEntity<ChronicPatientSchedule> createChronicSchedule(@RequestBody Map<String, Object> scheduleData) {
        ChronicPatientSchedule schedule = medicalAdminService.createChronicSchedule(scheduleData);
        
        // ✅ إرسال الطلب فوراً عند إنشاء الجدول (الإرسال الأول)
        try {
            log.info("🔄 إرسال الطلب الأول فوراً للجدول: {}", schedule.getId());
            scheduleService.processSingleSchedule(schedule.getId());
            log.info("✅ تم إرسال الطلب الأول بنجاح للجدول: {}", schedule.getId());
        } catch (Exception e) {
            log.error("❌ خطأ في إرسال الطلب الأول للجدول {}: {}", schedule.getId(), e.getMessage(), e);
            // لا نرمي الاستثناء هنا حتى لا يفشل إنشاء الجدول
            // الطلب سيتم إرساله في المرة القادمة من الـ scheduler
        }
        
        return ResponseEntity.ok(schedule);
    }

    // ✅ جلب جميع الجداول
    @GetMapping("/chronic-schedules")
    @PreAuthorize("hasRole('MEDICAL_ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> getAllSchedules() {
        return ResponseEntity.ok(medicalAdminService.getAllSchedules());
    }

    // ✅ حذف جدول
    @DeleteMapping("/delete-chronic-schedule/{scheduleId}")
    @PreAuthorize("hasRole('MEDICAL_ADMIN')")
    public ResponseEntity<String> deleteSchedule(@PathVariable UUID scheduleId) {
        medicalAdminService.deleteSchedule(scheduleId);
        return ResponseEntity.ok("تم حذف الجدول بنجاح");
    }

    // ✅ اختبار معالجة الجداول التلقائية (للاختبار اليدوي)
    @PostMapping("/test-process-schedules")
    @PreAuthorize("hasRole('MEDICAL_ADMIN')")
    public ResponseEntity<Map<String, Object>> testProcessSchedules() {
        try {
            scheduleService.processScheduledItems();
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "تم معالجة الجداول التلقائية بنجاح"
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "خطأ في معالجة الجداول: " + e.getMessage(),
                    "error", e.getClass().getSimpleName()
            ));
        }
    }

}
