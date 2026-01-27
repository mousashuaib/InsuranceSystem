package com.insurancesystem.Config;

import com.insurancesystem.Model.Entity.*;
import com.insurancesystem.Model.Entity.Enums.*;
import com.insurancesystem.Repository.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Demo Data Initializer - Arabic Insurance System
 * Creates comprehensive test data for demonstration purposes
 * All accounts use password: 123123
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    private final RoleRepository roleRepository;
    private final ClientRepository clientRepository;
    private final FamilyMemberRepository familyMemberRepository;
    private final PolicyRepository policyRepository;
    private final CoverageRepository coverageRepository;
    private final PriceListRepository priceListRepository;
    private final DoctorSpecializationRepository doctorSpecializationRepository;
    private final SearchProfileRepository searchProfileRepository;
    private final HealthcareProviderClaimRepository healthcareProviderClaimRepository;
    private final PasswordEncoder passwordEncoder;

    private static final String DEFAULT_PASSWORD = "123123";

    // Store created entities for reference
    private Policy goldPolicy;
    private Policy silverPolicy;
    private Policy bronzePolicy;
    private final Map<String, Client> createdClients = new HashMap<>();
    private final Map<String, DoctorSpecializationEntity> createdSpecializations = new HashMap<>();

    @PostConstruct
    @Transactional
    public void init() {
        log.info("========================================");
        log.info("Starting Demo Data Initialization...");
        log.info("All accounts will use password: {}", DEFAULT_PASSWORD);
        log.info("========================================");

        // Initialize in order - Roles MUST be first
        initializeRoles();
        initializeDoctorSpecializations();
        initializePoliciesAndCoverages();
        initializePriceList();
        initializeAdminAccounts();
        initializeDoctors();
        initializePharmacists();
        initializeLabTechs();
        initializeRadiologists();
        initializeInsuranceClients();
        initializeSearchProfiles();
        initializePendingFamilyMemberRequests();
        initializeHealthcareProviderClaims();

        log.info("========================================");
        log.info("Demo Data Initialization Complete!");
        log.info("========================================");
    }

    // ==================== ROLES ====================
    private void initializeRoles() {
        log.info("Initializing Roles...");

        int createdCount = 0;
        for (RoleName roleName : RoleName.values()) {
            if (roleRepository.findByName(roleName).isEmpty()) {
                Role role = Role.builder()
                    .name(roleName)
                    .build();
                roleRepository.save(role);
                log.info("Created role: {}", roleName);
                createdCount++;
            }
        }

        if (createdCount > 0) {
            log.info("Created {} new roles", createdCount);
        } else {
            log.info("All roles already exist");
        }
    }

    // ==================== DOCTOR SPECIALIZATIONS ====================
    private void initializeDoctorSpecializations() {
        log.info("Initializing Doctor Specializations...");

        if (doctorSpecializationRepository.count() > 0) {
            log.info("Specializations already exist, loading and updating restrictions...");
            doctorSpecializationRepository.findAll().forEach(spec -> {
                createdSpecializations.put(spec.getDisplayName(), spec);

                // Update Pediatrics with age restrictions
                if ("طب الأطفال".equals(spec.getDisplayName())) {
                    spec.setMinAge(0);
                    spec.setMaxAge(18);
                    doctorSpecializationRepository.save(spec);
                    log.info("Updated Pediatrics with age restrictions: 0-18 years");
                }

                // Update OB-GYN with gender restrictions
                if ("طب النساء والتوليد".equals(spec.getDisplayName())) {
                    spec.setGender("FEMALE");
                    spec.setAllowedGenders(Arrays.asList("FEMALE"));
                    doctorSpecializationRepository.save(spec);
                    log.info("Updated OB-GYN with gender restriction: FEMALE only");
                }
            });
            return;
        }

        createSpecialization("طب القلب", 150.0,
            Arrays.asList("ارتفاع ضغط الدم", "قصور القلب", "اضطراب نبضات القلب", "الذبحة الصدرية"),
            Arrays.asList("أدوية الضغط", "منظم ضربات القلب", "قسطرة قلبية"));

        createSpecialization("طب الأطفال", 80.0,
            Arrays.asList("التهاب الحلق", "التهاب الأذن", "الحمى", "الإسهال"),
            Arrays.asList("مضادات حيوية", "خافض حرارة", "محاليل"),
            null, 0, 18);

        createSpecialization("طب الباطنة", 100.0,
            Arrays.asList("السكري", "ارتفاع الكوليسترول", "التهاب المعدة", "فقر الدم"),
            Arrays.asList("أدوية السكري", "أدوية الكوليسترول", "مكملات الحديد"));

        createSpecialization("طب العيون", 120.0,
            Arrays.asList("قصر النظر", "المياه البيضاء", "التهاب الملتحمة", "جفاف العين"),
            Arrays.asList("نظارات طبية", "عملية الليزك", "قطرات العين"));

        createSpecialization("طب الأسنان", 90.0,
            Arrays.asList("تسوس الأسنان", "التهاب اللثة", "خلع ضرس العقل"),
            Arrays.asList("حشو الأسنان", "تنظيف اللثة", "خلع الأسنان"));

        createSpecialization("طب النساء والتوليد", 130.0,
            Arrays.asList("متابعة الحمل", "تكيس المبايض", "اضطرابات الدورة"),
            Arrays.asList("فحص الحمل", "سونار", "أدوية هرمونية"),
            "FEMALE", null, null);

        createSpecialization("طب العظام", 110.0,
            Arrays.asList("كسور العظام", "التهاب المفاصل", "آلام الظهر", "الانزلاق الغضروفي"),
            Arrays.asList("جبيرة", "علاج طبيعي", "مسكنات"));

        createSpecialization("طب الجلدية", 100.0,
            Arrays.asList("حب الشباب", "الأكزيما", "الصدفية", "الثعلبة"),
            Arrays.asList("كريمات موضعية", "مضادات الهيستامين", "علاج ضوئي"));

        createSpecialization("طب الأعصاب", 140.0,
            Arrays.asList("الصداع النصفي", "الصرع", "التصلب اللويحي", "الشلل الرعاش"),
            Arrays.asList("مضادات الصرع", "مسكنات الأعصاب", "علاج طبيعي"));

        createSpecialization("الطب النفسي", 160.0,
            Arrays.asList("الاكتئاب", "القلق", "الأرق", "الوسواس القهري"),
            Arrays.asList("مضادات الاكتئاب", "مهدئات", "جلسات علاج نفسي"));

        log.info("Created {} doctor specializations", createdSpecializations.size());
    }

    private void createSpecialization(String name, double price, List<String> diagnoses,
                                      List<String> treatments) {
        createSpecialization(name, price, diagnoses, treatments, null, null, null);
    }

    private void createSpecialization(String name, double price, List<String> diagnoses,
                                      List<String> treatments, String gender,
                                      Integer minAge, Integer maxAge) {
        DoctorSpecializationEntity spec = new DoctorSpecializationEntity();
        spec.setDisplayName(name);
        spec.setConsultationPrice(price);
        spec.setDiagnoses(diagnoses);
        spec.setTreatmentPlans(treatments);
        spec.setGender(gender);

        // Set allowedGenders list if gender restriction is specified
        if (gender != null && !gender.isEmpty()) {
            spec.setAllowedGenders(Arrays.asList(gender));
        }

        spec.setMinAge(minAge);
        spec.setMaxAge(maxAge);
        spec = doctorSpecializationRepository.save(spec);
        createdSpecializations.put(name, spec);
    }

    // ==================== POLICIES AND COVERAGES ====================
    private void initializePoliciesAndCoverages() {
        log.info("Initializing Policies and Coverages...");

        if (policyRepository.count() > 0) {
            log.info("Policies already exist, loading...");
            goldPolicy = policyRepository.findByPolicyNo("POL-GOLD-2024").orElse(null);
            silverPolicy = policyRepository.findByPolicyNo("POL-SILVER-2024").orElse(null);
            bronzePolicy = policyRepository.findByPolicyNo("POL-BRONZE-2024").orElse(null);
            return;
        }

        // Gold Policy - التأمين الذهبي
        goldPolicy = createPolicy("POL-GOLD-2024", "التأمين الذهبي الشامل",
            "تغطية شاملة لجميع الخدمات الطبية بما في ذلك الأسنان والنظارات",
            new BigDecimal("100000"), new BigDecimal("50"),
            "تغطية طوارئ كاملة على مدار الساعة");

        createGoldCoverages(goldPolicy);

        // Silver Policy - التأمين الفضي
        silverPolicy = createPolicy("POL-SILVER-2024", "التأمين الفضي",
            "تغطية جيدة للخدمات الطبية الأساسية",
            new BigDecimal("50000"), new BigDecimal("100"),
            "تغطية طوارئ محدودة");

        createSilverCoverages(silverPolicy);

        // Bronze Policy - التأمين البرونزي
        bronzePolicy = createPolicy("POL-BRONZE-2024", "التأمين البرونزي الأساسي",
            "تغطية أساسية للخدمات الطبية الضرورية",
            new BigDecimal("25000"), new BigDecimal("200"),
            "طوارئ فقط للحالات الحرجة");

        createBronzeCoverages(bronzePolicy);

        log.info("Created 3 policies with coverages");
    }

    private Policy createPolicy(String policyNo, String name, String description,
                                BigDecimal limit, BigDecimal deductible, String emergencyRules) {
        Policy policy = Policy.builder()
            .policyNo(policyNo)
            .name(name)
            .description(description)
            .startDate(LocalDate.of(2024, 1, 1))
            .endDate(LocalDate.of(2025, 12, 31))
            .status(PolicyStatus.ACTIVE)
            .coverageLimit(limit)
            .deductible(deductible)
            .emergencyRules(emergencyRules)
            .build();
        return policyRepository.save(policy);
    }

    private void createGoldCoverages(Policy policy) {
        // Full coverage for Gold
        createCoverage(policy, "كشف طبي عام", CoverageType.OUTPATIENT, 100, new BigDecimal("500"), true);
        createCoverage(policy, "كشف طبي متخصص", CoverageType.OUTPATIENT, 100, new BigDecimal("1000"), true);
        createCoverage(policy, "إقامة في المستشفى", CoverageType.INPATIENT, 100, new BigDecimal("50000"), true);
        createCoverage(policy, "عمليات جراحية", CoverageType.INPATIENT, 90, new BigDecimal("30000"), true);
        createCoverage(policy, "علاج الأسنان", CoverageType.DENTAL, 80, new BigDecimal("5000"), false);
        createCoverage(policy, "فحوصات النظر والنظارات", CoverageType.OPTICAL, 80, new BigDecimal("3000"), false);
        createCoverage(policy, "طوارئ وإسعاف", CoverageType.EMERGENCY, 100, new BigDecimal("20000"), true);
        createCoverage(policy, "فحوصات مخبرية", CoverageType.LAB, 100, new BigDecimal("10000"), false);
        createCoverage(policy, "أشعة وتصوير", CoverageType.XRAY, 100, new BigDecimal("15000"), false);
    }

    private void createSilverCoverages(Policy policy) {
        createCoverage(policy, "كشف طبي عام", CoverageType.OUTPATIENT, 80, new BigDecimal("300"), true);
        createCoverage(policy, "كشف طبي متخصص", CoverageType.OUTPATIENT, 70, new BigDecimal("500"), true);
        createCoverage(policy, "إقامة في المستشفى", CoverageType.INPATIENT, 80, new BigDecimal("30000"), true);
        createCoverage(policy, "عمليات جراحية", CoverageType.INPATIENT, 70, new BigDecimal("20000"), true);
        createCoverage(policy, "طوارئ وإسعاف", CoverageType.EMERGENCY, 90, new BigDecimal("15000"), true);
        createCoverage(policy, "فحوصات مخبرية", CoverageType.LAB, 80, new BigDecimal("5000"), false);
        createCoverage(policy, "أشعة وتصوير", CoverageType.XRAY, 80, new BigDecimal("8000"), false);
    }

    private void createBronzeCoverages(Policy policy) {
        createCoverage(policy, "كشف طبي عام", CoverageType.OUTPATIENT, 60, new BigDecimal("200"), true);
        createCoverage(policy, "إقامة في المستشفى", CoverageType.INPATIENT, 60, new BigDecimal("15000"), true);
        createCoverage(policy, "طوارئ وإسعاف", CoverageType.EMERGENCY, 80, new BigDecimal("10000"), true);
        createCoverage(policy, "فحوصات مخبرية", CoverageType.LAB, 60, new BigDecimal("2000"), false);
        createCoverage(policy, "أشعة وتصوير", CoverageType.XRAY, 60, new BigDecimal("3000"), false);
    }

    private void createCoverage(Policy policy, String serviceName, CoverageType type,
                                int percentage, BigDecimal maxLimit, boolean emergencyEligible) {
        Coverage coverage = Coverage.builder()
            .policy(policy)
            .serviceName(serviceName)
            .description("تغطية " + serviceName)
            .coverageType(type)
            .coveragePercent(BigDecimal.valueOf(percentage))
            .maxLimit(maxLimit)
            .amount(BigDecimal.ZERO)
            .emergencyEligible(emergencyEligible)
            .covered(true)
            .minimumDeductible(BigDecimal.valueOf(10))
            .requiresReferral(false)
            .build();
        coverageRepository.save(coverage);
    }

    // ==================== PRICE LIST (Medicines, Labs, Radiology) ====================
    private void initializePriceList() {
        log.info("Initializing Price List...");

        if (priceListRepository.count() > 0) {
            log.info("Price list already exists, skipping...");
            return;
        }

        // Medicines - أدوية
        createMedicine("باراسيتامول 500mg", "MED001", 15.0, 30, "مسكن للألم وخافض للحرارة");
        createMedicine("أموكسيسيلين 500mg", "MED002", 25.0, 21, "مضاد حيوي واسع المجال");
        createMedicine("أوميبرازول 20mg", "MED003", 35.0, 28, "لعلاج حموضة المعدة");
        createMedicine("ميتفورمين 850mg", "MED004", 20.0, 30, "لعلاج السكري من النوع الثاني");
        createMedicine("أملوديبين 5mg", "MED005", 30.0, 30, "لعلاج ارتفاع ضغط الدم");
        createMedicine("سيتريزين 10mg", "MED006", 18.0, 20, "مضاد للحساسية");
        createMedicine("إيبوبروفين 400mg", "MED007", 22.0, 30, "مسكن ومضاد للالتهاب");
        createMedicine("أزيثرومايسين 250mg", "MED008", 45.0, 6, "مضاد حيوي");
        createMedicine("لوسارتان 50mg", "MED009", 40.0, 30, "لعلاج ضغط الدم");
        createMedicine("أتورفاستاتين 20mg", "MED010", 50.0, 30, "لخفض الكوليسترول");
        createMedicine("فيتامين د 1000 وحدة", "MED011", 25.0, 60, "مكمل غذائي");
        createMedicine("أوميغا 3", "MED012", 35.0, 90, "مكمل غذائي للقلب");
        createMedicine("فنتولين بخاخ", "MED013", 40.0, 1, "موسع للشعب الهوائية");
        createMedicine("انسولين نوفورابيد", "MED014", 120.0, 5, "لعلاج السكري");
        createMedicine("كريم فيوسيدين", "MED015", 28.0, 1, "مضاد حيوي موضعي");

        // Lab Tests - فحوصات مخبرية
        createLabTest("فحص دم شامل CBC", "LAB001", 50.0, "فحص مكونات الدم");
        createLabTest("فحص السكر صائم", "LAB002", 25.0, "قياس مستوى السكر");
        createLabTest("فحص السكر التراكمي HbA1c", "LAB003", 60.0, "متوسط السكر 3 أشهر");
        createLabTest("فحص وظائف الكلى", "LAB004", 80.0, "كرياتينين ويوريا");
        createLabTest("فحص وظائف الكبد", "LAB005", 90.0, "إنزيمات الكبد");
        createLabTest("فحص الدهون الشامل", "LAB006", 70.0, "كوليسترول ودهون ثلاثية");
        createLabTest("فحص الغدة الدرقية TSH", "LAB007", 65.0, "هرمون الغدة الدرقية");
        createLabTest("فحص فيتامين د", "LAB008", 80.0, "مستوى فيتامين د");
        createLabTest("فحص البول الكامل", "LAB009", 30.0, "تحليل البول");
        createLabTest("فحص الحمل", "LAB010", 35.0, "كشف هرمون الحمل");
        createLabTest("فحص حمض اليوريك", "LAB011", 40.0, "للنقرس");
        createLabTest("فحص الحديد وفيريتين", "LAB012", 75.0, "مخزون الحديد");
        createLabTest("زراعة بول", "LAB013", 90.0, "كشف البكتيريا");
        createLabTest("فحص فيروس كورونا PCR", "LAB014", 150.0, "كوفيد-19");
        createLabTest("فحص الروماتيزم RF", "LAB015", 55.0, "عامل الروماتويد");

        // Radiology - أشعة
        createRadiologyTest("أشعة صدر X-Ray", "RAD001", 80.0, "تصوير الصدر");
        createRadiologyTest("أشعة عظام", "RAD002", 70.0, "تصوير العظام");
        createRadiologyTest("سونار بطن كامل", "RAD003", 150.0, "تصوير بالموجات الصوتية");
        createRadiologyTest("سونار الغدة الدرقية", "RAD004", 120.0, "فحص الغدة");
        createRadiologyTest("أشعة مقطعية CT للرأس", "RAD005", 400.0, "تصوير الدماغ");
        createRadiologyTest("أشعة مقطعية CT للبطن", "RAD006", 500.0, "تصوير البطن");
        createRadiologyTest("رنين مغناطيسي MRI للركبة", "RAD007", 800.0, "تصوير المفاصل");
        createRadiologyTest("رنين مغناطيسي MRI للظهر", "RAD008", 900.0, "تصوير العمود الفقري");
        createRadiologyTest("ماموغرام", "RAD009", 200.0, "فحص الثدي");
        createRadiologyTest("إيكو القلب", "RAD010", 250.0, "تصوير القلب بالموجات");
        createRadiologyTest("دوبلر الأوعية الدموية", "RAD011", 300.0, "فحص الشرايين والأوردة");
        createRadiologyTest("بانوراما الأسنان", "RAD012", 100.0, "تصوير الفكين");

        log.info("Created price list with medicines, lab tests, and radiology");
    }

    private void createMedicine(String name, String code, double price, int quantity, String notes) {
        PriceList item = PriceList.builder()
            .providerType(ProviderType.PHARMACY)
            .serviceName(name)
            .serviceCode(code)
            .price(price)
            .quantity(quantity)
            .notes(notes)
            .coverageStatus(CoverageStatus.COVERED)
            .coveragePercentage(80)
            .active(true)
            .build();
        priceListRepository.save(item);
    }

    private void createLabTest(String name, String code, double price, String notes) {
        PriceList item = PriceList.builder()
            .providerType(ProviderType.LAB)
            .serviceName(name)
            .serviceCode(code)
            .price(price)
            .notes(notes)
            .coverageStatus(CoverageStatus.COVERED)
            .coveragePercentage(90)
            .active(true)
            .build();
        priceListRepository.save(item);
    }

    private void createRadiologyTest(String name, String code, double price, String notes) {
        PriceList item = PriceList.builder()
            .providerType(ProviderType.RADIOLOGY)
            .serviceName(name)
            .serviceCode(code)
            .price(price)
            .notes(notes)
            .coverageStatus(CoverageStatus.COVERED)
            .coveragePercentage(85)
            .active(true)
            .build();
        priceListRepository.save(item);
    }

    // ==================== ADMIN ACCOUNTS ====================
    private void initializeAdminAccounts() {
        log.info("Initializing Admin Accounts...");

        // Insurance Manager - مدير التأمين
        createClient("manager@demo.com", "أحمد محمد المدير", "0591000001", "1000000001",
            LocalDate.of(1980, 5, 15), "M", RoleName.INSURANCE_MANAGER, null,
            null, null, null, null, null, null, goldPolicy);

        // Medical Admin - المسؤول الطبي
        createClient("medical@demo.com", "سارة أحمد الطبية", "0591000002", "1000000002",
            LocalDate.of(1985, 8, 20), "F", RoleName.MEDICAL_ADMIN, null,
            null, null, null, null, null, null, goldPolicy);

        // Coordination Admin - مسؤول التنسيق
        createClient("coordination@demo.com", "خالد عمر التنسيق", "0591000003", "1000000003",
            LocalDate.of(1982, 3, 10), "M", RoleName.COORDINATION_ADMIN, null,
            null, null, null, null, null, null, goldPolicy);

        log.info("Created 3 admin accounts");
    }

    // ==================== DOCTORS ====================
    private void initializeDoctors() {
        log.info("Initializing Doctors...");

        createClient("doctor.cardio@demo.com", "د. محمد أحمد القلب", "0592000001", "2000000001",
            LocalDate.of(1975, 6, 12), "M", RoleName.DOCTOR, "طب القلب",
            "الطب", "قسم أمراض القلب", null, null, null, null, goldPolicy);

        createClient("doctor.pediatric@demo.com", "د. فاطمة علي الأطفال", "0592000002", "2000000002",
            LocalDate.of(1980, 9, 25), "F", RoleName.DOCTOR, "طب الأطفال",
            "الطب", "قسم طب الأطفال", null, null, null, null, goldPolicy);

        createClient("doctor.internal@demo.com", "د. عمر خالد الباطنة", "0592000003", "2000000003",
            LocalDate.of(1978, 2, 18), "M", RoleName.DOCTOR, "طب الباطنة",
            "الطب", "قسم الباطنة", null, null, null, null, goldPolicy);

        createClient("doctor.eye@demo.com", "د. نور حسن العيون", "0592000004", "2000000004",
            LocalDate.of(1982, 11, 5), "F", RoleName.DOCTOR, "طب العيون",
            "الطب", "قسم العيون", null, null, null, null, goldPolicy);

        createClient("doctor.dental@demo.com", "د. ياسر محمود الأسنان", "0592000005", "2000000005",
            LocalDate.of(1985, 4, 30), "M", RoleName.DOCTOR, "طب الأسنان",
            "طب الأسنان", "قسم الأسنان", null, null, null, null, goldPolicy);

        createClient("doctor.gyn@demo.com", "د. ريم سعيد النسائية", "0592000006", "2000000006",
            LocalDate.of(1979, 7, 15), "F", RoleName.DOCTOR, "طب النساء والتوليد",
            "الطب", "قسم النساء والتوليد", null, null, null, null, goldPolicy);

        createClient("doctor.ortho@demo.com", "د. سامي عادل العظام", "0592000007", "2000000007",
            LocalDate.of(1976, 12, 8), "M", RoleName.DOCTOR, "طب العظام",
            "الطب", "قسم جراحة العظام", null, null, null, null, goldPolicy);

        createClient("doctor.derma@demo.com", "د. لينا كمال الجلدية", "0592000008", "2000000008",
            LocalDate.of(1983, 1, 22), "F", RoleName.DOCTOR, "طب الجلدية",
            "الطب", "قسم الجلدية", null, null, null, null, goldPolicy);

        log.info("Created 8 doctor accounts");
    }

    // ==================== PHARMACISTS ====================
    private void initializePharmacists() {
        log.info("Initializing Pharmacists...");

        createClient("pharmacy1@demo.com", "يوسف أحمد الصيدلي", "0593000001", "3000000001",
            LocalDate.of(1988, 5, 10), "M", RoleName.PHARMACIST, null,
            null, null, "PH001", "صيدلية الشفاء", "رام الله - شارع الإرسال", null, goldPolicy);

        createClient("pharmacy2@demo.com", "هدى محمد الصيدلانية", "0593000002", "3000000002",
            LocalDate.of(1990, 8, 15), "F", RoleName.PHARMACIST, null,
            null, null, "PH002", "صيدلية الأمل", "نابلس - شارع فيصل", null, goldPolicy);

        createClient("pharmacy3@demo.com", "كريم علي الصيدلي", "0593000003", "3000000003",
            LocalDate.of(1986, 3, 20), "M", RoleName.PHARMACIST, null,
            null, null, "PH003", "صيدلية النور", "الخليل - باب الزاوية", null, goldPolicy);

        log.info("Created 3 pharmacist accounts");
    }

    // ==================== LAB TECHNICIANS ====================
    private void initializeLabTechs() {
        log.info("Initializing Lab Technicians...");

        createClient("lab1@demo.com", "عامر سعيد المختبر", "0594000001", "4000000001",
            LocalDate.of(1987, 6, 5), "M", RoleName.LAB_TECH, null,
            null, null, null, null, null, new String[]{"LAB001", "مختبر الحياة", "رام الله - المصيون"}, goldPolicy);

        createClient("lab2@demo.com", "سمر خالد المختبر", "0594000002", "4000000002",
            LocalDate.of(1991, 9, 12), "F", RoleName.LAB_TECH, null,
            null, null, null, null, null, new String[]{"LAB002", "مختبر الأمل", "نابلس - دوار الشهداء"}, goldPolicy);

        createClient("lab3@demo.com", "باسم عمر المختبر", "0594000003", "4000000003",
            LocalDate.of(1985, 4, 18), "M", RoleName.LAB_TECH, null,
            null, null, null, null, null, new String[]{"LAB003", "مختبر الصحة", "بيت لحم - شارع النجمة"}, goldPolicy);

        log.info("Created 3 lab tech accounts");
    }

    // ==================== RADIOLOGISTS ====================
    private void initializeRadiologists() {
        log.info("Initializing Radiologists...");

        createClient("radiology1@demo.com", "طارق حسين الأشعة", "0595000001", "5000000001",
            LocalDate.of(1984, 7, 22), "M", RoleName.RADIOLOGIST, null,
            null, null, null, null, null, new String[]{"RAD001", "مركز الأشعة الحديث", "رام الله - الماصيون"}, goldPolicy);

        createClient("radiology2@demo.com", "منى أحمد الأشعة", "0595000002", "5000000002",
            LocalDate.of(1989, 11, 8), "F", RoleName.RADIOLOGIST, null,
            null, null, null, null, null, new String[]{"RAD002", "مركز التصوير الطبي", "نابلس - المخفية"}, goldPolicy);

        log.info("Created 2 radiologist accounts");
    }

    // ==================== INSURANCE CLIENTS ====================
    private void initializeInsuranceClients() {
        log.info("Initializing Insurance Clients...");

        // Gold Policy Clients
        Client client1 = createClient("client1@demo.com", "محمد أحمد عبدالله", "0596000001", "6000000001",
            LocalDate.of(1985, 3, 15), "M", RoleName.INSURANCE_CLIENT, null,
            null, null, null, null, null, null, goldPolicy);
        createFamilyMembers(client1);
        // Add chronic diseases to client1 (Diabetes and Hypertension)
        addChronicDiseasesToClient(client1, ChronicDisease.DIABETES, ChronicDisease.HYPERTENSION);

        Client client2 = createClient("client2@demo.com", "فاطمة محمد سعيد", "0596000002", "6000000002",
            LocalDate.of(1990, 7, 20), "F", RoleName.INSURANCE_CLIENT, null,
            null, null, null, null, null, null, goldPolicy);
        // Add chronic disease to client2 (Asthma)
        addChronicDiseasesToClient(client2, ChronicDisease.ASTHMA);

        // Silver Policy Clients
        Client client3 = createClient("client3@demo.com", "أحمد خالد عمر", "0596000003", "6000000003",
            LocalDate.of(1982, 9, 10), "M", RoleName.INSURANCE_CLIENT, null,
            null, null, null, null, null, null, silverPolicy);
        createFamilyMembersForClient3(client3);
        // Add chronic diseases to client3 (Heart Disease and Thyroid)
        addChronicDiseasesToClient(client3, ChronicDisease.HEART_DISEASE, ChronicDisease.THYROID);

        Client client4 = createClient("client4@demo.com", "نور حسن علي", "0596000004", "6000000004",
            LocalDate.of(1988, 12, 5), "F", RoleName.INSURANCE_CLIENT, null,
            null, null, null, null, null, null, silverPolicy);

        // Bronze Policy Clients
        Client client5 = createClient("client5@demo.com", "عمر سعيد محمد", "0596000005", "6000000005",
            LocalDate.of(1995, 5, 25), "M", RoleName.INSURANCE_CLIENT, null,
            null, null, null, null, null, null, bronzePolicy);
        // Add chronic disease to client5 (Kidney Disease)
        addChronicDiseasesToClient(client5, ChronicDisease.KIDNEY_DISEASE);

        Client client6 = createClient("client6@demo.com", "سارة أحمد خالد", "0596000006", "6000000006",
            LocalDate.of(1992, 2, 14), "F", RoleName.INSURANCE_CLIENT, null,
            null, null, null, null, null, null, bronzePolicy);

        log.info("Created 6 insurance client accounts with family members");
        log.info("Added chronic diseases to 5 clients for demo purposes");
    }

    // Helper method to add chronic diseases to a client
    private void addChronicDiseasesToClient(Client client, ChronicDisease... diseases) {
        if (client == null) return;
        Set<ChronicDisease> chronicSet = new HashSet<>(Arrays.asList(diseases));
        client.setChronicDiseases(chronicSet);
        clientRepository.save(client);
        log.info("Added chronic diseases {} to client: {}", chronicSet, client.getFullName());
    }

    private void createFamilyMembers(Client client) {
        // Wife
        createFamilyMember(client, "سمية أحمد محمد", "FM" + client.getNationalId().substring(0, 7) + "01",
            client.getEmployeeId() + ".01", FamilyRelation.WIFE, Gender.FEMALE,
            LocalDate.of(1988, 5, 10));

        // Son
        createFamilyMember(client, "يوسف محمد أحمد", "FM" + client.getNationalId().substring(0, 7) + "02",
            client.getEmployeeId() + ".02", FamilyRelation.SON, Gender.MALE,
            LocalDate.of(2015, 8, 20));

        // Daughter
        createFamilyMember(client, "مريم محمد أحمد", "FM" + client.getNationalId().substring(0, 7) + "03",
            client.getEmployeeId() + ".03", FamilyRelation.DAUGHTER, Gender.FEMALE,
            LocalDate.of(2018, 3, 15));

        // Father
        createFamilyMember(client, "أحمد عبدالله محمد", "FM" + client.getNationalId().substring(0, 7) + "04",
            client.getEmployeeId() + ".04", FamilyRelation.FATHER, Gender.MALE,
            LocalDate.of(1955, 11, 25));

        // Mother
        createFamilyMember(client, "خديجة سعيد علي", "FM" + client.getNationalId().substring(0, 7) + "05",
            client.getEmployeeId() + ".05", FamilyRelation.MOTHER, Gender.FEMALE,
            LocalDate.of(1960, 6, 8));
    }

    private void createFamilyMembersForClient3(Client client) {
        // Wife
        createFamilyMember(client, "ريم سعيد أحمد", "FM" + client.getNationalId().substring(0, 7) + "01",
            client.getEmployeeId() + ".01", FamilyRelation.WIFE, Gender.FEMALE,
            LocalDate.of(1985, 4, 12));

        // Son
        createFamilyMember(client, "عمر أحمد خالد", "FM" + client.getNationalId().substring(0, 7) + "02",
            client.getEmployeeId() + ".02", FamilyRelation.SON, Gender.MALE,
            LocalDate.of(2012, 9, 5));

        // Daughter
        createFamilyMember(client, "لين أحمد خالد", "FM" + client.getNationalId().substring(0, 7) + "03",
            client.getEmployeeId() + ".03", FamilyRelation.DAUGHTER, Gender.FEMALE,
            LocalDate.of(2016, 1, 28));
    }

    private void createFamilyMember(Client client, String fullName, String nationalId,
                                    String insuranceNumber, FamilyRelation relation,
                                    Gender gender, LocalDate dateOfBirth) {
        if (familyMemberRepository.existsByNationalId(nationalId)) {
            return;
        }

        FamilyMember member = FamilyMember.builder()
            .client(client)
            .fullName(fullName)
            .nationalId(nationalId)
            .insuranceNumber(insuranceNumber)
            .relation(relation)
            .gender(gender)
            .dateOfBirth(dateOfBirth)
            .status(ProfileStatus.APPROVED)
            .documentImages(List.of())
            .build();
        familyMemberRepository.save(member);
    }

    // ==================== HELPER METHOD ====================
    private Client createClient(String email, String fullName, String phone, String nationalId,
                                LocalDate dateOfBirth, String gender, RoleName roleName,
                                String specialization, String faculty, String department,
                                String pharmacyCode, String pharmacyName, String pharmacyLocation,
                                String[] labRadInfo, Policy policy) {

        if (clientRepository.findByEmail(email).isPresent()) {
            Client existing = clientRepository.findByEmail(email).get();
            createdClients.put(email, existing);
            return existing;
        }

        Role role = roleRepository.findByName(roleName)
            .orElseThrow(() -> new RuntimeException(roleName + " role not found"));

        Set<Role> roles = new HashSet<>();
        roles.add(role);

        String employeeId = "EMP" + nationalId;

        Client.ClientBuilder builder = Client.builder()
            .email(email)
            .passwordHash(passwordEncoder.encode(DEFAULT_PASSWORD))
            .fullName(fullName)
            .phone(phone)
            .nationalId(nationalId)
            .employeeId(employeeId)
            .dateOfBirth(dateOfBirth)
            .gender(gender)
            .status(MemberStatus.ACTIVE)
            .roleRequestStatus(RoleRequestStatus.APPROVED)
            .requestedRole(roleName)
            .roles(roles)
            .emailVerified(true)
            .policy(policy);

        // Doctor specific
        if (roleName == RoleName.DOCTOR && specialization != null) {
            builder.specialization(specialization)
                .faculty(faculty)
                .department(department);
        }

        // Pharmacist specific
        if (roleName == RoleName.PHARMACIST && pharmacyCode != null) {
            builder.pharmacyCode(pharmacyCode)
                .pharmacyName(pharmacyName)
                .pharmacyLocation(pharmacyLocation);
        }

        // Lab Tech specific
        if (roleName == RoleName.LAB_TECH && labRadInfo != null) {
            builder.labCode(labRadInfo[0])
                .labName(labRadInfo[1])
                .labLocation(labRadInfo[2]);
        }

        // Radiologist specific
        if (roleName == RoleName.RADIOLOGIST && labRadInfo != null) {
            builder.radiologyCode(labRadInfo[0])
                .radiologyName(labRadInfo[1])
                .radiologyLocation(labRadInfo[2]);
        }

        Client client = builder.build();
        client = clientRepository.save(client);
        createdClients.put(email, client);

        log.info("Created {} account: {} ({})", roleName, fullName, email);
        return client;
    }

    // ==================== SEARCH PROFILES (Healthcare Providers on Map) ====================
    private void initializeSearchProfiles() {
        log.info("Initializing Search Profiles for Healthcare Providers...");

        // Fix any radiology profiles that were incorrectly created as CLINIC
        searchProfileRepository.findAll().stream()
            .filter(p -> p.getType() == SearchProfileType.CLINIC &&
                        p.getOwner() != null &&
                        p.getOwner().getEmail() != null &&
                        p.getOwner().getEmail().startsWith("radiology"))
            .forEach(p -> {
                p.setType(SearchProfileType.RADIOLOGY);
                searchProfileRepository.save(p);
                log.info("Fixed radiology profile type for: {}", p.getName());
            });

        boolean profilesExist = searchProfileRepository.count() > 0;

        // Always try to add PENDING profiles for demo (even if other profiles exist)
        initializePendingSearchProfiles();

        if (profilesExist) {
            log.info("Approved search profiles already exist, skipping approved profile creation...");
            log.info("Created {} search profiles", searchProfileRepository.count());
            return;
        }

        // Palestinian cities coordinates
        // Ramallah: 31.9038, 35.2034
        // Nablus: 32.2211, 35.2544
        // Hebron: 31.5326, 35.0998
        // Bethlehem: 31.7054, 35.2024

        // Doctor Profiles
        createSearchProfile("doctor.cardio@demo.com", "عيادة د. محمد القلب للقلب والأوعية الدموية",
            SearchProfileType.DOCTOR, "رام الله - شارع الإرسال", 31.9038, 35.2034,
            "0592000001", "أخصائي أمراض القلب والشرايين - خبرة 20 سنة");

        createSearchProfile("doctor.pediatric@demo.com", "عيادة د. فاطمة الأطفال لطب الأطفال",
            SearchProfileType.DOCTOR, "رام الله - المصيون", 31.9065, 35.2010,
            "0592000002", "أخصائية طب الأطفال وحديثي الولادة");

        createSearchProfile("doctor.internal@demo.com", "عيادة د. عمر الباطنة للأمراض الداخلية",
            SearchProfileType.DOCTOR, "نابلس - شارع فيصل", 32.2211, 35.2544,
            "0592000003", "أخصائي الطب الباطني والسكري");

        createSearchProfile("doctor.eye@demo.com", "مركز د. نور لطب العيون",
            SearchProfileType.DOCTOR, "نابلس - دوار الشهداء", 32.2230, 35.2560,
            "0592000004", "أخصائية جراحة العيون والليزك");

        createSearchProfile("doctor.dental@demo.com", "عيادة د. ياسر لطب الأسنان",
            SearchProfileType.DOCTOR, "الخليل - باب الزاوية", 31.5326, 35.0998,
            "0592000005", "أخصائي تجميل وزراعة الأسنان");

        createSearchProfile("doctor.gyn@demo.com", "عيادة د. ريم للنساء والتوليد",
            SearchProfileType.DOCTOR, "بيت لحم - شارع النجمة", 31.7054, 35.2024,
            "0592000006", "أخصائية أمراض النساء والتوليد");

        createSearchProfile("doctor.ortho@demo.com", "مركز د. سامي لجراحة العظام",
            SearchProfileType.DOCTOR, "رام الله - البالوع", 31.9010, 35.2070,
            "0592000007", "أخصائي جراحة العظام والمفاصل");

        createSearchProfile("doctor.derma@demo.com", "عيادة د. لينا للأمراض الجلدية",
            SearchProfileType.DOCTOR, "نابلس - شارع سفيان", 32.2200, 35.2520,
            "0592000008", "أخصائية الأمراض الجلدية والتجميل");

        // Pharmacy Profiles
        createSearchProfile("pharmacy1@demo.com", "صيدلية الشفاء",
            SearchProfileType.PHARMACY, "رام الله - شارع الإرسال", 31.9045, 35.2040,
            "0593000001", "صيدلية متكاملة - أدوية ومستحضرات طبية");

        createSearchProfile("pharmacy2@demo.com", "صيدلية الأمل",
            SearchProfileType.PHARMACY, "نابلس - شارع فيصل", 32.2220, 35.2550,
            "0593000002", "صيدلية على مدار الساعة");

        createSearchProfile("pharmacy3@demo.com", "صيدلية النور",
            SearchProfileType.PHARMACY, "الخليل - باب الزاوية", 31.5330, 35.1005,
            "0593000003", "صيدلية ومستودع أدوية");

        // Lab Profiles
        createSearchProfile("lab1@demo.com", "مختبر الحياة الطبي",
            SearchProfileType.LAB, "رام الله - المصيون", 31.9070, 35.2000,
            "0594000001", "مختبر تحاليل طبية شامل - نتائج سريعة");

        createSearchProfile("lab2@demo.com", "مختبر الأمل للتحاليل",
            SearchProfileType.LAB, "نابلس - دوار الشهداء", 32.2235, 35.2570,
            "0594000002", "مختبر معتمد - جميع أنواع التحاليل");

        createSearchProfile("lab3@demo.com", "مختبر الصحة",
            SearchProfileType.LAB, "بيت لحم - شارع النجمة", 31.7060, 35.2030,
            "0594000003", "مختبر تحاليل وفحوصات طبية");

        // Radiology Profiles
        createSearchProfile("radiology1@demo.com", "مركز الأشعة الحديث",
            SearchProfileType.RADIOLOGY, "رام الله - الماصيون", 31.9080, 35.1990,
            "0595000001", "أشعة سينية - رنين مغناطيسي - أشعة مقطعية");

        createSearchProfile("radiology2@demo.com", "مركز التصوير الطبي",
            SearchProfileType.RADIOLOGY, "نابلس - المخفية", 32.2180, 35.2500,
            "0595000002", "مركز متكامل للتصوير الإشعاعي");

        log.info("Created {} search profiles", searchProfileRepository.count());
    }

    // Separate method for pending profiles - called even if approved profiles exist
    private void initializePendingSearchProfiles() {
        log.info("Checking/Creating PENDING search profiles for demo...");

        // PENDING profiles for demo (to show in pending list)
        createPendingSearchProfile("doctor.cardio@demo.com", "مركز القلب الجديد - فرع جنين",
            SearchProfileType.DOCTOR, "جنين - شارع الناصرة", 32.4607, 35.2950,
            "0592100001", "فرع جديد لعيادة القلب");

        createPendingSearchProfile("pharmacy1@demo.com", "صيدلية الشفاء - فرع البيرة",
            SearchProfileType.PHARMACY, "البيرة - المنارة", 31.9100, 35.2100,
            "0593100001", "فرع جديد للصيدلية");

        createPendingSearchProfile("lab1@demo.com", "مختبر الحياة - فرع طولكرم",
            SearchProfileType.LAB, "طولكرم - شارع نابلس", 32.3100, 35.0300,
            "0594100001", "فرع جديد للمختبر");
    }

    private void createSearchProfile(String ownerEmail, String name, SearchProfileType type,
                                     String address, double lat, double lng,
                                     String contactInfo, String description) {
        createSearchProfileWithStatus(ownerEmail, name, type, address, lat, lng, contactInfo, description, ProfileStatus.APPROVED);
    }

    private void createPendingSearchProfile(String ownerEmail, String name, SearchProfileType type,
                                            String address, double lat, double lng,
                                            String contactInfo, String description) {
        createSearchProfileWithStatus(ownerEmail, name, type, address, lat, lng, contactInfo, description, ProfileStatus.PENDING);
    }

    private void createSearchProfileWithStatus(String ownerEmail, String name, SearchProfileType type,
                                               String address, double lat, double lng,
                                               String contactInfo, String description, ProfileStatus status) {
        Client owner = createdClients.get(ownerEmail);
        if (owner == null) {
            log.warn("Owner not found for search profile: {}", ownerEmail);
            return;
        }

        // Check if profile with same name already exists
        if (searchProfileRepository.findAll().stream().anyMatch(p -> p.getName().equals(name))) {
            return;
        }

        SearchProfile profile = SearchProfile.builder()
            .name(name)
            .type(type)
            .address(address)
            .locationLat(lat)
            .locationLng(lng)
            .contactInfo(contactInfo)
            .description(description)
            .owner(owner)
            .status(status)
            .build();

        searchProfileRepository.save(profile);
        log.info("Created search profile: {} ({})", name, type);
    }

    // ==================== PENDING FAMILY MEMBER REQUESTS ====================
    private void initializePendingFamilyMemberRequests() {
        log.info("Initializing Pending Family Member Requests...");

        // Get clients to add pending family member requests
        Client client2 = createdClients.get("client2@demo.com");
        Client client4 = createdClients.get("client4@demo.com");
        Client client5 = createdClients.get("client5@demo.com");
        Client client6 = createdClients.get("client6@demo.com");

        int pendingCount = 0;

        // Pending requests for client2 (فاطمة محمد سعيد) - Gold Policy
        if (client2 != null) {
            pendingCount += createPendingFamilyMember(client2, "أحمد محمد سعيد", "PFM200001",
                client2.getEmployeeId() + ".01", FamilyRelation.HUSBAND, Gender.MALE,
                LocalDate.of(1987, 4, 15));

            pendingCount += createPendingFamilyMember(client2, "ليلى محمد سعيد", "PFM200002",
                client2.getEmployeeId() + ".02", FamilyRelation.DAUGHTER, Gender.FEMALE,
                LocalDate.of(2015, 9, 22));

            pendingCount += createPendingFamilyMember(client2, "يوسف محمد سعيد", "PFM200003",
                client2.getEmployeeId() + ".03", FamilyRelation.SON, Gender.MALE,
                LocalDate.of(2018, 1, 10));
        }

        // Pending requests for client4 (نور حسن علي) - Silver Policy
        if (client4 != null) {
            pendingCount += createPendingFamilyMember(client4, "سامي حسن علي", "PFM400001",
                client4.getEmployeeId() + ".01", FamilyRelation.HUSBAND, Gender.MALE,
                LocalDate.of(1985, 11, 5));

            pendingCount += createPendingFamilyMember(client4, "جمال حسن علي", "PFM400002",
                client4.getEmployeeId() + ".02", FamilyRelation.FATHER, Gender.MALE,
                LocalDate.of(1958, 3, 20));

            pendingCount += createPendingFamilyMember(client4, "سعاد أحمد محمود", "PFM400003",
                client4.getEmployeeId() + ".03", FamilyRelation.MOTHER, Gender.FEMALE,
                LocalDate.of(1962, 7, 8));
        }

        // Pending requests for client5 (عمر سعيد محمد) - Bronze Policy
        if (client5 != null) {
            pendingCount += createPendingFamilyMember(client5, "سارة أحمد عمر", "PFM500001",
                client5.getEmployeeId() + ".01", FamilyRelation.WIFE, Gender.FEMALE,
                LocalDate.of(1997, 6, 18));

            pendingCount += createPendingFamilyMember(client5, "آدم عمر سعيد", "PFM500002",
                client5.getEmployeeId() + ".02", FamilyRelation.SON, Gender.MALE,
                LocalDate.of(2021, 12, 3));
        }

        // Pending requests for client6 (سارة أحمد خالد) - Bronze Policy
        if (client6 != null) {
            pendingCount += createPendingFamilyMember(client6, "خالد محمد أحمد", "PFM600001",
                client6.getEmployeeId() + ".01", FamilyRelation.HUSBAND, Gender.MALE,
                LocalDate.of(1990, 2, 25));

            pendingCount += createPendingFamilyMember(client6, "نور سارة خالد", "PFM600002",
                client6.getEmployeeId() + ".02", FamilyRelation.DAUGHTER, Gender.FEMALE,
                LocalDate.of(2019, 8, 14));

            pendingCount += createPendingFamilyMember(client6, "محمد أحمد خالد", "PFM600003",
                client6.getEmployeeId() + ".03", FamilyRelation.FATHER, Gender.MALE,
                LocalDate.of(1960, 5, 30));
        }

        log.info("Created {} pending family member requests", pendingCount);
    }

    private int createPendingFamilyMember(Client client, String fullName, String nationalId,
                                          String insuranceNumber, FamilyRelation relation,
                                          Gender gender, LocalDate dateOfBirth) {
        // Skip if already exists
        if (familyMemberRepository.existsByNationalId(nationalId)) {
            return 0;
        }

        FamilyMember member = FamilyMember.builder()
            .client(client)
            .fullName(fullName)
            .nationalId(nationalId)
            .insuranceNumber(insuranceNumber)
            .relation(relation)
            .gender(gender)
            .dateOfBirth(dateOfBirth)
            .status(ProfileStatus.PENDING)  // PENDING status for manager approval
            .documentImages(List.of())
            .build();
        familyMemberRepository.save(member);
        log.info("Created PENDING family member request: {} for client {}", fullName, client.getFullName());
        return 1;
    }

    // ==================== HEALTHCARE PROVIDER CLAIMS ====================
    private void initializeHealthcareProviderClaims() {
        log.info("Initializing Healthcare Provider Claims...");

        // Skip if claims already exist
        if (healthcareProviderClaimRepository.count() > 0) {
            log.info("Healthcare claims already exist, skipping...");
            return;
        }

        // Get providers
        Client doctor1 = createdClients.get("doctor.general@demo.com");
        Client doctor2 = createdClients.get("doctor.pediatric@demo.com");
        Client doctor3 = createdClients.get("doctor.internal@demo.com");
        Client pharmacy1 = createdClients.get("pharmacy1@demo.com");
        Client pharmacy2 = createdClients.get("pharmacy2@demo.com");
        Client lab1 = createdClients.get("lab1@demo.com");
        Client lab2 = createdClients.get("lab2@demo.com");
        Client radiology1 = createdClients.get("radiology1@demo.com");

        // Get clients
        Client client1 = createdClients.get("client1@demo.com");
        Client client2 = createdClients.get("client2@demo.com");
        Client client3 = createdClients.get("client3@demo.com");
        Client client4 = createdClients.get("client4@demo.com");
        Client client5 = createdClients.get("client5@demo.com");

        int claimCount = 0;

        // ========== APPROVED CLAIMS (APPROVED_FINAL) ==========
        if (doctor1 != null && client1 != null) {
            createClaim(doctor1, client1, goldPolicy, "استشارة طبية عامة - صداع مزمن",
                "صداع توتري مزمن", "راحة وتجنب الإجهاد، مسكنات عند الحاجة",
                150.0, LocalDate.now().minusDays(10), ClaimStatus.APPROVED_FINAL,
                Instant.now().minusSeconds(86400 * 8), null);
            claimCount++;
        }

        if (doctor2 != null && client2 != null) {
            createClaim(doctor2, client2, goldPolicy, "فحص أطفال روتيني",
                "فحص دوري - نمو طبيعي", "متابعة التطعيمات",
                120.0, LocalDate.now().minusDays(15), ClaimStatus.APPROVED_FINAL,
                Instant.now().minusSeconds(86400 * 12), null);
            claimCount++;
        }

        if (pharmacy1 != null && client1 != null) {
            createClaim(pharmacy1, client1, goldPolicy, "صرف أدوية - مسكنات",
                null, null, 85.0, LocalDate.now().minusDays(9), ClaimStatus.APPROVED_FINAL,
                Instant.now().minusSeconds(86400 * 7), "باراسيتامول 500mg x30\nإيبوبروفين 400mg x20");
            claimCount++;
        }

        if (lab1 != null && client3 != null) {
            createClaim(lab1, client3, silverPolicy, "فحوصات مخبرية شاملة",
                null, null, 180.0, LocalDate.now().minusDays(20), ClaimStatus.APPROVED_FINAL,
                Instant.now().minusSeconds(86400 * 18), "فحص دم شامل CBC\nفحص السكر صائم\nفحص الدهون الشامل");
            claimCount++;
        }

        if (radiology1 != null && client4 != null) {
            createClaim(radiology1, client4, silverPolicy, "أشعة صدر",
                null, null, 80.0, LocalDate.now().minusDays(25), ClaimStatus.APPROVED_FINAL,
                Instant.now().minusSeconds(86400 * 22), "أشعة صدر X-Ray - النتيجة طبيعية");
            claimCount++;
        }

        // ========== REJECTED CLAIMS (REJECTED_FINAL) ==========
        if (doctor3 != null && client5 != null) {
            HealthcareProviderClaim rejectedClaim = createClaim(doctor3, client5, bronzePolicy,
                "عملية تجميلية", "طلب عملية تجميل الأنف", "عملية تجميلية غير طبية",
                5000.0, LocalDate.now().minusDays(30), ClaimStatus.REJECTED_FINAL,
                Instant.now().minusSeconds(86400 * 25), null);
            if (rejectedClaim != null) {
                rejectedClaim.setRejectionReason("العمليات التجميلية غير مغطاة بالتأمين الصحي");
                rejectedClaim.setRejectedAt(Instant.now().minusSeconds(86400 * 24));
                healthcareProviderClaimRepository.save(rejectedClaim);
            }
            claimCount++;
        }

        if (pharmacy2 != null && client4 != null) {
            HealthcareProviderClaim rejectedClaim = createClaim(pharmacy2, client4, silverPolicy,
                "مكملات غذائية غير مغطاة", null, null,
                250.0, LocalDate.now().minusDays(18), ClaimStatus.REJECTED_FINAL,
                Instant.now().minusSeconds(86400 * 15), "فيتامينات متعددة\nمكملات البروتين");
            if (rejectedClaim != null) {
                rejectedClaim.setRejectionReason("المكملات الغذائية غير الطبية غير مشمولة بالتغطية");
                rejectedClaim.setRejectedAt(Instant.now().minusSeconds(86400 * 14));
                healthcareProviderClaimRepository.save(rejectedClaim);
            }
            claimCount++;
        }

        // ========== PENDING CLAIMS (PENDING_MEDICAL) ==========
        if (doctor1 != null && client2 != null) {
            createClaim(doctor1, client2, goldPolicy, "استشارة طبية - آلام الظهر",
                "آلام أسفل الظهر", "فحص سريري، قد يحتاج أشعة",
                200.0, LocalDate.now().minusDays(2), ClaimStatus.PENDING_MEDICAL, null, null);
            claimCount++;
        }

        if (doctor2 != null && client3 != null) {
            createClaim(doctor2, client3, silverPolicy, "فحص طفل - حرارة مرتفعة",
                "التهاب حلق حاد", "مضاد حيوي وخافض حرارة",
                130.0, LocalDate.now().minusDays(1), ClaimStatus.PENDING_MEDICAL, null, null);
            claimCount++;
        }

        if (lab2 != null && client1 != null) {
            createClaim(lab2, client1, goldPolicy, "فحوصات الغدة الدرقية",
                null, null, 145.0, LocalDate.now(), ClaimStatus.PENDING_MEDICAL,
                null, "فحص الغدة الدرقية TSH\nفحص T3 و T4");
            claimCount++;
        }

        if (pharmacy1 != null && client5 != null) {
            createClaim(pharmacy1, client5, bronzePolicy, "صرف أدوية السكري",
                null, null, 95.0, LocalDate.now().minusDays(3), ClaimStatus.PENDING_MEDICAL,
                null, "ميتفورمين 850mg x60\nجهاز قياس السكر");
            claimCount++;
        }

        // ========== RETURNED FOR REVIEW ==========
        if (doctor3 != null && client4 != null) {
            HealthcareProviderClaim returnedClaim = createClaim(doctor3, client4, silverPolicy,
                "استشارة باطنية - آلام المعدة", "التهاب المعدة المزمن", "أدوية وحمية غذائية",
                180.0, LocalDate.now().minusDays(5), ClaimStatus.RETURNED_FOR_REVIEW, null, null);
            if (returnedClaim != null) {
                returnedClaim.setRejectionReason("يرجى إرفاق نتائج المنظار السابقة للمراجعة");
                healthcareProviderClaimRepository.save(returnedClaim);
            }
            claimCount++;
        }

        log.info("Created {} healthcare provider claims", claimCount);
    }

    private HealthcareProviderClaim createClaim(Client provider, Client patient, Policy policy,
                                                 String description, String diagnosis, String treatmentDetails,
                                                 Double amount, LocalDate serviceDate, ClaimStatus status,
                                                 Instant approvedAt, String roleSpecificData) {
        if (provider == null || patient == null) {
            return null;
        }

        HealthcareProviderClaim claim = HealthcareProviderClaim.builder()
            .healthcareProvider(provider)
            .clientId(patient.getId())
            .clientName(patient.getFullName())
            .policy(policy)
            .description(description)
            .diagnosis(diagnosis)
            .treatmentDetails(treatmentDetails)
            .amount(amount)
            .serviceDate(serviceDate)
            .status(status)
            .submittedAt(Instant.now().minusSeconds(86400 * 30)) // Submitted 30 days ago
            .approvedAt(approvedAt)
            .roleSpecificData(roleSpecificData)
            .isCovered(status == ClaimStatus.APPROVED_FINAL)
            .insuranceCoveredAmount(status == ClaimStatus.APPROVED_FINAL ? BigDecimal.valueOf(amount * 0.8) : BigDecimal.ZERO)
            .clientPayAmount(status == ClaimStatus.APPROVED_FINAL ? BigDecimal.valueOf(amount * 0.2) : BigDecimal.ZERO)
            .coveragePercentUsed(status == ClaimStatus.APPROVED_FINAL ? BigDecimal.valueOf(80) : BigDecimal.ZERO)
            .build();

        healthcareProviderClaimRepository.save(claim);
        log.info("Created {} claim: {} for patient {}", status, description, patient.getFullName());
        return claim;
    }
}
