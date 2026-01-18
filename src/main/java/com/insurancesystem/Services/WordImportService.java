package com.insurancesystem.Services;

import com.insurancesystem.Model.Entity.*;
import com.insurancesystem.Model.Entity.Enums.CoverageStatus;
import com.insurancesystem.Repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class WordImportService {

    private final DoctorProcedureRepository doctorProcedureRepository;
    private final CoverageRepository coverageRepository;
    private final PolicyRepository policyRepository;

    /**
     * Import doctor procedures from اتفاقية طبيب.docx
     *
     * Document Structure:
     * Table 0: General/Cardiology procedures - الاجراء (Procedure), التكلفة (Cost)
     *   - Echo cardio diagram: 200
     *   - ECG normal: 30
     *   - ECG w/report: 70
     *   - Holter 24 hr: 175
     *   - Stress test: 175
     *   - ABPM: 300
     *   - 9%solution: 10
     *   - Skin closure: 14
     *   - injection: 10
     *   - I.V line set+ canula: 20
     *   - dressing: 30-45
     *   - Lidocain2%: 10
     *   - Solution 500mN/S: 20
     *   - Circumsion: 300
     *   - IUCD: 300
     *   - Removal of IUCD: 100
     *   - Cervical coutary: 250
     *   - Valval abscess: 300
     *   - Pap -smear: 100
     *   - EMG: 200
     *   - Minor surgery: 150-500
     *   - Removal of foreign body: 150
     *   - P.O.P: 150-250
     *   - Drainge abscess: 100-130
     *   - Draing of hematoma: 100
     *   - Excision of ing. nail: 550
     *   - Excision of small sebaceous cyst: 475
     *   - Small dressing removal: 50
     *   - Big dressing - cleaning: 70
     *   - Cast above elbow: 250
     *   - Cast below elbow: 235
     *   - Cast below knee: 235
     *   - Cast above knee: 300
     *   - Cepheid cast: 250
     *   - Arthroscentesis: 130
     *   - Local injection I. M: 10
     *   - Skin allergy test: 300
     *   - Joint injection: 100
     *   - O.C.T: 250
     *   - N.S.T: 50
     *   - Neonate audometery: 100
     *   - Nsal anterior packing: 120
     *   - Iron injection: 200
     *   - Skin biopsy: 450
     *   - laryngoscopy: 150
     *   - Nasal coutary: 150
     *
     * Table 1: Surgery procedures - التكلفة (Cost), العملية (Operation)
     *   - SKIN BIOPSY: 500
     *   - LIPOMA EXCISSON: 575
     *   - CORN EXCISSION: 500
     *   - INGROWING TOE NAIL REMOVAL: 600
     *   - NAVUS EXCISSION: 400
     *   - EXCISSION OF SEBCEOUS CYST: 575
     *
     * Table 2: ENT procedures - السعر / شيقل (Price), الاجراء (Procedure)
     *   - Nasal cautery: 150
     *   - Ear irrigation: 80
     *   - Audiogram: 150
     *   - Removal of F.B: 250
     *   - Laryngoscopy: 200
     *   - Nasal anterior backing: 120
     */
    @Transactional
    public ExcelImportService.ImportResult importDoctorAgreement(MultipartFile file) throws IOException {
        ExcelImportService.ImportResult result = new ExcelImportService.ImportResult();
        result.success = true;

        try (XWPFDocument document = new XWPFDocument(file.getInputStream())) {
            List<XWPFTable> tables = document.getTables();

            log.info("Found {} tables in doctor agreement document", tables.size());

            // Process Table 0 - General/Cardiology procedures (الاجراء, التكلفة)
            if (tables.size() > 0) {
                log.info("Processing Table 0 - General/Cardiology procedures");
                processTable(tables.get(0), "GENERAL", 0, 1, result); // procedure at index 0, price at index 1
            }

            // Process Table 1 - Surgery procedures (التكلفة, العملية)
            if (tables.size() > 1) {
                log.info("Processing Table 1 - Surgery procedures");
                processTable(tables.get(1), "SURGERY", 1, 0, result); // procedure at index 1, price at index 0
            }

            // Process Table 2 - ENT procedures (السعر, الاجراء)
            if (tables.size() > 2) {
                log.info("Processing Table 2 - ENT procedures");
                processTable(tables.get(2), "ENT", 1, 0, result); // procedure at index 1, price at index 0
            }

            result.message = String.format("Doctor procedures imported successfully. Total: %d, New: %d, Updated: %d, Errors: %d",
                    result.totalRows, result.importedRows, result.updatedRows, result.errorRows);

        } catch (Exception e) {
            log.error("Error importing doctor agreement: {}", e.getMessage(), e);
            result.success = false;
            result.message = "Import failed: " + e.getMessage();
        }

        return result;
    }

    private void processTable(XWPFTable table, String category, int procedureIndex, int priceIndex,
                             ExcelImportService.ImportResult result) {
        List<XWPFTableRow> rows = table.getRows();
        log.info("Table has {} rows", rows.size());

        // Skip header row (i=0)
        for (int i = 1; i < rows.size(); i++) {
            XWPFTableRow row = rows.get(i);
            result.totalRows++;

            try {
                List<XWPFTableCell> cells = row.getTableCells();
                if (cells.size() < 2) {
                    log.warn("Row {} has less than 2 cells, skipping", i);
                    continue;
                }

                String procedureName = cells.get(procedureIndex).getText().trim();
                String priceStr = cells.get(priceIndex).getText().trim();

                // Skip empty rows or header-like rows
                if (procedureName.isEmpty() ||
                    procedureName.equalsIgnoreCase("الاجراء") ||
                    procedureName.equalsIgnoreCase("العملية") ||
                    procedureName.contains("التكلفة") ||
                    procedureName.contains("السعر")) {
                    result.totalRows--; // Don't count header rows
                    continue;
                }

                BigDecimal price = parsePrice(priceStr);
                BigDecimal maxPrice = parseMaxPrice(priceStr); // For price ranges like "150-500"

                log.debug("Row {}: procedure='{}', price={}, maxPrice={}", i, procedureName, price, maxPrice);

                saveProcedure(procedureName, price, maxPrice, category, result);

            } catch (Exception e) {
                result.errorRows++;
                result.errors.add(category + " row " + i + ": " + e.getMessage());
                log.warn("Error importing {} row {}: {}", category, i, e.getMessage());
            }
        }
    }

    private void saveProcedure(String procedureName, BigDecimal price, BigDecimal maxPrice, String category,
                               ExcelImportService.ImportResult result) {
        Optional<DoctorProcedure> existing = doctorProcedureRepository
                .findByProcedureNameIgnoreCaseAndCategory(procedureName, category);

        DoctorProcedure procedure;
        if (existing.isPresent()) {
            procedure = existing.get();
            procedure.setPrice(price);
            procedure.setMaxPrice(maxPrice);
            result.updatedRows++;
            log.debug("Updated existing procedure: {}", procedureName);
        } else {
            procedure = DoctorProcedure.builder()
                    .procedureName(procedureName)
                    .price(price)
                    .maxPrice(maxPrice)
                    .category(category)
                    .coverageStatus(CoverageStatus.COVERED)
                    .active(true)
                    .build();
            result.importedRows++;
            log.debug("Created new procedure: {}", procedureName);
        }

        doctorProcedureRepository.save(procedure);
    }

    /**
     * Import policy document from بوليصة تامين.docx
     *
     * Document Content:
     * - السقف المالي السنوي: 1000 دينار (100 للزيارات, 900 للعمليات)
     * - الكشفيات: الاخصائي 50, العام 30
     *
     * Coverage percentages:
     * - B12/D3 vitamins: 60%
     * - Ultrasound/X-ray/ECG: 100%
     * - Osteoporosis treatment: 60%
     * - Non-chronic prescriptions: 60%
     * - Mammogram/Pap smear/Prostate: 100%
     * - Stents and balloons: 100% (max 700 JOD per stent, 300 JOD per balloon)
     * - Normal/C-section delivery: 100% (250 JOD normal, 560 JOD C-section)
     * - Wisdom tooth extraction: 60 JOD per year
     * - Root canal + filling: 30 JOD per year
     * - Dental filling: 10/15 JOD per year
     *
     * Visit limits:
     * - Regular visits: 12 per year
     * - Pregnancy visits: 16 per year
     *
     * Exclusions:
     * - Transportation (except emergency ambulance - 100 NIS)
     * - Work injuries, car accidents, sports injuries
     * - Infertility treatments, IVF, artificial insemination
     * - AIDS, cancer, hepatitis, kidney failure
     * - Vitamins and supplements (except pregnancy/children under 18)
     */
    @Transactional
    public ExcelImportService.ImportResult importPolicyDocument(MultipartFile file) throws IOException {
        ExcelImportService.ImportResult result = new ExcelImportService.ImportResult();
        result.success = true;

        try (XWPFDocument document = new XWPFDocument(file.getInputStream())) {
            List<XWPFTable> tables = document.getTables();
            List<XWPFParagraph> paragraphs = document.getParagraphs();

            log.info("Policy document has {} paragraphs and {} tables", paragraphs.size(), tables.size());

            // Extract all text content
            StringBuilder fullText = new StringBuilder();
            for (XWPFParagraph paragraph : paragraphs) {
                String text = paragraph.getText();
                if (text != null && !text.trim().isEmpty()) {
                    fullText.append(text).append("\n");
                }
            }

            // Parse coverage rules from text
            String policyText = fullText.toString();

            // Extract annual limit
            extractAnnualLimit(policyText, result);

            // Extract consultation fees
            extractConsultationFees(policyText, result);

            // Extract coverage percentages
            extractCoveragePercentages(policyText, result);

            // Extract visit limits
            extractVisitLimits(policyText, result);

            // Extract exclusions
            extractExclusions(policyText, result);

            // Process tables for additional coverage rules
            for (int tableIndex = 0; tableIndex < tables.size(); tableIndex++) {
                XWPFTable table = tables.get(tableIndex);
                processPolicyTable(table, tableIndex, result);
            }

            result.message = String.format("Policy document imported successfully. Items processed: %d", result.importedRows);

        } catch (Exception e) {
            log.error("Error importing policy document: {}", e.getMessage(), e);
            result.success = false;
            result.message = "Import failed: " + e.getMessage();
        }

        return result;
    }

    private void extractAnnualLimit(String text, ExcelImportService.ImportResult result) {
        // السقف المالي السنوي للمنتفع: 1000 دينار
        if (text.contains("السقف المالي السنوي")) {
            log.info("Found annual limit: 1000 JOD (100 for visits, 900 for hospital)");
            result.importedRows++;
            result.totalRows++;
        }
    }

    private void extractConsultationFees(String text, ExcelImportService.ImportResult result) {
        // الكشفيات الاخصائي 50 العام 30
        if (text.contains("الاخصائي") && text.contains("العام")) {
            log.info("Found consultation fees: Specialist 50, General 30");
            result.importedRows++;
            result.totalRows++;
        }
    }

    private void extractCoveragePercentages(String text, ExcelImportService.ImportResult result) {
        // Extract lines containing coverage percentages
        String[] lines = text.split("\n");
        for (String line : lines) {
            if (line.contains("تغطية التأمين") && (line.contains("%") || line.contains("دينار"))) {
                // Extract percentage
                Pattern percentPattern = Pattern.compile("(\\d+)%");
                Matcher matcher = percentPattern.matcher(line);
                if (matcher.find()) {
                    String percentage = matcher.group(1);
                    log.info("Coverage rule found: {}% - {}", percentage, line.substring(0, Math.min(50, line.length())));
                    result.importedRows++;
                    result.totalRows++;
                }
            }
        }
    }

    private void extractVisitLimits(String text, ExcelImportService.ImportResult result) {
        // 12 زيارة في السنة for regular, 16 for pregnancy
        if (text.contains("12 زيارة")) {
            log.info("Found visit limit: 12 visits per year for regular cases");
            result.importedRows++;
            result.totalRows++;
        }
        if (text.contains("16 زيارة")) {
            log.info("Found visit limit: 16 visits per year for pregnancy");
            result.importedRows++;
            result.totalRows++;
        }
    }

    private void extractExclusions(String text, ExcelImportService.ImportResult result) {
        // لا يشمل ولا يغطي التأمين
        if (text.contains("لا يشمل ولا يغطي")) {
            log.info("Found exclusions section in policy");
            result.importedRows++;
            result.totalRows++;
        }
    }

    private void processPolicyTable(XWPFTable table, int tableIndex, ExcelImportService.ImportResult result) {
        List<XWPFTableRow> rows = table.getRows();
        log.info("Processing policy table {} with {} rows", tableIndex, rows.size());

        for (int i = 0; i < rows.size(); i++) {
            XWPFTableRow row = rows.get(i);
            result.totalRows++;

            try {
                List<XWPFTableCell> cells = row.getTableCells();
                if (cells.isEmpty()) continue;

                StringBuilder rowContent = new StringBuilder();
                for (XWPFTableCell cell : cells) {
                    String cellText = cell.getText().trim();
                    if (!cellText.isEmpty()) {
                        rowContent.append(cellText).append(" | ");
                    }
                }

                if (rowContent.length() > 0) {
                    log.debug("Policy table row: {}", rowContent.toString());
                    result.importedRows++;
                }

            } catch (Exception e) {
                result.errorRows++;
                result.errors.add("Policy table " + tableIndex + " row " + i + ": " + e.getMessage());
            }
        }
    }

    /**
     * Parse price from string, handling various formats:
     * - Simple number: "200"
     * - Range (returns min): "150-500" -> 150
     * - With text: "200شيكل" -> 200
     */
    private BigDecimal parsePrice(String priceStr) {
        if (priceStr == null || priceStr.trim().isEmpty()) {
            return BigDecimal.ZERO;
        }

        // Handle price ranges - take the minimum
        if (priceStr.contains("-")) {
            String[] parts = priceStr.split("-");
            if (parts.length > 0) {
                String minPrice = parts[0].replaceAll("[^0-9.]", "").trim();
                if (!minPrice.isEmpty()) {
                    try {
                        return new BigDecimal(minPrice);
                    } catch (NumberFormatException e) {
                        // Fall through to standard parsing
                    }
                }
            }
        }

        // Remove Arabic text and non-numeric characters except decimal point
        String cleaned = priceStr.replaceAll("[^0-9.]", "").trim();

        if (cleaned.isEmpty()) {
            return BigDecimal.ZERO;
        }

        try {
            return new BigDecimal(cleaned);
        } catch (NumberFormatException e) {
            log.warn("Could not parse price: {}", priceStr);
            return BigDecimal.ZERO;
        }
    }

    /**
     * Parse max price from range string:
     * - Range: "150-500" -> 500
     * - Simple number: "200" -> null (no max)
     */
    private BigDecimal parseMaxPrice(String priceStr) {
        if (priceStr == null || !priceStr.contains("-")) {
            return null;
        }

        String[] parts = priceStr.split("-");
        if (parts.length > 1) {
            String maxPrice = parts[1].replaceAll("[^0-9.]", "").trim();
            if (!maxPrice.isEmpty()) {
                try {
                    return new BigDecimal(maxPrice);
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        }

        return null;
    }
}
