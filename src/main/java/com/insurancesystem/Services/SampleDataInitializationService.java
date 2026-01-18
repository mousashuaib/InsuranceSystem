package com.insurancesystem.Services;

import com.insurancesystem.Model.Entity.*;
import com.insurancesystem.Model.Entity.Enums.CoverageStatus;
import com.insurancesystem.Model.Entity.Enums.RoleName;
import com.insurancesystem.Repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/**
 * Service to initialize sample data for Coverage Management feature.
 * This runs on application startup if the tables are empty.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SampleDataInitializationService implements CommandLineRunner {

    private final MedicinePriceRepository medicinePriceRepository;
    private final MedicalTestRepository medicalTestRepository;
    private final MedicalDiagnosisRepository medicalDiagnosisRepository;
    private final DoctorProcedureRepository doctorProcedureRepository;
    private final ClientRepository clientRepository;
    private final DoctorMedicineAssignmentRepository doctorMedicineAssignmentRepository;

    @Override
    @Transactional
    public void run(String... args) {
        initializeMedicines();
        initializeLabTests();
        initializeRadiologyTests();
        initializeDoctorProcedures();
        initializeDiagnoses();
        initializeDoctorMedicineAssignments();
        log.info("✅ Sample data initialization completed");
    }

    private void initializeMedicines() {
        if (medicinePriceRepository.count() > 0) {
            log.info("Medicines already exist, skipping initialization");
            return;
        }

        log.info("Initializing sample medicines...");

        List<MedicinePrice> medicines = Arrays.asList(
            // Covered medicines
            createMedicine("Paracetamol 500mg", "Paracetamol", "Tablet", "500mg", new BigDecimal("5.00"), CoverageStatus.COVERED),
            createMedicine("Amoxicillin 500mg", "Amoxicillin Trihydrate", "Capsule", "500mg", new BigDecimal("15.00"), CoverageStatus.COVERED),
            createMedicine("Ibuprofen 400mg", "Ibuprofen", "Tablet", "400mg", new BigDecimal("8.00"), CoverageStatus.COVERED),
            createMedicine("Omeprazole 20mg", "Omeprazole", "Capsule", "20mg", new BigDecimal("12.00"), CoverageStatus.COVERED),
            createMedicine("Metformin 500mg", "Metformin HCl", "Tablet", "500mg", new BigDecimal("10.00"), CoverageStatus.COVERED),
            createMedicine("Amlodipine 5mg", "Amlodipine Besylate", "Tablet", "5mg", new BigDecimal("15.00"), CoverageStatus.COVERED),
            createMedicine("Azithromycin 250mg", "Azithromycin", "Tablet", "250mg", new BigDecimal("25.00"), CoverageStatus.COVERED),
            createMedicine("Cetirizine 10mg", "Cetirizine HCl", "Tablet", "10mg", new BigDecimal("6.00"), CoverageStatus.COVERED),
            createMedicine("Ranitidine 150mg", "Ranitidine HCl", "Tablet", "150mg", new BigDecimal("8.00"), CoverageStatus.COVERED),
            createMedicine("Metoprolol 50mg", "Metoprolol Tartrate", "Tablet", "50mg", new BigDecimal("12.00"), CoverageStatus.COVERED),

            // Requires approval medicines
            createMedicine("Atorvastatin 20mg", "Atorvastatin Calcium", "Tablet", "20mg", new BigDecimal("35.00"), CoverageStatus.REQUIRES_APPROVAL),
            createMedicine("Clopidogrel 75mg", "Clopidogrel Bisulfate", "Tablet", "75mg", new BigDecimal("45.00"), CoverageStatus.REQUIRES_APPROVAL),
            createMedicine("Pregabalin 75mg", "Pregabalin", "Capsule", "75mg", new BigDecimal("55.00"), CoverageStatus.REQUIRES_APPROVAL),
            createMedicine("Duloxetine 30mg", "Duloxetine HCl", "Capsule", "30mg", new BigDecimal("65.00"), CoverageStatus.REQUIRES_APPROVAL),
            createMedicine("Rosuvastatin 10mg", "Rosuvastatin Calcium", "Tablet", "10mg", new BigDecimal("40.00"), CoverageStatus.REQUIRES_APPROVAL),

            // Not covered medicines
            createMedicine("Sildenafil 50mg", "Sildenafil Citrate", "Tablet", "50mg", new BigDecimal("80.00"), CoverageStatus.NOT_COVERED),
            createMedicine("Minoxidil 5%", "Minoxidil", "Solution", "60ml", new BigDecimal("120.00"), CoverageStatus.NOT_COVERED),
            createMedicine("Finasteride 1mg", "Finasteride", "Tablet", "1mg", new BigDecimal("150.00"), CoverageStatus.NOT_COVERED)
        );

        medicinePriceRepository.saveAll(medicines);
        log.info("✅ Initialized {} medicines", medicines.size());
    }

    private MedicinePrice createMedicine(String drugName, String genericName, String type, String unit, BigDecimal price, CoverageStatus status) {
        return MedicinePrice.builder()
                .drugName(drugName)
                .genericName(genericName)
                .type(type)
                .unit(unit)
                .price(price)
                .coverageStatus(status)
                .active(true)
                .build();
    }

    private void initializeLabTests() {
        if (medicalTestRepository.findByCategoryAndActiveTrue("LAB").size() > 0) {
            log.info("Lab tests already exist, skipping initialization");
            return;
        }

        log.info("Initializing sample lab tests...");

        List<MedicalTest> labTests = Arrays.asList(
            // Covered lab tests
            createTest("CBC (Complete Blood Count)", "LAB", new BigDecimal("50.00"), CoverageStatus.COVERED),
            createTest("Fasting Blood Sugar", "LAB", new BigDecimal("30.00"), CoverageStatus.COVERED),
            createTest("Lipid Profile", "LAB", new BigDecimal("80.00"), CoverageStatus.COVERED),
            createTest("Liver Function Test (LFT)", "LAB", new BigDecimal("90.00"), CoverageStatus.COVERED),
            createTest("Kidney Function Test (KFT)", "LAB", new BigDecimal("85.00"), CoverageStatus.COVERED),
            createTest("Thyroid Panel (TSH/T3/T4)", "LAB", new BigDecimal("120.00"), CoverageStatus.COVERED),
            createTest("Urinalysis", "LAB", new BigDecimal("25.00"), CoverageStatus.COVERED),
            createTest("HbA1c (Glycated Hemoglobin)", "LAB", new BigDecimal("70.00"), CoverageStatus.COVERED),
            createTest("Electrolytes Panel", "LAB", new BigDecimal("60.00"), CoverageStatus.COVERED),
            createTest("ESR (Erythrocyte Sedimentation Rate)", "LAB", new BigDecimal("20.00"), CoverageStatus.COVERED),

            // Requires approval lab tests
            createTest("Vitamin D", "LAB", new BigDecimal("120.00"), CoverageStatus.REQUIRES_APPROVAL),
            createTest("Vitamin B12", "LAB", new BigDecimal("100.00"), CoverageStatus.REQUIRES_APPROVAL),
            createTest("Iron Studies", "LAB", new BigDecimal("110.00"), CoverageStatus.REQUIRES_APPROVAL),
            createTest("Hormone Panel", "LAB", new BigDecimal("200.00"), CoverageStatus.REQUIRES_APPROVAL),
            createTest("Tumor Markers", "LAB", new BigDecimal("300.00"), CoverageStatus.REQUIRES_APPROVAL),

            // Not covered lab tests
            createTest("Genetic Testing", "LAB", new BigDecimal("500.00"), CoverageStatus.NOT_COVERED),
            createTest("Allergy Panel (Full)", "LAB", new BigDecimal("400.00"), CoverageStatus.NOT_COVERED)
        );

        medicalTestRepository.saveAll(labTests);
        log.info("✅ Initialized {} lab tests", labTests.size());
    }

    private void initializeRadiologyTests() {
        if (medicalTestRepository.findByCategoryAndActiveTrue("RADIOLOGY").size() > 0) {
            log.info("Radiology tests already exist, skipping initialization");
            return;
        }

        log.info("Initializing sample radiology tests...");

        List<MedicalTest> radiologyTests = Arrays.asList(
            // Covered radiology
            createTest("Chest X-Ray", "RADIOLOGY", new BigDecimal("80.00"), CoverageStatus.COVERED),
            createTest("X-Ray Spine (Cervical)", "RADIOLOGY", new BigDecimal("100.00"), CoverageStatus.COVERED),
            createTest("X-Ray Spine (Lumbar)", "RADIOLOGY", new BigDecimal("100.00"), CoverageStatus.COVERED),
            createTest("X-Ray Extremities", "RADIOLOGY", new BigDecimal("70.00"), CoverageStatus.COVERED),
            createTest("Ultrasound Abdomen", "RADIOLOGY", new BigDecimal("150.00"), CoverageStatus.COVERED),
            createTest("Ultrasound Pelvis", "RADIOLOGY", new BigDecimal("150.00"), CoverageStatus.COVERED),
            createTest("Echocardiogram", "RADIOLOGY", new BigDecimal("200.00"), CoverageStatus.COVERED),
            createTest("ECG (Electrocardiogram)", "RADIOLOGY", new BigDecimal("50.00"), CoverageStatus.COVERED),

            // Requires approval radiology
            createTest("CT Scan Head", "RADIOLOGY", new BigDecimal("400.00"), CoverageStatus.REQUIRES_APPROVAL),
            createTest("CT Scan Chest", "RADIOLOGY", new BigDecimal("450.00"), CoverageStatus.REQUIRES_APPROVAL),
            createTest("CT Scan Abdomen", "RADIOLOGY", new BigDecimal("500.00"), CoverageStatus.REQUIRES_APPROVAL),
            createTest("MRI Brain", "RADIOLOGY", new BigDecimal("800.00"), CoverageStatus.REQUIRES_APPROVAL),
            createTest("MRI Spine", "RADIOLOGY", new BigDecimal("850.00"), CoverageStatus.REQUIRES_APPROVAL),
            createTest("MRI Knee", "RADIOLOGY", new BigDecimal("700.00"), CoverageStatus.REQUIRES_APPROVAL),

            // Not covered radiology
            createTest("PET Scan", "RADIOLOGY", new BigDecimal("2000.00"), CoverageStatus.NOT_COVERED),
            createTest("Whole Body MRI", "RADIOLOGY", new BigDecimal("2500.00"), CoverageStatus.NOT_COVERED)
        );

        medicalTestRepository.saveAll(radiologyTests);
        log.info("✅ Initialized {} radiology tests", radiologyTests.size());
    }

    private MedicalTest createTest(String testName, String category, BigDecimal price, CoverageStatus status) {
        return MedicalTest.builder()
                .testName(testName)
                .category(category)
                .price(price)
                .coverageStatus(status)
                .active(true)
                .build();
    }

    private void initializeDoctorProcedures() {
        if (doctorProcedureRepository.count() > 0) {
            log.info("Doctor procedures already exist, skipping initialization");
            return;
        }

        log.info("Initializing sample doctor procedures...");

        List<DoctorProcedure> procedures = Arrays.asList(
            // General procedures - Covered
            createProcedure("ECG Normal", "GENERAL", new BigDecimal("30.00"), null, CoverageStatus.COVERED),
            createProcedure("ECG with Report", "GENERAL", new BigDecimal("70.00"), null, CoverageStatus.COVERED),
            createProcedure("Injection (I.M)", "GENERAL", new BigDecimal("10.00"), null, CoverageStatus.COVERED),
            createProcedure("I.V Line Set + Canula", "GENERAL", new BigDecimal("20.00"), null, CoverageStatus.COVERED),
            createProcedure("Dressing (Small)", "GENERAL", new BigDecimal("30.00"), new BigDecimal("45.00"), CoverageStatus.COVERED),
            createProcedure("Dressing (Large)", "GENERAL", new BigDecimal("50.00"), new BigDecimal("70.00"), CoverageStatus.COVERED),

            // Cardiology procedures
            createProcedure("Echo Cardiogram", "CARDIOLOGY", new BigDecimal("200.00"), null, CoverageStatus.COVERED),
            createProcedure("Stress Test", "CARDIOLOGY", new BigDecimal("175.00"), null, CoverageStatus.COVERED),
            createProcedure("Holter Monitor 24hr", "CARDIOLOGY", new BigDecimal("175.00"), null, CoverageStatus.COVERED),
            createProcedure("ABPM", "CARDIOLOGY", new BigDecimal("300.00"), null, CoverageStatus.REQUIRES_APPROVAL),

            // Surgery procedures
            createProcedure("Minor Surgery", "SURGERY", new BigDecimal("150.00"), new BigDecimal("500.00"), CoverageStatus.REQUIRES_APPROVAL),
            createProcedure("Skin Biopsy", "SURGERY", new BigDecimal("500.00"), null, CoverageStatus.REQUIRES_APPROVAL),
            createProcedure("Lipoma Excision", "SURGERY", new BigDecimal("575.00"), null, CoverageStatus.REQUIRES_APPROVAL),
            createProcedure("Ingrown Toenail Removal", "SURGERY", new BigDecimal("600.00"), null, CoverageStatus.COVERED),
            createProcedure("Sebaceous Cyst Excision", "SURGERY", new BigDecimal("575.00"), null, CoverageStatus.REQUIRES_APPROVAL),
            createProcedure("Circumcision", "SURGERY", new BigDecimal("300.00"), null, CoverageStatus.COVERED),

            // ENT procedures
            createProcedure("Nasal Cautery", "ENT", new BigDecimal("150.00"), null, CoverageStatus.COVERED),
            createProcedure("Ear Irrigation", "ENT", new BigDecimal("80.00"), null, CoverageStatus.COVERED),
            createProcedure("Audiogram", "ENT", new BigDecimal("150.00"), null, CoverageStatus.COVERED),
            createProcedure("Laryngoscopy", "ENT", new BigDecimal("200.00"), null, CoverageStatus.REQUIRES_APPROVAL),
            createProcedure("Foreign Body Removal", "ENT", new BigDecimal("250.00"), null, CoverageStatus.COVERED),

            // Orthopedic procedures
            createProcedure("Cast (Below Elbow)", "ORTHOPEDIC", new BigDecimal("235.00"), null, CoverageStatus.COVERED),
            createProcedure("Cast (Above Elbow)", "ORTHOPEDIC", new BigDecimal("250.00"), null, CoverageStatus.COVERED),
            createProcedure("Cast (Below Knee)", "ORTHOPEDIC", new BigDecimal("235.00"), null, CoverageStatus.COVERED),
            createProcedure("Cast (Above Knee)", "ORTHOPEDIC", new BigDecimal("300.00"), null, CoverageStatus.COVERED),
            createProcedure("Joint Injection", "ORTHOPEDIC", new BigDecimal("100.00"), null, CoverageStatus.COVERED),
            createProcedure("Arthrocentesis", "ORTHOPEDIC", new BigDecimal("130.00"), null, CoverageStatus.REQUIRES_APPROVAL),

            // OB-GYN procedures
            createProcedure("Pap Smear", "OBGYN", new BigDecimal("100.00"), null, CoverageStatus.COVERED),
            createProcedure("IUD Insertion", "OBGYN", new BigDecimal("300.00"), null, CoverageStatus.COVERED),
            createProcedure("IUD Removal", "OBGYN", new BigDecimal("100.00"), null, CoverageStatus.COVERED),
            createProcedure("Cervical Cautery", "OBGYN", new BigDecimal("250.00"), null, CoverageStatus.REQUIRES_APPROVAL),
            createProcedure("NST (Non-Stress Test)", "OBGYN", new BigDecimal("50.00"), null, CoverageStatus.COVERED)
        );

        doctorProcedureRepository.saveAll(procedures);
        log.info("✅ Initialized {} doctor procedures", procedures.size());
    }

    private DoctorProcedure createProcedure(String name, String category, BigDecimal price, BigDecimal maxPrice, CoverageStatus status) {
        return DoctorProcedure.builder()
                .procedureName(name)
                .category(category)
                .price(price)
                .maxPrice(maxPrice)
                .coverageStatus(status)
                .active(true)
                .build();
    }

    private void initializeDiagnoses() {
        if (medicalDiagnosisRepository.count() > 0) {
            log.info("Diagnoses already exist, skipping initialization");
            return;
        }

        log.info("Initializing sample diagnoses...");

        List<MedicalDiagnosis> diagnoses = Arrays.asList(
            createDiagnosis("Hypertension", "ارتفاع ضغط الدم"),
            createDiagnosis("Diabetes Mellitus Type 2", "السكري النوع الثاني"),
            createDiagnosis("Diabetes Mellitus Type 1", "السكري النوع الأول"),
            createDiagnosis("Asthma", "الربو"),
            createDiagnosis("Chronic Obstructive Pulmonary Disease", "مرض الانسداد الرئوي المزمن"),
            createDiagnosis("Coronary Artery Disease", "مرض الشريان التاجي"),
            createDiagnosis("Heart Failure", "فشل القلب"),
            createDiagnosis("Atrial Fibrillation", "الرجفان الأذيني"),
            createDiagnosis("Chronic Kidney Disease", "مرض الكلى المزمن"),
            createDiagnosis("Gastritis", "التهاب المعدة"),
            createDiagnosis("Gastroesophageal Reflux Disease", "مرض الارتجاع المعدي المريئي"),
            createDiagnosis("Peptic Ulcer Disease", "مرض القرحة الهضمية"),
            createDiagnosis("Migraine", "الصداع النصفي"),
            createDiagnosis("Tension Headache", "صداع التوتر"),
            createDiagnosis("Depression", "الاكتئاب"),
            createDiagnosis("Anxiety Disorder", "اضطراب القلق"),
            createDiagnosis("Hypothyroidism", "قصور الغدة الدرقية"),
            createDiagnosis("Hyperthyroidism", "فرط نشاط الغدة الدرقية"),
            createDiagnosis("Osteoporosis", "هشاشة العظام"),
            createDiagnosis("Osteoarthritis", "التهاب المفاصل التنكسي"),
            createDiagnosis("Rheumatoid Arthritis", "التهاب المفاصل الروماتويدي"),
            createDiagnosis("Anemia", "فقر الدم"),
            createDiagnosis("Iron Deficiency Anemia", "فقر الدم بعوز الحديد"),
            createDiagnosis("Vitamin D Deficiency", "نقص فيتامين د"),
            createDiagnosis("Upper Respiratory Tract Infection", "عدوى الجهاز التنفسي العلوي"),
            createDiagnosis("Urinary Tract Infection", "عدوى المسالك البولية"),
            createDiagnosis("Pneumonia", "الالتهاب الرئوي"),
            createDiagnosis("Bronchitis", "التهاب الشعب الهوائية"),
            createDiagnosis("Sinusitis", "التهاب الجيوب الأنفية"),
            createDiagnosis("Allergic Rhinitis", "التهاب الأنف التحسسي"),
            createDiagnosis("Eczema", "الإكزيما"),
            createDiagnosis("Psoriasis", "الصدفية"),
            createDiagnosis("Acne Vulgaris", "حب الشباب"),
            createDiagnosis("Lower Back Pain", "آلام أسفل الظهر"),
            createDiagnosis("Cervical Spondylosis", "داء الفقار الرقبي"),
            createDiagnosis("Sciatica", "عرق النسا")
        );

        medicalDiagnosisRepository.saveAll(diagnoses);
        log.info("✅ Initialized {} diagnoses", diagnoses.size());
    }

    private MedicalDiagnosis createDiagnosis(String englishName, String arabicName) {
        return MedicalDiagnosis.builder()
                .englishName(englishName)
                .arabicName(arabicName)
                .active(true)
                .build();
    }

    private void initializeDoctorMedicineAssignments() {
        if (doctorMedicineAssignmentRepository.count() > 0) {
            log.info("Doctor medicine assignments already exist, skipping initialization");
            return;
        }

        // Get all doctors
        List<Client> doctors = clientRepository.findAll().stream()
                .filter(c -> c.hasRole(RoleName.DOCTOR))
                .toList();

        if (doctors.isEmpty()) {
            log.info("No doctors found, skipping doctor medicine assignments initialization");
            return;
        }

        // Get all active medicines
        List<MedicinePrice> medicines = medicinePriceRepository.findByActiveTrue();
        if (medicines.isEmpty()) {
            log.info("No medicines found, skipping doctor medicine assignments initialization");
            return;
        }

        log.info("Initializing doctor medicine assignments for {} doctors and {} medicines...",
                doctors.size(), medicines.size());

        // Get the first manager as the assigner (or use system)
        Client assigner = clientRepository.findAll().stream()
                .filter(c -> c.hasRole(RoleName.INSURANCE_MANAGER))
                .findFirst()
                .orElse(null);

        int totalAssignments = 0;

        for (Client doctor : doctors) {
            String specialization = doctor.getSpecialization();
            if (specialization == null || specialization.isEmpty()) {
                specialization = "General Practice";
            }

            // Assign medicines based on specialization
            List<MedicinePrice> medicinesToAssign = getMedicinesForSpecialization(medicines, specialization);

            for (MedicinePrice medicine : medicinesToAssign) {
                DoctorMedicineAssignment assignment = DoctorMedicineAssignment.builder()
                        .doctor(doctor)
                        .medicine(medicine)
                        .assignedBy(assigner)
                        .specialization(specialization)
                        .maxDailyPrescriptions(getMaxDailyPrescriptions(medicine))
                        .maxQuantityPerPrescription(getMaxQuantityPerPrescription(medicine))
                        .notes("Auto-assigned during system initialization")
                        .active(true)
                        .build();
                doctorMedicineAssignmentRepository.save(assignment);
                totalAssignments++;
            }
        }

        log.info("✅ Initialized {} doctor medicine assignments", totalAssignments);
    }

    private List<MedicinePrice> getMedicinesForSpecialization(List<MedicinePrice> allMedicines, String specialization) {
        // All doctors can prescribe common medicines (pain relievers, antibiotics, etc.)
        List<String> commonMedicines = Arrays.asList(
                "Paracetamol", "Ibuprofen", "Amoxicillin", "Azithromycin",
                "Cetirizine", "Omeprazole", "Ranitidine"
        );

        // Specialization-specific medicines
        List<String> cardiologyMedicines = Arrays.asList(
                "Amlodipine", "Metoprolol", "Atorvastatin", "Clopidogrel", "Rosuvastatin"
        );

        List<String> endocrinologyMedicines = Arrays.asList(
                "Metformin", "Amlodipine"
        );

        List<String> neurologyMedicines = Arrays.asList(
                "Pregabalin", "Duloxetine"
        );

        String specLower = specialization.toLowerCase();

        return allMedicines.stream()
                .filter(m -> {
                    String drugName = m.getDrugName().toLowerCase();
                    String genericName = m.getGenericName() != null ? m.getGenericName().toLowerCase() : "";

                    // Check if it's a common medicine
                    boolean isCommon = commonMedicines.stream()
                            .anyMatch(common -> drugName.contains(common.toLowerCase()) ||
                                    genericName.contains(common.toLowerCase()));
                    if (isCommon) return true;

                    // Check specialization-specific medicines
                    if (specLower.contains("cardio") || specLower.contains("heart") || specLower.contains("قلب")) {
                        return cardiologyMedicines.stream()
                                .anyMatch(med -> drugName.contains(med.toLowerCase()) ||
                                        genericName.contains(med.toLowerCase()));
                    }

                    if (specLower.contains("endocrin") || specLower.contains("diabet") || specLower.contains("سكري") || specLower.contains("غدد")) {
                        return endocrinologyMedicines.stream()
                                .anyMatch(med -> drugName.contains(med.toLowerCase()) ||
                                        genericName.contains(med.toLowerCase()));
                    }

                    if (specLower.contains("neuro") || specLower.contains("أعصاب") || specLower.contains("عصب")) {
                        return neurologyMedicines.stream()
                                .anyMatch(med -> drugName.contains(med.toLowerCase()) ||
                                        genericName.contains(med.toLowerCase()));
                    }

                    // General practitioners and internal medicine get all covered medicines
                    if (specLower.contains("general") || specLower.contains("internal") ||
                            specLower.contains("عام") || specLower.contains("باطن")) {
                        return m.getCoverageStatus() == CoverageStatus.COVERED;
                    }

                    // Default: only common medicines
                    return false;
                })
                .toList();
    }

    private Integer getMaxDailyPrescriptions(MedicinePrice medicine) {
        // Set limits based on coverage status
        if (medicine.getCoverageStatus() == CoverageStatus.REQUIRES_APPROVAL) {
            return 5; // Lower limit for controlled medicines
        }
        return 20; // Higher limit for common medicines
    }

    private Integer getMaxQuantityPerPrescription(MedicinePrice medicine) {
        // Set quantity limits based on medicine type
        String type = medicine.getType() != null ? medicine.getType().toLowerCase() : "";
        if (type.contains("tablet") || type.contains("capsule")) {
            return medicine.getCoverageStatus() == CoverageStatus.REQUIRES_APPROVAL ? 30 : 90;
        }
        if (type.contains("solution") || type.contains("syrup")) {
            return 3; // 3 bottles max
        }
        return 30; // Default
    }
}
