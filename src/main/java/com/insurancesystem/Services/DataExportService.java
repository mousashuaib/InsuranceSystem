package com.insurancesystem.Services;

import com.insurancesystem.Model.Entity.*;
import com.insurancesystem.Model.Entity.Enums.CoverageType;
import com.insurancesystem.Repository.*;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataExportService {

    private final MedicinePriceRepository medicinePriceRepository;
    private final MedicalTestRepository medicalTestRepository;
    private final MedicalDiagnosisRepository medicalDiagnosisRepository;
    private final DoctorProcedureRepository doctorProcedureRepository;
    private final PolicyRepository policyRepository;

    // ==================== EXCEL EXPORTS ====================

    public byte[] exportMedicinesToExcel() throws IOException {
        List<MedicinePrice> medicines = medicinePriceRepository.findByActiveTrue();

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Medicines");

            // Create header style
            CellStyle headerStyle = createHeaderStyle(workbook);

            // Create header row
            org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(0);
            String[] headers = {"Drug Name", "Generic Name", "Composition", "Type", "Unit", "Price", "Coverage Status"};
            for (int i = 0; i < headers.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Create data rows
            int rowNum = 1;
            for (MedicinePrice medicine : medicines) {
                org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(medicine.getDrugName() != null ? medicine.getDrugName() : "");
                row.createCell(1).setCellValue(medicine.getGenericName() != null ? medicine.getGenericName() : "");
                row.createCell(2).setCellValue(medicine.getComposition() != null ? medicine.getComposition() : "");
                row.createCell(3).setCellValue(medicine.getType() != null ? medicine.getType() : "");
                row.createCell(4).setCellValue(medicine.getUnit() != null ? medicine.getUnit() : "");
                row.createCell(5).setCellValue(medicine.getPrice() != null ? medicine.getPrice().doubleValue() : 0);
                row.createCell(6).setCellValue(medicine.getCoverageStatus() != null ? medicine.getCoverageStatus().name() : "");
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    public byte[] exportLabTestsToExcel() throws IOException {
        List<MedicalTest> tests = medicalTestRepository.findByCategoryAndActiveTrue("LAB");

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Lab Tests");

            CellStyle headerStyle = createHeaderStyle(workbook);

            org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(0);
            String[] headers = {"Test Name", "Category", "Price", "Coverage Status"};
            for (int i = 0; i < headers.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;
            for (MedicalTest test : tests) {
                org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(test.getTestName() != null ? test.getTestName() : "");
                row.createCell(1).setCellValue(test.getCategory() != null ? test.getCategory() : "");
                row.createCell(2).setCellValue(test.getPrice() != null ? test.getPrice().doubleValue() : 0);
                row.createCell(3).setCellValue(test.getCoverageStatus() != null ? test.getCoverageStatus().name() : "");
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    public byte[] exportRadiologyToExcel() throws IOException {
        List<MedicalTest> tests = medicalTestRepository.findByCategoryAndActiveTrue("RADIOLOGY");

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Radiology");

            CellStyle headerStyle = createHeaderStyle(workbook);

            org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(0);
            String[] headers = {"Test Name", "Category", "Price", "Coverage Status"};
            for (int i = 0; i < headers.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;
            for (MedicalTest test : tests) {
                org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(test.getTestName() != null ? test.getTestName() : "");
                row.createCell(1).setCellValue(test.getCategory() != null ? test.getCategory() : "");
                row.createCell(2).setCellValue(test.getPrice() != null ? test.getPrice().doubleValue() : 0);
                row.createCell(3).setCellValue(test.getCoverageStatus() != null ? test.getCoverageStatus().name() : "");
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    public byte[] exportDiagnosesToExcel() throws IOException {
        List<MedicalDiagnosis> diagnoses = medicalDiagnosisRepository.findByActiveTrue();

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Diagnoses");

            CellStyle headerStyle = createHeaderStyle(workbook);

            org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(0);
            String[] headers = {"English Name", "Arabic Name", "Description"};
            for (int i = 0; i < headers.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;
            for (MedicalDiagnosis diagnosis : diagnoses) {
                org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(diagnosis.getEnglishName() != null ? diagnosis.getEnglishName() : "");
                row.createCell(1).setCellValue(diagnosis.getArabicName() != null ? diagnosis.getArabicName() : "");
                row.createCell(2).setCellValue(diagnosis.getDescription() != null ? diagnosis.getDescription() : "");
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    public byte[] exportProceduresToExcel() throws IOException {
        List<DoctorProcedure> procedures = doctorProcedureRepository.findByActiveTrue();

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Doctor Procedures");

            CellStyle headerStyle = createHeaderStyle(workbook);

            org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(0);
            String[] headers = {"Procedure Name", "Category", "Price", "Coverage Status"};
            for (int i = 0; i < headers.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;
            for (DoctorProcedure procedure : procedures) {
                org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(procedure.getProcedureName() != null ? procedure.getProcedureName() : "");
                row.createCell(1).setCellValue(procedure.getCategory() != null ? procedure.getCategory() : "");
                row.createCell(2).setCellValue(procedure.getPrice() != null ? procedure.getPrice().doubleValue() : 0);
                row.createCell(3).setCellValue(procedure.getCoverageStatus() != null ? procedure.getCoverageStatus().name() : "");
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    public byte[] exportPoliciesToExcel() throws IOException {
        List<Policy> policies = policyRepository.findAll();

        try (Workbook workbook = new XSSFWorkbook()) {
            // Create Policies sheet
            Sheet policySheet = workbook.createSheet("Policies");
            CellStyle headerStyle = createHeaderStyle(workbook);

            org.apache.poi.ss.usermodel.Row headerRow = policySheet.createRow(0);
            String[] policyHeaders = {"Policy No", "Name", "Description", "Start Date", "End Date", "Status", "Coverage Limit", "Deductible"};
            for (int i = 0; i < policyHeaders.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
                cell.setCellValue(policyHeaders[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;
            for (Policy policy : policies) {
                org.apache.poi.ss.usermodel.Row row = policySheet.createRow(rowNum++);
                row.createCell(0).setCellValue(policy.getPolicyNo() != null ? policy.getPolicyNo() : "");
                row.createCell(1).setCellValue(policy.getName() != null ? policy.getName() : "");
                row.createCell(2).setCellValue(policy.getDescription() != null ? policy.getDescription() : "");
                row.createCell(3).setCellValue(policy.getStartDate() != null ? policy.getStartDate().toString() : "");
                row.createCell(4).setCellValue(policy.getEndDate() != null ? policy.getEndDate().toString() : "");
                row.createCell(5).setCellValue(policy.getStatus() != null ? policy.getStatus().name() : "");
                row.createCell(6).setCellValue(policy.getCoverageLimit() != null ? policy.getCoverageLimit().doubleValue() : 0);
                row.createCell(7).setCellValue(policy.getDeductible() != null ? policy.getDeductible().doubleValue() : 0);
            }

            for (int i = 0; i < policyHeaders.length; i++) {
                policySheet.autoSizeColumn(i);
            }

            // Create Coverages sheet
            Sheet coverageSheet = workbook.createSheet("Coverages");

            org.apache.poi.ss.usermodel.Row coverageHeaderRow = coverageSheet.createRow(0);
            String[] coverageHeaders = {"Policy No", "Service Name", "Coverage Type", "Coverage %", "Max Limit", "Deductible", "Is Covered", "Emergency Eligible", "Requires Referral", "Gender", "Min Age", "Max Age", "Frequency Limit", "Frequency Period"};
            for (int i = 0; i < coverageHeaders.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = coverageHeaderRow.createCell(i);
                cell.setCellValue(coverageHeaders[i]);
                cell.setCellStyle(headerStyle);
            }

            int coverageRowNum = 1;
            for (Policy policy : policies) {
                if (policy.getCoverages() != null) {
                    for (Coverage coverage : policy.getCoverages()) {
                        org.apache.poi.ss.usermodel.Row row = coverageSheet.createRow(coverageRowNum++);
                        row.createCell(0).setCellValue(policy.getPolicyNo() != null ? policy.getPolicyNo() : "");
                        row.createCell(1).setCellValue(coverage.getServiceName() != null ? coverage.getServiceName() : "");
                        row.createCell(2).setCellValue(coverage.getCoverageType() != null ? coverage.getCoverageType().name() : "");
                        row.createCell(3).setCellValue(coverage.getCoveragePercent() != null ? coverage.getCoveragePercent().doubleValue() : 0);
                        row.createCell(4).setCellValue(coverage.getMaxLimit() != null ? coverage.getMaxLimit().doubleValue() : 0);
                        row.createCell(5).setCellValue(coverage.getMinimumDeductible() != null ? coverage.getMinimumDeductible().doubleValue() : 0);
                        row.createCell(6).setCellValue(coverage.isCovered() ? "Yes" : "No");
                        row.createCell(7).setCellValue(coverage.isEmergencyEligible() ? "Yes" : "No");
                        row.createCell(8).setCellValue(coverage.isRequiresReferral() ? "Yes" : "No");
                        row.createCell(9).setCellValue(coverage.getAllowedGender() != null ? coverage.getAllowedGender().name() : "ALL");
                        row.createCell(10).setCellValue(coverage.getMinAge() != null ? coverage.getMinAge() : 0);
                        row.createCell(11).setCellValue(coverage.getMaxAge() != null ? coverage.getMaxAge() : 0);
                        row.createCell(12).setCellValue(coverage.getFrequencyLimit() != null ? coverage.getFrequencyLimit() : 0);
                        row.createCell(13).setCellValue(coverage.getFrequencyPeriod() != null ? coverage.getFrequencyPeriod().name() : "");
                    }
                }
            }

            for (int i = 0; i < coverageHeaders.length; i++) {
                coverageSheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    // ==================== PDF EXPORTS ====================

    public byte[] exportMedicinesToPdf() throws DocumentException {
        List<MedicinePrice> medicines = medicinePriceRepository.findByActiveTrue();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4.rotate());
        PdfWriter.getInstance(document, outputStream);

        document.open();

        // Add title
        com.lowagie.text.Font titleFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 18, com.lowagie.text.Font.BOLD);
        Paragraph title = new Paragraph("Medicine Price List", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);

        // Create table
        PdfPTable table = new PdfPTable(7);
        table.setWidthPercentage(100);

        // Add headers
        String[] headers = {"Drug Name", "Generic Name", "Composition", "Type", "Unit", "Price", "Coverage"};
        com.lowagie.text.Font headerFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 10, com.lowagie.text.Font.BOLD);
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
            cell.setBackgroundColor(new java.awt.Color(200, 200, 200));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(5);
            table.addCell(cell);
        }

        // Add data
        com.lowagie.text.Font dataFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 8);
        for (MedicinePrice medicine : medicines) {
            table.addCell(new Phrase(medicine.getDrugName() != null ? medicine.getDrugName() : "", dataFont));
            table.addCell(new Phrase(medicine.getGenericName() != null ? medicine.getGenericName() : "", dataFont));
            table.addCell(new Phrase(medicine.getComposition() != null ? medicine.getComposition() : "", dataFont));
            table.addCell(new Phrase(medicine.getType() != null ? medicine.getType() : "", dataFont));
            table.addCell(new Phrase(medicine.getUnit() != null ? medicine.getUnit() : "", dataFont));
            table.addCell(new Phrase(medicine.getPrice() != null ? medicine.getPrice().toString() : "0", dataFont));
            table.addCell(new Phrase(medicine.getCoverageStatus() != null ? medicine.getCoverageStatus().name() : "", dataFont));
        }

        document.add(table);
        document.close();

        return outputStream.toByteArray();
    }

    public byte[] exportLabTestsToPdf() throws DocumentException {
        List<MedicalTest> tests = medicalTestRepository.findByCategoryAndActiveTrue("LAB");
        return exportTestsToPdf(tests, "Lab Tests");
    }

    public byte[] exportRadiologyToPdf() throws DocumentException {
        List<MedicalTest> tests = medicalTestRepository.findByCategoryAndActiveTrue("RADIOLOGY");
        return exportTestsToPdf(tests, "Radiology Tests");
    }

    private byte[] exportTestsToPdf(List<MedicalTest> tests, String titleText) throws DocumentException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, outputStream);

        document.open();

        com.lowagie.text.Font titleFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 18, com.lowagie.text.Font.BOLD);
        Paragraph title = new Paragraph(titleText, titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);

        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);

        String[] headers = {"Test Name", "Category", "Price", "Coverage"};
        com.lowagie.text.Font headerFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 10, com.lowagie.text.Font.BOLD);
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
            cell.setBackgroundColor(new java.awt.Color(200, 200, 200));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(5);
            table.addCell(cell);
        }

        com.lowagie.text.Font dataFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 9);
        for (MedicalTest test : tests) {
            table.addCell(new Phrase(test.getTestName() != null ? test.getTestName() : "", dataFont));
            table.addCell(new Phrase(test.getCategory() != null ? test.getCategory() : "", dataFont));
            table.addCell(new Phrase(test.getPrice() != null ? test.getPrice().toString() : "0", dataFont));
            table.addCell(new Phrase(test.getCoverageStatus() != null ? test.getCoverageStatus().name() : "", dataFont));
        }

        document.add(table);
        document.close();

        return outputStream.toByteArray();
    }

    public byte[] exportDiagnosesToPdf() throws DocumentException {
        List<MedicalDiagnosis> diagnoses = medicalDiagnosisRepository.findByActiveTrue();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, outputStream);

        document.open();

        com.lowagie.text.Font titleFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 18, com.lowagie.text.Font.BOLD);
        Paragraph title = new Paragraph("Medical Diagnoses", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);

        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(100);

        String[] headers = {"English Name", "Arabic Name", "Description"};
        com.lowagie.text.Font headerFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 10, com.lowagie.text.Font.BOLD);
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
            cell.setBackgroundColor(new java.awt.Color(200, 200, 200));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(5);
            table.addCell(cell);
        }

        com.lowagie.text.Font dataFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 9);
        for (MedicalDiagnosis diagnosis : diagnoses) {
            table.addCell(new Phrase(diagnosis.getEnglishName() != null ? diagnosis.getEnglishName() : "", dataFont));
            table.addCell(new Phrase(diagnosis.getArabicName() != null ? diagnosis.getArabicName() : "", dataFont));
            table.addCell(new Phrase(diagnosis.getDescription() != null ? diagnosis.getDescription() : "", dataFont));
        }

        document.add(table);
        document.close();

        return outputStream.toByteArray();
    }

    public byte[] exportProceduresToPdf() throws DocumentException {
        List<DoctorProcedure> procedures = doctorProcedureRepository.findByActiveTrue();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, outputStream);

        document.open();

        com.lowagie.text.Font titleFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 18, com.lowagie.text.Font.BOLD);
        Paragraph title = new Paragraph("Doctor Procedures", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);

        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);

        String[] headers = {"Procedure Name", "Category", "Price", "Coverage"};
        com.lowagie.text.Font headerFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 10, com.lowagie.text.Font.BOLD);
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
            cell.setBackgroundColor(new java.awt.Color(200, 200, 200));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(5);
            table.addCell(cell);
        }

        com.lowagie.text.Font dataFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 9);
        for (DoctorProcedure procedure : procedures) {
            table.addCell(new Phrase(procedure.getProcedureName() != null ? procedure.getProcedureName() : "", dataFont));
            table.addCell(new Phrase(procedure.getCategory() != null ? procedure.getCategory() : "", dataFont));
            table.addCell(new Phrase(procedure.getPrice() != null ? procedure.getPrice().toString() : "0", dataFont));
            table.addCell(new Phrase(procedure.getCoverageStatus() != null ? procedure.getCoverageStatus().name() : "", dataFont));
        }

        document.add(table);
        document.close();

        return outputStream.toByteArray();
    }

    public byte[] exportPoliciesToPdf() throws DocumentException {
        List<Policy> policies = policyRepository.findAll();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4.rotate());
        PdfWriter.getInstance(document, outputStream);

        document.open();

        // Title
        com.lowagie.text.Font titleFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 18, com.lowagie.text.Font.BOLD);
        Paragraph title = new Paragraph("Insurance Policies & Coverages", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);

        // Policies Table
        com.lowagie.text.Font sectionFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 14, com.lowagie.text.Font.BOLD);
        Paragraph policiesTitle = new Paragraph("Policies", sectionFont);
        policiesTitle.setSpacingAfter(10);
        document.add(policiesTitle);

        PdfPTable policyTable = new PdfPTable(6);
        policyTable.setWidthPercentage(100);

        String[] policyHeaders = {"Policy No", "Name", "Status", "Coverage Limit", "Deductible", "Period"};
        com.lowagie.text.Font headerFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 9, com.lowagie.text.Font.BOLD);
        for (String header : policyHeaders) {
            PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
            cell.setBackgroundColor(new java.awt.Color(200, 200, 200));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(5);
            policyTable.addCell(cell);
        }

        com.lowagie.text.Font dataFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 8);
        for (Policy policy : policies) {
            policyTable.addCell(new Phrase(policy.getPolicyNo() != null ? policy.getPolicyNo() : "", dataFont));
            policyTable.addCell(new Phrase(policy.getName() != null ? policy.getName() : "", dataFont));
            policyTable.addCell(new Phrase(policy.getStatus() != null ? policy.getStatus().name() : "", dataFont));
            policyTable.addCell(new Phrase(policy.getCoverageLimit() != null ? policy.getCoverageLimit().toString() : "0", dataFont));
            policyTable.addCell(new Phrase(policy.getDeductible() != null ? policy.getDeductible().toString() : "0", dataFont));
            String period = (policy.getStartDate() != null ? policy.getStartDate().toString() : "") + " - " +
                           (policy.getEndDate() != null ? policy.getEndDate().toString() : "");
            policyTable.addCell(new Phrase(period, dataFont));
        }

        document.add(policyTable);

        // Coverages Table
        document.add(new Paragraph("\n"));
        Paragraph coveragesTitle = new Paragraph("Coverages", sectionFont);
        coveragesTitle.setSpacingAfter(10);
        document.add(coveragesTitle);

        PdfPTable coverageTable = new PdfPTable(8);
        coverageTable.setWidthPercentage(100);

        String[] coverageHeaders = {"Policy No", "Service", "Type", "Coverage %", "Max Limit", "Deductible", "Covered", "Emergency"};
        for (String header : coverageHeaders) {
            PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
            cell.setBackgroundColor(new java.awt.Color(200, 200, 200));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(4);
            coverageTable.addCell(cell);
        }

        for (Policy policy : policies) {
            if (policy.getCoverages() != null) {
                for (Coverage coverage : policy.getCoverages()) {
                    coverageTable.addCell(new Phrase(policy.getPolicyNo() != null ? policy.getPolicyNo() : "", dataFont));
                    coverageTable.addCell(new Phrase(coverage.getServiceName() != null ? coverage.getServiceName() : "", dataFont));
                    coverageTable.addCell(new Phrase(coverage.getCoverageType() != null ? coverage.getCoverageType().name() : "", dataFont));
                    coverageTable.addCell(new Phrase(coverage.getCoveragePercent() != null ? coverage.getCoveragePercent().toString() + "%" : "0%", dataFont));
                    coverageTable.addCell(new Phrase(coverage.getMaxLimit() != null ? coverage.getMaxLimit().toString() : "0", dataFont));
                    coverageTable.addCell(new Phrase(coverage.getMinimumDeductible() != null ? coverage.getMinimumDeductible().toString() : "0", dataFont));
                    coverageTable.addCell(new Phrase(coverage.isCovered() ? "Yes" : "No", dataFont));
                    coverageTable.addCell(new Phrase(coverage.isEmergencyEligible() ? "Yes" : "No", dataFont));
                }
            }
        }

        document.add(coverageTable);
        document.close();

        return outputStream.toByteArray();
    }

    // ==================== HELPER METHODS ====================

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
}
