package com.insurancesystem.Services;

import com.insurancesystem.Model.Entity.*;
import com.insurancesystem.Model.Entity.Enums.CoverageStatus;
import com.insurancesystem.Repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExcelImportService {

    private final MedicinePriceRepository medicinePriceRepository;
    private final MedicalTestRepository medicalTestRepository;
    private final MedicalDiagnosisRepository medicalDiagnosisRepository;
    private final DoctorProcedureRepository doctorProcedureRepository;
    private final DoctorSpecializationRepository doctorSpecializationRepository;

    public static class ImportResult {
        public boolean success = true;
        public String message = "";
        public int totalRows = 0;
        public int importedRows = 0;
        public int updatedRows = 0;
        public int errorRows = 0;
        public List<String> errors = new ArrayList<>();

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public String getSummary() {
            return String.format("Total: %d, Imported: %d, Updated: %d, Errors: %d",
                    totalRows, importedRows, updatedRows, errorRows);
        }
    }

    /**
     * Import medicine prices from اسعار الادوية.xlsx
     * Columns: Drug Name (col 0), Composition (col 1), Price (col 2)
     */
    @Transactional
    public ImportResult importMedicinePrices(MultipartFile file) throws IOException {
        ImportResult result = new ImportResult();

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            // Skip header row (row 0)
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                result.totalRows++;

                try {
                    String drugName = getCellValueAsString(row.getCell(0));
                    String composition = getCellValueAsString(row.getCell(1));
                    String priceStr = getCellValueAsString(row.getCell(2));

                    if (drugName == null || drugName.trim().isEmpty()) {
                        continue;
                    }

                    // Parse price - remove "شيكل" and any non-numeric chars
                    BigDecimal price = parsePrice(priceStr);

                    // Check if exists
                    Optional<MedicinePrice> existing = medicinePriceRepository.findByDrugNameIgnoreCase(drugName.trim());

                    MedicinePrice medicine;
                    if (existing.isPresent()) {
                        medicine = existing.get();
                        medicine.setComposition(composition);
                        medicine.setPrice(price);
                        result.updatedRows++;
                    } else {
                        medicine = MedicinePrice.builder()
                                .drugName(drugName.trim())
                                .composition(composition)
                                .price(price)
                                .coverageStatus(CoverageStatus.COVERED)
                                .active(true)
                                .build();
                        result.importedRows++;
                    }

                    medicinePriceRepository.save(medicine);

                } catch (Exception e) {
                    result.errorRows++;
                    result.errors.add("Row " + (i + 1) + ": " + e.getMessage());
                    log.warn("Error importing row {}: {}", i + 1, e.getMessage());
                }
            }
        }

        return result;
    }

    /**
     * Import medicine data with coverage from ملف الادوية.xlsx
     * Columns: Name (0), Generic name (1), Type (2), MED_UNIT_NA (3), COVERAGE_STATUS_NA (4)
     */
    @Transactional
    public ImportResult importMedicineData(MultipartFile file) throws IOException {
        ImportResult result = new ImportResult();

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            // Skip header row
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                result.totalRows++;

                try {
                    String name = getCellValueAsString(row.getCell(0));
                    String genericName = getCellValueAsString(row.getCell(1));
                    String type = getCellValueAsString(row.getCell(2));
                    String unit = getCellValueAsString(row.getCell(3));
                    String coverageStatusStr = getCellValueAsString(row.getCell(4));

                    if (name == null || name.trim().isEmpty()) {
                        continue;
                    }

                    CoverageStatus coverageStatus = parseCoverageStatus(coverageStatusStr);

                    // Check if exists
                    Optional<MedicinePrice> existing = medicinePriceRepository.findByDrugNameIgnoreCase(name.trim());

                    MedicinePrice medicine;
                    if (existing.isPresent()) {
                        medicine = existing.get();
                        medicine.setGenericName(genericName);
                        medicine.setType(type);
                        medicine.setUnit(unit);
                        medicine.setCoverageStatus(coverageStatus);
                        result.updatedRows++;
                    } else {
                        medicine = MedicinePrice.builder()
                                .drugName(name.trim())
                                .genericName(genericName)
                                .type(type)
                                .unit(unit)
                                .price(BigDecimal.ZERO)
                                .coverageStatus(coverageStatus)
                                .active(true)
                                .build();
                        result.importedRows++;
                    }

                    medicinePriceRepository.save(medicine);

                } catch (Exception e) {
                    result.errorRows++;
                    result.errors.add("Row " + (i + 1) + ": " + e.getMessage());
                    log.warn("Error importing row {}: {}", i + 1, e.getMessage());
                }
            }
        }

        return result;
    }

    /**
     * Import lab tests from فحوصات طبية.xlsx
     * Columns: TEST_FORMAL_NAME (0), COVERAGE_STATUS_NA (1)
     */
    @Transactional
    public ImportResult importMedicalTests(MultipartFile file) throws IOException {
        ImportResult result = new ImportResult();

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            // Skip header row
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                result.totalRows++;

                try {
                    String testName = getCellValueAsString(row.getCell(0));
                    String coverageStatusStr = getCellValueAsString(row.getCell(1));

                    if (testName == null || testName.trim().isEmpty()) {
                        continue;
                    }

                    CoverageStatus coverageStatus = parseCoverageStatus(coverageStatusStr);

                    // Check if exists
                    Optional<MedicalTest> existing = medicalTestRepository.findByTestNameIgnoreCaseAndCategory(testName.trim(), "LAB");

                    MedicalTest test;
                    if (existing.isPresent()) {
                        test = existing.get();
                        test.setCoverageStatus(coverageStatus);
                        result.updatedRows++;
                    } else {
                        test = MedicalTest.builder()
                                .testName(testName.trim())
                                .category("LAB")
                                .coverageStatus(coverageStatus)
                                .active(true)
                                .build();
                        result.importedRows++;
                    }

                    medicalTestRepository.save(test);

                } catch (Exception e) {
                    result.errorRows++;
                    result.errors.add("Row " + (i + 1) + ": " + e.getMessage());
                    log.warn("Error importing row {}: {}", i + 1, e.getMessage());
                }
            }
        }

        return result;
    }

    /**
     * Import radiology data from ملف الاشعة.xlsx
     * Columns: RAY_NAME (0), COVERAGE_STATUS_NA (1)
     */
    @Transactional
    public ImportResult importRadiologyData(MultipartFile file) throws IOException {
        ImportResult result = new ImportResult();

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            // Skip header row
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                result.totalRows++;

                try {
                    String rayName = getCellValueAsString(row.getCell(0));
                    String coverageStatusStr = getCellValueAsString(row.getCell(1));

                    if (rayName == null || rayName.trim().isEmpty()) {
                        continue;
                    }

                    CoverageStatus coverageStatus = parseCoverageStatus(coverageStatusStr);

                    // Check if exists
                    Optional<MedicalTest> existing = medicalTestRepository.findByTestNameIgnoreCaseAndCategory(rayName.trim(), "RADIOLOGY");

                    MedicalTest test;
                    if (existing.isPresent()) {
                        test = existing.get();
                        test.setCoverageStatus(coverageStatus);
                        result.updatedRows++;
                    } else {
                        test = MedicalTest.builder()
                                .testName(rayName.trim())
                                .category("RADIOLOGY")
                                .coverageStatus(coverageStatus)
                                .active(true)
                                .build();
                        result.importedRows++;
                    }

                    medicalTestRepository.save(test);

                } catch (Exception e) {
                    result.errorRows++;
                    result.errors.add("Row " + (i + 1) + ": " + e.getMessage());
                    log.warn("Error importing row {}: {}", i + 1, e.getMessage());
                }
            }
        }

        return result;
    }

    /**
     * Import diagnoses from تشخيصات طبية.xlsx
     * Columns: (empty), English name (1), Arabic name (2)
     */
    @Transactional
    public ImportResult importDiagnoses(MultipartFile file) throws IOException {
        ImportResult result = new ImportResult();

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            // Skip header row
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                result.totalRows++;

                try {
                    String englishName = getCellValueAsString(row.getCell(1));
                    String arabicName = getCellValueAsString(row.getCell(2));

                    if (englishName == null || englishName.trim().isEmpty()) {
                        continue;
                    }

                    // Check if exists
                    Optional<MedicalDiagnosis> existing = medicalDiagnosisRepository.findByEnglishNameIgnoreCase(englishName.trim());

                    MedicalDiagnosis diagnosis;
                    if (existing.isPresent()) {
                        diagnosis = existing.get();
                        if (arabicName != null && !arabicName.trim().isEmpty()) {
                            diagnosis.setArabicName(arabicName.trim());
                        }
                        result.updatedRows++;
                    } else {
                        diagnosis = MedicalDiagnosis.builder()
                                .englishName(englishName.trim())
                                .arabicName(arabicName != null ? arabicName.trim() : "")
                                .active(true)
                                .build();
                        result.importedRows++;
                    }

                    medicalDiagnosisRepository.save(diagnosis);

                } catch (Exception e) {
                    result.errorRows++;
                    result.errors.add("Row " + (i + 1) + ": " + e.getMessage());
                    log.warn("Error importing row {}: {}", i + 1, e.getMessage());
                }
            }
        }

        return result;
    }

    /**
     * Import medical center data (job titles, specializations) from بيانات مركز طبي.xlsx
     * Sheet 1 - Columns: المسمى الوظيفي (0), التخصصات (1)
     * Sheet 2 - Columns: Speciality (0), Arabic name (1)
     */
    @Transactional
    public ImportResult importMedicalCenterData(MultipartFile file) throws IOException {
        ImportResult result = new ImportResult();

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            // Process first sheet - specializations in Arabic
            if (workbook.getNumberOfSheets() > 0) {
                Sheet sheet1 = workbook.getSheetAt(0);
                Set<String> specializations = new HashSet<>();

                for (int i = 1; i <= sheet1.getLastRowNum(); i++) {
                    Row row = sheet1.getRow(i);
                    if (row == null) continue;

                    String specialization = getCellValueAsString(row.getCell(1));
                    if (specialization != null && !specialization.trim().isEmpty()) {
                        specializations.add(specialization.trim());
                    }
                }

                // Import unique specializations
                for (String spec : specializations) {
                    result.totalRows++;
                    try {
                        if (!doctorSpecializationRepository.existsByDisplayNameIgnoreCase(spec)) {
                            DoctorSpecializationEntity entity = new DoctorSpecializationEntity();
                            entity.setDisplayName(spec);
                            entity.setConsultationPrice(0.0); // Default consultation price
                            doctorSpecializationRepository.save(entity);
                            result.importedRows++;
                        } else {
                            result.updatedRows++;
                        }
                    } catch (Exception e) {
                        result.errorRows++;
                        result.errors.add("Specialization '" + spec + "': " + e.getMessage());
                    }
                }
            }

            // Process second sheet - English specializations
            if (workbook.getNumberOfSheets() > 1) {
                Sheet sheet2 = workbook.getSheetAt(1);

                for (int i = 1; i <= sheet2.getLastRowNum(); i++) {
                    Row row = sheet2.getRow(i);
                    if (row == null) continue;

                    result.totalRows++;

                    try {
                        String englishName = getCellValueAsString(row.getCell(0));
                        String arabicName = getCellValueAsString(row.getCell(1));

                        if (englishName == null || englishName.trim().isEmpty()) {
                            continue;
                        }

                        if (!doctorSpecializationRepository.existsByDisplayNameIgnoreCase(englishName.trim())) {
                            DoctorSpecializationEntity entity = new DoctorSpecializationEntity();
                            entity.setDisplayName(englishName.trim());
                            entity.setConsultationPrice(0.0); // Default consultation price
                            doctorSpecializationRepository.save(entity);
                            result.importedRows++;
                        } else {
                            result.updatedRows++;
                        }
                    } catch (Exception e) {
                        result.errorRows++;
                        result.errors.add("Row " + (i + 1) + " (Sheet 2): " + e.getMessage());
                    }
                }
            }
        }

        return result;
    }

    // Helper methods

    private String getCellValueAsString(Cell cell) {
        if (cell == null) return null;

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                }
                return String.valueOf((int) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return cell.getStringCellValue();
                } catch (Exception e) {
                    return String.valueOf(cell.getNumericCellValue());
                }
            default:
                return null;
        }
    }

    private BigDecimal parsePrice(String priceStr) {
        if (priceStr == null || priceStr.trim().isEmpty()) {
            return BigDecimal.ZERO;
        }

        // Remove Arabic text and non-numeric characters except decimal point
        String cleaned = priceStr.replaceAll("[^0-9.]", "").trim();

        if (cleaned.isEmpty()) {
            return BigDecimal.ZERO;
        }

        try {
            return new BigDecimal(cleaned);
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    private CoverageStatus parseCoverageStatus(String statusStr) {
        if (statusStr == null || statusStr.trim().isEmpty()) {
            return CoverageStatus.NOT_COVERED;
        }

        String status = statusStr.trim();

        // Check for covered status (مغطى / مغطاة)
        if (status.contains("مغطى") || status.contains("مغطاة") ||
            status.equalsIgnoreCase("covered")) {
            return CoverageStatus.COVERED;
        }

        // Check for requires approval (يحتاج الى موافقة / تحتاج الى موافقة)
        if (status.contains("موافقة") || status.contains("يحتاج") || status.contains("تحتاج") ||
            status.equalsIgnoreCase("requires approval") || status.equalsIgnoreCase("needs approval")) {
            return CoverageStatus.REQUIRES_APPROVAL;
        }

        return CoverageStatus.NOT_COVERED;
    }
}
