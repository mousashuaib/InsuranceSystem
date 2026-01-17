package com.insurancesystem.Services;

import com.insurancesystem.Exception.NotFoundException;
import com.insurancesystem.Model.Dto.LabRequestDTO;
import com.insurancesystem.Model.Dto.PrescriptionDTO;
import com.insurancesystem.Model.Dto.PrescriptionItemDTO;
import com.insurancesystem.Model.Entity.ChronicPatientSchedule;
import com.insurancesystem.Model.Entity.Client;
import com.insurancesystem.Model.Entity.Enums.ProviderType;
import com.insurancesystem.Model.Entity.Enums.RoleName;
import com.insurancesystem.Model.Entity.PriceList;
import com.insurancesystem.Repository.ClientRepository;
import com.insurancesystem.Repository.PriceListRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChronicScheduleScheduler {

    private final MedicalAdminServices medicalAdminService;
    private final PrescriptionService prescriptionService;
    private final LabRequestService labRequestService;
    private final RadiologyRequestService radiologyRequestService;
    private final HealthcareProviderClaimService claimService;
    private final NotificationService notificationService;
    private final ClientRepository clientRepository;
    private final PriceListRepository priceListRepository;

    // ✅ تشغيل كل يوم في الساعة 8 صباحاً
    // ملاحظة: يمكن تغيير الوقت للاختبار، مثلاً كل دقيقة: "0 * * * * ?"
    @Scheduled(cron = "0 0 8 * * ?")
    @Transactional
    public void processScheduledItems() {
        log.info("🔄 بدء معالجة الجداول التلقائية للمرضى المزمنين...");
        
        try {
            List<ChronicPatientSchedule> schedules = medicalAdminService.getSchedulesDueForSending();
            log.info("📋 تم العثور على {} جدول مستحق للإرسال", schedules.size());

            if (schedules.isEmpty()) {
                log.info("ℹ️ لا توجد جداول مستحقة للإرسال اليوم");
                return;
            }

            for (ChronicPatientSchedule schedule : schedules) {
                try {
                    log.info("🔄 معالجة الجدول: ID={}, Type={}, Patient={}", 
                            schedule.getId(), schedule.getScheduleType(), 
                            schedule.getPatient() != null ? schedule.getPatient().getFullName() : "Unknown");
                    processSchedule(schedule);
                    medicalAdminService.updateLastSentDate(schedule.getId());
                    log.info("✅ تم معالجة الجدول بنجاح: {}", schedule.getId());
                } catch (Exception e) {
                    log.error("❌ خطأ في معالجة الجدول {}: {}", schedule.getId(), e.getMessage(), e);
                    // لا نوقف المعالجة عند حدوث خطأ في جدول واحد
                }
            }
            
            log.info("✅ انتهت معالجة الجداول التلقائية");
        } catch (Exception e) {
            log.error("❌ خطأ عام في معالجة الجداول التلقائية: {}", e.getMessage(), e);
        }
    }

    // ✅ معالجة جدول واحد (للاستدعاء الفوري)
    @Transactional
    public void processSingleSchedule(UUID scheduleId) {
        ChronicPatientSchedule schedule = medicalAdminService.getScheduleById(scheduleId);
        if (schedule == null) {
            log.error("❌ الجدول غير موجود: {}", scheduleId);
            return;
        }
        
        if (!schedule.getIsActive()) {
            log.warn("⚠️ الجدول غير نشط: {}", scheduleId);
            return;
        }
        
        processSchedule(schedule);
        
        // ✅ تحديث تاريخ آخر إرسال وتاريخ الإرسال التالي
        medicalAdminService.updateLastSentDate(schedule.getId());
    }
    
    private void processSchedule(ChronicPatientSchedule schedule) {
        Client patient = schedule.getPatient();
        String scheduleType = schedule.getScheduleType();

        // ✅ الحصول على مدير طبي لإنشاء الطلبات نيابة عنه
        List<Client> medicalAdmins = clientRepository.findByRoles_Name(RoleName.MEDICAL_ADMIN);
        Client medicalAdmin = medicalAdmins.isEmpty() ? null : medicalAdmins.get(0);

        if (medicalAdmin == null) {
            log.error("❌ لم يتم العثور على مدير طبي");
            return;
        }

        switch (scheduleType) {
            case "PRESCRIPTION":
                createPrescription(schedule, patient, medicalAdmin);
                break;
            case "LAB":
                createLabRequest(schedule, patient, medicalAdmin);
                break;
        }

        // ✅ إرسال إشعار للمريض
        String notificationMessage = buildNotificationMessage(schedule);
        notificationService.sendToUser(patient.getId(), notificationMessage);
    }

    private void createPrescription(ChronicPatientSchedule schedule, Client patient, Client medicalAdmin) {
        try {
            log.info("📝 إنشاء وصفة طبية للمريض: {} - الدواء: {}", 
                    patient.getFullName(), schedule.getMedicationName());
            
            // ✅ البحث عن الدواء في PriceList (بحث دقيق أولاً)
            List<PriceList> medications = priceListRepository.findByProviderTypeAndServiceName(
                    ProviderType.PHARMACY, 
                    schedule.getMedicationName()
            );
            
            // ✅ إذا لم يتم العثور، جرب البحث بجميع الأدوية من نفس النوع
            if (medications.isEmpty()) {
                log.warn("⚠️ لم يتم العثور على الدواء بالاسم الدقيق: {}، جاري البحث في جميع الأدوية...", schedule.getMedicationName());
                List<PriceList> allMedications = priceListRepository.findByProviderType(ProviderType.PHARMACY);
                medications = allMedications.stream()
                        .filter(med -> med.getServiceName() != null && 
                                med.getServiceName().equalsIgnoreCase(schedule.getMedicationName()))
                        .collect(java.util.stream.Collectors.toList());
            }
            
            if (medications.isEmpty()) {
                log.error("❌ لم يتم العثور على الدواء: {} في قاعدة البيانات. يرجى التحقق من الاسم.", schedule.getMedicationName());
                throw new NotFoundException("الدواء غير موجود في قائمة الأسعار: " + schedule.getMedicationName());
            }
            
            PriceList medication = medications.get(0); // استخدام أول دواء يطابق الاسم
            log.info("✅ تم العثور على الدواء: {} (ID: {})", medication.getServiceName(), medication.getId());
            
            // ✅ إنشاء PrescriptionItemDTO
            PrescriptionItemDTO itemDTO = PrescriptionItemDTO.builder()
                    .medicineId(medication.getId())
                    .dosage(1) // جرعة افتراضية
                    .timesPerDay(1) // مرة واحدة في اليوم افتراضياً
                    .duration(30) // مدة 30 يوم افتراضياً
                    .build();
            
            List<PrescriptionItemDTO> items = new ArrayList<>();
            items.add(itemDTO);
            
            // ✅ إنشاء PrescriptionDTO
            PrescriptionDTO prescriptionDTO = PrescriptionDTO.builder()
                    .memberId(patient.getId())
                    .memberName(patient.getFullName())
                    .diagnosis("وصفة تلقائية للمريض المزمن - " + schedule.getNotes())
                    .treatment("دواء مزمن: " + schedule.getMedicationName() + 
                              (schedule.getNotes() != null ? " - " + schedule.getNotes() : ""))
                    .items(items)
                    .build();
            
            // ✅ إنشاء الوصفة (يجب أن يكون medicalAdmin مسجل دخول كـ Doctor)
            // ملاحظة: قد نحتاج إلى تعديل PrescriptionService للسماح بإنشاء وصفة من Medical Admin
            // أو يمكننا استخدام SecurityContext لتسجيل دخول medicalAdmin مؤقتاً
            log.info("✅ تم إنشاء PrescriptionDTO للمريض: {}", patient.getFullName());
            
            // ✅ استدعاء PrescriptionService.create
            // ملاحظة: هذا يتطلب أن يكون medicalAdmin له دور DOCTOR أيضاً
            // أو نحتاج إلى تعديل PrescriptionService للسماح بإنشاء وصفة من Medical Admin
            PrescriptionDTO createdPrescription = prescriptionService.createPrescriptionWithDoctor(prescriptionDTO, medicalAdmin);
            
            log.info("✅ تم إنشاء الوصفة الطبية بنجاح للمريض: {} - Prescription ID: {}", 
                    patient.getFullName(), createdPrescription.getId());
            
        } catch (Exception e) {
            log.error("❌ خطأ في إنشاء الوصفة للمريض {} - الدواء {}: {}", 
                    patient.getFullName(), schedule.getMedicationName(), e.getMessage(), e);
            throw e; // إعادة رمي الاستثناء لمعرفة المشكلة
        }
    }

    private void createLabRequest(ChronicPatientSchedule schedule, Client patient, Client medicalAdmin) {
        try {
            log.info("🔬 إنشاء طلب فحص مختبر للمريض: {} - الفحص: {}", 
                    patient.getFullName(), schedule.getLabTestName());
            
            // ✅ البحث عن الفحص في PriceList (بحث دقيق أولاً)
            List<PriceList> labTests = priceListRepository.findByProviderTypeAndServiceName(
                    ProviderType.LAB, 
                    schedule.getLabTestName()
            );
            
            // ✅ إذا لم يتم العثور، جرب البحث بجميع الفحوصات من نفس النوع
            if (labTests.isEmpty()) {
                log.warn("⚠️ لم يتم العثور على الفحص بالاسم الدقيق: {}، جاري البحث في جميع الفحوصات...", schedule.getLabTestName());
                List<PriceList> allLabTests = priceListRepository.findByProviderType(ProviderType.LAB);
                labTests = allLabTests.stream()
                        .filter(test -> test.getServiceName() != null && 
                                test.getServiceName().equalsIgnoreCase(schedule.getLabTestName()))
                        .collect(java.util.stream.Collectors.toList());
            }
            
            if (labTests.isEmpty()) {
                log.error("❌ لم يتم العثور على الفحص: {} في قاعدة البيانات. يرجى التحقق من الاسم.", schedule.getLabTestName());
                throw new NotFoundException("الفحص غير موجود في قائمة الأسعار: " + schedule.getLabTestName());
            }
            
            PriceList labTest = labTests.get(0); // استخدام أول فحص يطابق الاسم
            log.info("✅ تم العثور على الفحص: {} (ID: {})", labTest.getServiceName(), labTest.getId());
            
            // ✅ إنشاء LabRequestDTO
            LabRequestDTO labRequestDTO = LabRequestDTO.builder()
                    .memberId(patient.getId())
                    .memberName(patient.getFullName())
                    .testId(labTest.getId())
                    .testName(labTest.getServiceName())
                    .diagnosis("فحص تلقائي للمريض المزمن - " + schedule.getNotes())
                    .treatment("فحص دوري: " + schedule.getLabTestName() + 
                              (schedule.getNotes() != null ? " - " + schedule.getNotes() : ""))
                    .notes("طلب تلقائي من جدولة المرضى المزمنين" + 
                          (schedule.getNotes() != null ? " - " + schedule.getNotes() : ""))
                    .build();
            
            log.info("✅ تم إنشاء LabRequestDTO للمريض: {}", patient.getFullName());
            
            // ✅ استدعاء LabRequestService.createLabRequestWithDoctor
            LabRequestDTO createdLabRequest = labRequestService.createLabRequestWithDoctor(labRequestDTO, medicalAdmin);
            
            log.info("✅ تم إنشاء طلب الفحص بنجاح للمريض: {} - Lab Request ID: {}", 
                    patient.getFullName(), createdLabRequest.getId());
            
        } catch (Exception e) {
            log.error("❌ خطأ في إنشاء طلب المختبر للمريض {} - الفحص {}: {}", 
                    patient.getFullName(), schedule.getLabTestName(), e.getMessage(), e);
            throw e; // إعادة رمي الاستثناء لمعرفة المشكلة
        }
    }

    private String buildNotificationMessage(ChronicPatientSchedule schedule) {
        StringBuilder message = new StringBuilder("✅ Automatic schedule sent: ");
        
        switch (schedule.getScheduleType()) {
            case "PRESCRIPTION":
                message.append("Prescription for medication: ").append(schedule.getMedicationName());
                break;
            case "LAB":
                message.append("Lab test request: ").append(schedule.getLabTestName());
                break;
        }
        
        return message.toString();
    }
}

