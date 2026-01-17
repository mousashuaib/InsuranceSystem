package com.insurancesystem.Services;

import com.insurancesystem.Exception.NotFoundException;
import com.insurancesystem.Model.Entity.Client;
import com.insurancesystem.Model.Entity.ChronicPatientSchedule;
import com.insurancesystem.Model.Entity.Enums.MemberStatus;
import com.insurancesystem.Model.Entity.Enums.RoleName;
import com.insurancesystem.Model.MapStruct.ClientMapper;
import com.insurancesystem.Repository.ClientRepository;
import com.insurancesystem.Repository.ChronicPatientScheduleRepository;
import com.insurancesystem.Repository.HealthcareProviderClaimRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class MedicalAdminServices {
    private final ClientRepository clientRepository;
    private final ClientMapper clientMapper;
    private final HealthcareProviderClaimRepository claimRepository;
    private final ChronicPatientScheduleRepository scheduleRepository;

    // ✅ تفعيل / تعطيل مستخدم
    public void toggleUserStatus(UUID id) {
        Client user = clientRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
        if (user.getStatus() == MemberStatus.ACTIVE)
            user.setStatus(MemberStatus.DEACTIVATED);
        else
            user.setStatus(MemberStatus.ACTIVE);
        clientRepository.save(user);
    }

    private boolean hasRole(Client client, RoleName role) {
        return client.getRoles().stream().anyMatch(r -> r.getName() == role);
    }



    public Map<String, Object> getFullDashboardStats() {
        Map<String, Object> result = new LinkedHashMap<>();

        // 🔹 كل المستخدمين
        List<Client> all = clientRepository.findAll();

        // 🔹 حساب الأعداد حسب الدور
        long doctors = all.stream().filter(c -> hasRole(c, RoleName.DOCTOR)).count();
        long labs = all.stream().filter(c -> hasRole(c, RoleName.LAB_TECH)).count();
        long radiologists = all.stream().filter(c -> hasRole(c, RoleName.RADIOLOGIST)).count();
        long pharmacists = all.stream().filter(c -> hasRole(c, RoleName.PHARMACIST)).count();
        long providersCount = doctors + labs + radiologists + pharmacists;

        // ✅ أكثر طبيب إرسالاً للمطالبات
        List<Object[]> topDoctors = claimRepository.findTopDoctorsByClaims();
        if (topDoctors.isEmpty()) {
            result.put("topDoctorName", "لا يوجد بيانات");
            result.put("claimCount", 0L);
        } else {
            Object[] row = topDoctors.get(0);
            result.put("topDoctorName", (String) row[0]);
            result.put("claimCount", ((Long) row[1]));
        }

        // 🧮 دمج كل الإحصاءات في كائن واحد
        result.put("doctors", doctors);
        result.put("labs", labs);
        result.put("radiologists", radiologists);
        result.put("pharmacists", pharmacists);
        result.put("total", providersCount);
        result.put("providersCount", providersCount);

        return result;
    }

    // ✅ جلب المرضى المزمنين مع جميع المعلومات والوثائق
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getChronicPatients() {
        // ✅ جلب جميع العملاء مع تحميل chronicDocumentPaths
        List<Client> allClients = clientRepository.findAll();
        
        return allClients.stream()
                .filter(client -> hasRole(client, RoleName.INSURANCE_CLIENT))
                .filter(client -> client.getChronicDiseases() != null && !client.getChronicDiseases().isEmpty())
                .map(client -> {
                    // ✅ التأكد من تحميل chronicDocumentPaths (EAGER fetch)
                    List<String> documentPaths = client.getChronicDocumentPaths();
                    if (documentPaths == null) {
                        documentPaths = new ArrayList<>();
                    }
                    
                    // ✅ تنسيق تاريخ الميلاد
                    String dateOfBirthStr = "";
                    if (client.getDateOfBirth() != null) {
                        dateOfBirthStr = client.getDateOfBirth().toString();
                    }
                    
                    Map<String, Object> patientData = new HashMap<>();
                    patientData.put("id", client.getId());
                    patientData.put("fullName", client.getFullName() != null ? client.getFullName() : "");
                    patientData.put("employeeId", client.getEmployeeId() != null ? client.getEmployeeId() : "");
                    patientData.put("email", client.getEmail() != null ? client.getEmail() : "");
                    patientData.put("phone", client.getPhone() != null ? client.getPhone() : "");
                    patientData.put("nationalId", client.getNationalId() != null ? client.getNationalId() : "");
                    patientData.put("department", client.getDepartment() != null ? client.getDepartment() : "");
                    patientData.put("faculty", client.getFaculty() != null ? client.getFaculty() : "");
                    patientData.put("gender", client.getGender() != null ? client.getGender() : "");
                    patientData.put("dateOfBirth", dateOfBirthStr);
                    patientData.put("age", client.getAge() != null ? client.getAge() : null);
                    patientData.put("chronicDiseases", client.getChronicDiseases().stream()
                            .map(Enum::name)
                            .collect(Collectors.toList()));
                    // ✅ إضافة وثائق الأمراض المزمنة
                    patientData.put("chronicDocumentPaths", documentPaths);
                    return patientData;
                })
                .collect(Collectors.toList());
    }

    // ✅ إنشاء جدول تلقائي
    public ChronicPatientSchedule createChronicSchedule(Map<String, Object> scheduleData) {
        UUID patientId = UUID.fromString((String) scheduleData.get("patientId"));
        Client patient = clientRepository.findById(patientId)
                .orElseThrow(() -> new NotFoundException("Patient not found"));

        ChronicPatientSchedule schedule = ChronicPatientSchedule.builder()
                .patient(patient)
                .scheduleType((String) scheduleData.get("scheduleType"))
                .intervalMonths((Integer) scheduleData.get("intervalMonths"))
                .notes((String) scheduleData.get("notes"))
                .isActive(true)
                .build();

        String scheduleType = (String) scheduleData.get("scheduleType");
        if ("PRESCRIPTION".equals(scheduleType)) {
            schedule.setMedicationName((String) scheduleData.get("medicationName"));
            // ✅ حفظ كمية الدواء
            if (scheduleData.get("medicationQuantity") != null) {
                schedule.setMedicationQuantity((Integer) scheduleData.get("medicationQuantity"));
            } else {
                schedule.setMedicationQuantity(1); // قيمة افتراضية
            }
        } else if ("LAB".equals(scheduleType)) {
            schedule.setLabTestName((String) scheduleData.get("labTestName"));
        }

        // ✅ تعيين nextSendDate للفترة التالية (بعد شهر/شهرين...)
        schedule.setNextSendDate(LocalDate.now().plusMonths(schedule.getIntervalMonths()));
        
        // ✅ حفظ الجدول أولاً
        ChronicPatientSchedule savedSchedule = scheduleRepository.save(schedule);
        
        // ✅ إرسال الطلب فوراً عند إنشاء الجدول (الإرسال الأول)
        // سيتم استدعاء processSchedule مباشرة لإرسال الطلب الآن
        // ثم بعد ذلك، الـ scheduler سيرسل الطلبات تلقائياً كل فترة
        
        return savedSchedule;
    }

    // ✅ جلب جميع الجداول
    public List<Map<String, Object>> getAllSchedules() {
        return scheduleRepository.findByIsActiveTrueOrderByCreatedAtDesc().stream()
                .map(schedule -> {
                    Map<String, Object> scheduleData = new HashMap<>();
                    scheduleData.put("id", schedule.getId());
                    scheduleData.put("patientName", schedule.getPatient().getFullName());
                    scheduleData.put("scheduleType", schedule.getScheduleType());
                    scheduleData.put("medicationName", schedule.getMedicationName());
                    scheduleData.put("labTestName", schedule.getLabTestName());
                    scheduleData.put("radiologyTestName", schedule.getRadiologyTestName());
                    scheduleData.put("description", schedule.getDescription());
                    scheduleData.put("amount", schedule.getAmount());
                    scheduleData.put("intervalMonths", schedule.getIntervalMonths());
                    scheduleData.put("lastSentAt", schedule.getLastSentAt());
                    scheduleData.put("nextSendDate", schedule.getNextSendDate());
                    return scheduleData;
                })
                .collect(Collectors.toList());
    }

    // ✅ حذف جدول
    public void deleteSchedule(UUID scheduleId) {
        ChronicPatientSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new NotFoundException("Schedule not found"));
        schedule.setIsActive(false);
        scheduleRepository.save(schedule);
    }

    // ✅ جلب الجداول المستحقة للإرسال (للاستخدام في Scheduler)
    public List<ChronicPatientSchedule> getSchedulesDueForSending() {
        return scheduleRepository.findSchedulesDueForSending(LocalDate.now());
    }

    // ✅ تحديث تاريخ آخر إرسال
    public void updateLastSentDate(UUID scheduleId) {
        ChronicPatientSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new NotFoundException("Schedule not found"));
        schedule.setLastSentAt(Instant.now());
        schedule.setNextSendDate(LocalDate.now().plusMonths(schedule.getIntervalMonths()));
        scheduleRepository.save(schedule);
    }
    
    // ✅ الحصول على جدول بالمعرف
    public ChronicPatientSchedule getScheduleById(UUID scheduleId) {
        return scheduleRepository.findById(scheduleId).orElse(null);
    }

}
