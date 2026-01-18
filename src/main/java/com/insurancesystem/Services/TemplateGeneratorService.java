package com.insurancesystem.Services;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
@Slf4j
public class TemplateGeneratorService {

    /**
     * Generate Medicine Prices template (اسعار الادوية.xlsx)
     */
    public byte[] generateMedicinePricesTemplate() throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Medicine Prices");
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle exampleStyle = createExampleStyle(workbook);

            // Headers
            org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(0);
            String[] headers = {"Drug Name", "Composition", "Price"};
            for (int i = 0; i < headers.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Example data
            String[][] examples = {
                {"Paracetamol 500mg", "Paracetamol 500mg", "15"},
                {"Amoxicillin 250mg", "Amoxicillin Trihydrate 250mg", "25"},
                {"Omeprazole 20mg", "Omeprazole 20mg", "35"},
                {"Ibuprofen 400mg", "Ibuprofen 400mg", "20"},
                {"Metformin 500mg", "Metformin HCl 500mg", "18"}
            };

            for (int i = 0; i < examples.length; i++) {
                org.apache.poi.ss.usermodel.Row row = sheet.createRow(i + 1);
                for (int j = 0; j < examples[i].length; j++) {
                    org.apache.poi.ss.usermodel.Cell cell = row.createCell(j);
                    cell.setCellValue(examples[i][j]);
                    cell.setCellStyle(exampleStyle);
                }
            }

            autoSizeColumns(sheet, headers.length);
            return toByteArray(workbook);
        }
    }

    /**
     * Generate Medicine Data template with coverage (ملف الادوية.xlsx)
     */
    public byte[] generateMedicineDataTemplate() throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Medicine Data");
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle coveredStyle = createCoveredStyle(workbook);
            CellStyle approvalStyle = createApprovalStyle(workbook);
            CellStyle notCoveredStyle = createNotCoveredStyle(workbook);

            // Headers
            org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(0);
            String[] headers = {"Name", "Generic", "Type", "MED_UNIT", "COVERAGE_STATUS"};
            for (int i = 0; i < headers.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Example data with different coverage statuses
            Object[][] examples = {
                {"Panadol Extra", "Paracetamol", "Tablet", "500mg", "مغطى", coveredStyle},
                {"Augmentin", "Amoxicillin + Clavulanic Acid", "Tablet", "625mg", "مغطى", coveredStyle},
                {"Nexium", "Esomeprazole", "Capsule", "40mg", "مغطى", coveredStyle},
                {"Lipitor", "Atorvastatin", "Tablet", "20mg", "يحتاج الى موافقة", approvalStyle},
                {"Plavix", "Clopidogrel", "Tablet", "75mg", "يحتاج الى موافقة", approvalStyle},
                {"Humira", "Adalimumab", "Injection", "40mg", "غير مغطى", notCoveredStyle}
            };

            for (int i = 0; i < examples.length; i++) {
                org.apache.poi.ss.usermodel.Row row = sheet.createRow(i + 1);
                CellStyle rowStyle = (CellStyle) examples[i][5];
                for (int j = 0; j < 5; j++) {
                    org.apache.poi.ss.usermodel.Cell cell = row.createCell(j);
                    cell.setCellValue((String) examples[i][j]);
                    if (j == 4) { // Coverage status column
                        cell.setCellStyle(rowStyle);
                    }
                }
            }

            autoSizeColumns(sheet, headers.length);
            return toByteArray(workbook);
        }
    }

    /**
     * Generate Lab Tests template (فحوصات طبية.xlsx)
     */
    public byte[] generateLabTestsTemplate() throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Lab Tests");
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle coveredStyle = createCoveredStyle(workbook);
            CellStyle approvalStyle = createApprovalStyle(workbook);
            CellStyle notCoveredStyle = createNotCoveredStyle(workbook);

            // Headers
            org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(0);
            String[] headers = {"TEST_FORMAL_NAME", "PRICE", "COVERAGE_STATUS"};
            for (int i = 0; i < headers.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Example data
            Object[][] examples = {
                {"CBC (Complete Blood Count)", "50", "مغطى", coveredStyle},
                {"Lipid Profile", "80", "مغطى", coveredStyle},
                {"Fasting Blood Sugar", "30", "مغطى", coveredStyle},
                {"HbA1c (Glycated Hemoglobin)", "70", "مغطى", coveredStyle},
                {"Thyroid Panel (TSH/T3/T4)", "120", "مغطى", coveredStyle},
                {"Vitamin D", "120", "يحتاج الى موافقة", approvalStyle},
                {"Vitamin B12", "100", "يحتاج الى موافقة", approvalStyle},
                {"Genetic Testing", "500", "غير مغطى", notCoveredStyle}
            };

            for (int i = 0; i < examples.length; i++) {
                org.apache.poi.ss.usermodel.Row row = sheet.createRow(i + 1);
                CellStyle rowStyle = (CellStyle) examples[i][3];
                for (int j = 0; j < 3; j++) {
                    org.apache.poi.ss.usermodel.Cell cell = row.createCell(j);
                    cell.setCellValue((String) examples[i][j]);
                    if (j == 2) { // Coverage status column
                        cell.setCellStyle(rowStyle);
                    }
                }
            }

            autoSizeColumns(sheet, headers.length);
            return toByteArray(workbook);
        }
    }

    /**
     * Generate Radiology template (ملف الاشعة.xlsx)
     */
    public byte[] generateRadiologyTemplate() throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Radiology");
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle coveredStyle = createCoveredStyle(workbook);
            CellStyle approvalStyle = createApprovalStyle(workbook);
            CellStyle notCoveredStyle = createNotCoveredStyle(workbook);

            // Headers
            org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(0);
            String[] headers = {"RAY_NAME", "PRICE", "COVERAGE_STATUS"};
            for (int i = 0; i < headers.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Example data
            Object[][] examples = {
                {"Chest X-Ray", "80", "مغطى", coveredStyle},
                {"X-Ray Spine", "100", "مغطى", coveredStyle},
                {"Ultrasound Abdomen", "150", "مغطى", coveredStyle},
                {"Echocardiogram", "200", "مغطى", coveredStyle},
                {"CT Scan Head", "400", "يحتاج الى موافقة", approvalStyle},
                {"MRI Brain", "800", "يحتاج الى موافقة", approvalStyle},
                {"PET Scan", "2000", "غير مغطى", notCoveredStyle}
            };

            for (int i = 0; i < examples.length; i++) {
                org.apache.poi.ss.usermodel.Row row = sheet.createRow(i + 1);
                CellStyle rowStyle = (CellStyle) examples[i][3];
                for (int j = 0; j < 3; j++) {
                    org.apache.poi.ss.usermodel.Cell cell = row.createCell(j);
                    cell.setCellValue((String) examples[i][j]);
                    if (j == 2) { // Coverage status column
                        cell.setCellStyle(rowStyle);
                    }
                }
            }

            autoSizeColumns(sheet, headers.length);
            return toByteArray(workbook);
        }
    }

    /**
     * Generate Diagnoses template (تشخيصات طبية.xlsx)
     */
    public byte[] generateDiagnosesTemplate() throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Diagnoses");
            CellStyle headerStyle = createHeaderStyle(workbook);

            // Headers
            org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(0);
            String[] headers = {"English name", "Arabic name"};
            for (int i = 0; i < headers.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Example data
            String[][] examples = {
                {"Hypertension", "ارتفاع ضغط الدم"},
                {"Diabetes Mellitus Type 2", "السكري النوع الثاني"},
                {"Asthma", "الربو"},
                {"Coronary Artery Disease", "مرض الشريان التاجي"},
                {"Chronic Kidney Disease", "مرض الكلى المزمن"},
                {"Gastritis", "التهاب المعدة"},
                {"Migraine", "الصداع النصفي"},
                {"Depression", "الاكتئاب"},
                {"Hypothyroidism", "قصور الغدة الدرقية"},
                {"Osteoporosis", "هشاشة العظام"}
            };

            for (int i = 0; i < examples.length; i++) {
                org.apache.poi.ss.usermodel.Row row = sheet.createRow(i + 1);
                for (int j = 0; j < examples[i].length; j++) {
                    row.createCell(j).setCellValue(examples[i][j]);
                }
            }

            autoSizeColumns(sheet, headers.length);
            return toByteArray(workbook);
        }
    }

    /**
     * Generate Medical Center Data template (بيانات مركز طبي.xlsx)
     */
    public byte[] generateMedicalCenterTemplate() throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            // Sheet 1: Job Titles
            Sheet jobTitlesSheet = workbook.createSheet("Job Titles");
            CellStyle headerStyle = createHeaderStyle(workbook);

            org.apache.poi.ss.usermodel.Row headerRow1 = jobTitlesSheet.createRow(0);
            org.apache.poi.ss.usermodel.Cell headerCell1 = headerRow1.createCell(0);
            headerCell1.setCellValue("المسمى الوظيفي");
            headerCell1.setCellStyle(headerStyle);

            String[] jobTitles = {"طبيب عام", "طبيب أخصائي", "طبيب استشاري", "ممرض", "ممرضة",
                    "فني مختبر", "فني أشعة", "صيدلي", "أخصائي علاج طبيعي", "أخصائي تغذية"};
            for (int i = 0; i < jobTitles.length; i++) {
                jobTitlesSheet.createRow(i + 1).createCell(0).setCellValue(jobTitles[i]);
            }
            jobTitlesSheet.autoSizeColumn(0);

            // Sheet 2: Specializations
            Sheet specializationsSheet = workbook.createSheet("Specializations");

            org.apache.poi.ss.usermodel.Row headerRow2 = specializationsSheet.createRow(0);
            org.apache.poi.ss.usermodel.Cell headerCell2 = headerRow2.createCell(0);
            headerCell2.setCellValue("التخصص");
            headerCell2.setCellStyle(headerStyle);

            String[] specializations = {"طب عام", "طب باطني", "جراحة عامة", "طب أطفال",
                    "طب نسائية وتوليد", "جراحة عظام", "طب العيون", "طب الأنف والأذن والحنجرة",
                    "طب الجلدية", "طب القلب", "طب الأعصاب", "طب الكلى"};
            for (int i = 0; i < specializations.length; i++) {
                specializationsSheet.createRow(i + 1).createCell(0).setCellValue(specializations[i]);
            }
            specializationsSheet.autoSizeColumn(0);

            return toByteArray(workbook);
        }
    }

    /**
     * Generate Doctor Procedures template (اتفاقية طبيب.docx)
     */
    public byte[] generateDoctorProceduresTemplate() throws IOException {
        try (XWPFDocument document = new XWPFDocument()) {
            // Title
            XWPFParagraph title = document.createParagraph();
            title.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun titleRun = title.createRun();
            titleRun.setText("اتفاقية الاجراءات الطبية - Doctor Procedures Agreement");
            titleRun.setBold(true);
            titleRun.setFontSize(16);

            // Instructions
            XWPFParagraph instructions = document.createParagraph();
            XWPFRun instrRun = instructions.createRun();
            instrRun.setText("Instructions: Create 3 tables as shown below. Price ranges like '150-500' are supported.");
            instrRun.setItalic(true);
            instrRun.addBreak();

            // Table 1: General/Cardiology Procedures
            XWPFParagraph table1Title = document.createParagraph();
            XWPFRun t1Run = table1Title.createRun();
            t1Run.setText("Table 1: General/Cardiology Procedures (الاجراءات العامة)");
            t1Run.setBold(true);

            XWPFTable table1 = document.createTable(6, 2);
            table1.getRow(0).getCell(0).setText("الاجراء (Procedure)");
            table1.getRow(0).getCell(1).setText("التكلفة (Cost)");
            table1.getRow(1).getCell(0).setText("Echo cardio diagram");
            table1.getRow(1).getCell(1).setText("200");
            table1.getRow(2).getCell(0).setText("ECG normal");
            table1.getRow(2).getCell(1).setText("30");
            table1.getRow(3).getCell(0).setText("Stress test");
            table1.getRow(3).getCell(1).setText("175");
            table1.getRow(4).getCell(0).setText("Minor surgery");
            table1.getRow(4).getCell(1).setText("150-500");
            table1.getRow(5).getCell(0).setText("dressing");
            table1.getRow(5).getCell(1).setText("30-45");

            document.createParagraph(); // Spacing

            // Table 2: Surgery Procedures
            XWPFParagraph table2Title = document.createParagraph();
            XWPFRun t2Run = table2Title.createRun();
            t2Run.setText("Table 2: Surgery Procedures (عمليات صغرى)");
            t2Run.setBold(true);

            XWPFTable table2 = document.createTable(5, 2);
            table2.getRow(0).getCell(0).setText("التكلفة (Cost)");
            table2.getRow(0).getCell(1).setText("العملية (Operation)");
            table2.getRow(1).getCell(0).setText("500");
            table2.getRow(1).getCell(1).setText("SKIN BIOPSY");
            table2.getRow(2).getCell(0).setText("575");
            table2.getRow(2).getCell(1).setText("LIPOMA EXCISION");
            table2.getRow(3).getCell(0).setText("600");
            table2.getRow(3).getCell(1).setText("INGROWING TOE NAIL REMOVAL");
            table2.getRow(4).getCell(0).setText("400");
            table2.getRow(4).getCell(1).setText("NAVUS EXCISION");

            document.createParagraph(); // Spacing

            // Table 3: ENT Procedures
            XWPFParagraph table3Title = document.createParagraph();
            XWPFRun t3Run = table3Title.createRun();
            t3Run.setText("Table 3: ENT Procedures (عيادة الانف والاذن والحنجرة)");
            t3Run.setBold(true);

            XWPFTable table3 = document.createTable(5, 2);
            table3.getRow(0).getCell(0).setText("السعر / شيقل (Price)");
            table3.getRow(0).getCell(1).setText("الاجراء (Procedure)");
            table3.getRow(1).getCell(0).setText("150");
            table3.getRow(1).getCell(1).setText("Nasal cautery");
            table3.getRow(2).getCell(0).setText("80");
            table3.getRow(2).getCell(1).setText("Ear irrigation");
            table3.getRow(3).getCell(0).setText("200");
            table3.getRow(3).getCell(1).setText("Laryngoscopy");
            table3.getRow(4).getCell(0).setText("250");
            table3.getRow(4).getCell(1).setText("Removal of F.B");

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.write(out);
            return out.toByteArray();
        }
    }

    /**
     * Generate Insurance Policy template (بوليصة تامين.docx)
     */
    public byte[] generatePolicyTemplate() throws IOException {
        try (XWPFDocument document = new XWPFDocument()) {
            // Title
            XWPFParagraph title = document.createParagraph();
            title.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun titleRun = title.createRun();
            titleRun.setText("بوليصة التأمين الصحي - Health Insurance Policy");
            titleRun.setBold(true);
            titleRun.setFontSize(16);

            // Annual Limit
            XWPFParagraph limitSection = document.createParagraph();
            XWPFRun limitRun = limitSection.createRun();
            limitRun.setText("السقف المالي السنوي للمنتفع:");
            limitRun.setBold(true);
            limitRun.addBreak();
            limitRun.setText("يكون السقف المالي السنوي للتأمين الصحي للمنتفع 1000 دينار أردني");
            limitRun.addBreak();
            limitRun.setText("منها 100 دينار للزيارات والأدوية، و900 للعمليات والإقامة");

            document.createParagraph();

            // Consultation Fees
            XWPFParagraph consultSection = document.createParagraph();
            XWPFRun consultRun = consultSection.createRun();
            consultRun.setText("الكشفيات:");
            consultRun.setBold(true);
            consultRun.addBreak();
            consultRun.setText("الاخصائي 50 - العام 30");

            document.createParagraph();

            // Coverage Table
            XWPFParagraph coverageTitle = document.createParagraph();
            XWPFRun covTitleRun = coverageTitle.createRun();
            covTitleRun.setText("جدول التغطيات:");
            covTitleRun.setBold(true);

            XWPFTable coverageTable = document.createTable(8, 2);
            coverageTable.getRow(0).getCell(0).setText("نوع التغطية");
            coverageTable.getRow(0).getCell(1).setText("نسبة التغطية");
            coverageTable.getRow(1).getCell(0).setText("تغطية الأدوية غير المزمنة");
            coverageTable.getRow(1).getCell(1).setText("60%");
            coverageTable.getRow(2).getCell(0).setText("تغطية الصور الشعاعية والتلفزيونية");
            coverageTable.getRow(2).getCell(1).setText("100%");
            coverageTable.getRow(3).getCell(0).setText("تغطية فيتامين B12 و D3");
            coverageTable.getRow(3).getCell(1).setText("60%");
            coverageTable.getRow(4).getCell(0).setText("تغطية الماموجرام وفحص عنق الرحم");
            coverageTable.getRow(4).getCell(1).setText("100%");
            coverageTable.getRow(5).getCell(0).setText("تغطية الولادة الطبيعية");
            coverageTable.getRow(5).getCell(1).setText("250 دينار");
            coverageTable.getRow(6).getCell(0).setText("تغطية الولادة القيصرية");
            coverageTable.getRow(6).getCell(1).setText("560 دينار");
            coverageTable.getRow(7).getCell(0).setText("تغطية الشبكات والبالونات");
            coverageTable.getRow(7).getCell(1).setText("100% (حد أقصى 700 للشبكة، 300 للبالون)");

            document.createParagraph();

            // Visit Limits Table
            XWPFParagraph visitTitle = document.createParagraph();
            XWPFRun visitTitleRun = visitTitle.createRun();
            visitTitleRun.setText("جدول الزيارات:");
            visitTitleRun.setBold(true);

            XWPFTable visitTable = document.createTable(3, 2);
            visitTable.getRow(0).getCell(0).setText("نوع الزيارة");
            visitTable.getRow(0).getCell(1).setText("الحد الأقصى");
            visitTable.getRow(1).getCell(0).setText("زيارات الأطباء العادية");
            visitTable.getRow(1).getCell(1).setText("12 زيارة في السنة");
            visitTable.getRow(2).getCell(0).setText("زيارات الحامل");
            visitTable.getRow(2).getCell(1).setText("16 زيارة في السنة");

            document.createParagraph();

            // Exclusions
            XWPFParagraph exclusionsTitle = document.createParagraph();
            XWPFRun exclTitleRun = exclusionsTitle.createRun();
            exclTitleRun.setText("لا يشمل ولا يغطي التأمين:");
            exclTitleRun.setBold(true);

            XWPFParagraph exclusionsList = document.createParagraph();
            XWPFRun exclRun = exclusionsList.createRun();
            exclRun.setText("• المواصلات من وإلى الوحدات الصحية");
            exclRun.addBreak();
            exclRun.setText("• إصابات العمل وحوادث السير والألعاب الرياضية");
            exclRun.addBreak();
            exclRun.setText("• العقم وعمليات التلقيح الصناعي");
            exclRun.addBreak();
            exclRun.setText("• الأمراض الوبائية والسرطان وغسيل الكلى");
            exclRun.addBreak();
            exclRun.setText("• علاجات الفيتامينات والمكملات الغذائية (باستثناء الحمل والأطفال)");

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.write(out);
            return out.toByteArray();
        }
    }

    // Helper methods
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        org.apache.poi.ss.usermodel.Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        return style;
    }

    private CellStyle createExampleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private CellStyle createCoveredStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private CellStyle createApprovalStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.LIGHT_ORANGE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private CellStyle createNotCoveredStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.CORAL.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private void autoSizeColumns(Sheet sheet, int columnCount) {
        for (int i = 0; i < columnCount; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private byte[] toByteArray(Workbook workbook) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        return out.toByteArray();
    }
}
