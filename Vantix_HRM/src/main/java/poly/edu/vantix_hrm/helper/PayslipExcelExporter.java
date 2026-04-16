package poly.edu.vantix_hrm.helper;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import poly.edu.vantix_hrm.dto.salaries.ResponseSalaryTableDTO;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;

public class PayslipExcelExporter {

    public static ByteArrayInputStream exportPayslip(ResponseSalaryTableDTO salary) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Phiếu Lương");

            sheet.setColumnWidth(0, 7500); // Cột Khoản mục
            sheet.setColumnWidth(1, 6000); // Cột Số tiền

            // --- STYLES ---
            CellStyle titleStyle = workbook.createCellStyle();
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 16);
            titleFont.setColor(IndexedColors.DARK_BLUE.getIndex());
            titleStyle.setFont(titleFont);
            titleStyle.setAlignment(HorizontalAlignment.CENTER);

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.SEA_GREEN.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);

            CellStyle labelStyle = workbook.createCellStyle();
            Font boldFont = workbook.createFont();
            boldFont.setBold(true);
            labelStyle.setFont(boldFont);

            CellStyle currencyStyle = workbook.createCellStyle();
            DataFormat format = workbook.createDataFormat();
            currencyStyle.setDataFormat(format.getFormat("#,##0"));
            currencyStyle.setAlignment(HorizontalAlignment.RIGHT);

            CellStyle boldCurrencyStyle = workbook.createCellStyle();
            boldCurrencyStyle.setDataFormat(format.getFormat("#,##0"));
            boldCurrencyStyle.setFont(boldFont);
            boldCurrencyStyle.setAlignment(HorizontalAlignment.RIGHT);

            CellStyle netSalaryStyle = workbook.createCellStyle();
            Font netFont = workbook.createFont();
            netFont.setBold(true);
            netFont.setFontHeightInPoints((short) 15);
            netFont.setColor(IndexedColors.DARK_RED.getIndex());
            netSalaryStyle.setFont(netFont);
            netSalaryStyle.setDataFormat(format.getFormat("#,##0"));
            netSalaryStyle.setAlignment(HorizontalAlignment.RIGHT);

            int rowIdx = 0;

            // Title
            Row rowTitle = sheet.createRow(rowIdx++);
            rowTitle.setHeightInPoints(30);
            Cell cellTitle = rowTitle.createCell(0);
            cellTitle.setCellValue("CHI TIẾT PHIẾU LƯƠNG");
            cellTitle.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 1));

            createRow(sheet, rowIdx++, "Nhân viên:", salary.getEmployeeName(), labelStyle, null);
            createRow(sheet, rowIdx++, "Mã nhân viên:", "EMP" + salary.getEmployeeId(), labelStyle, null);

            String monthStr = salary.getSalaryMonth() != null ? "Tháng " + salary.getSalaryMonth().getMonthValue() + "/" + salary.getSalaryMonth().getYear() : "";
            createRow(sheet, rowIdx++, "Kỳ lương:", monthStr, labelStyle, null);

            rowIdx++; // Trống

            // THU NHẬP
            Row rowInc = sheet.createRow(rowIdx++);
            Cell cInc = rowInc.createCell(0);
            cInc.setCellValue("KHOẢN THU NHẬP");
            cInc.setCellStyle(headerStyle);
            rowInc.createCell(1).setCellStyle(headerStyle);

            createRow(sheet, rowIdx++, "Lương cơ bản (Base)", salary.getBaseSalarySnapshot(), null, currencyStyle);
            createRow(sheet, rowIdx++, "Ngày công thực tế", salary.getActualWorkDays() + " ngày", null, null);
            createRow(sheet, rowIdx++, "Phụ cấp (Allowance)", salary.getAllowance(), null, currencyStyle);
            createRow(sheet, rowIdx++, "Thưởng (Bonus)", salary.getBonus(), null, currencyStyle);
            createRow(sheet, rowIdx++, "TỔNG THU NHẬP", salary.getTotalIncome(), labelStyle, boldCurrencyStyle);

            rowIdx++; // Trống

            // KHẤU TRỪ
            Row rowDed = sheet.createRow(rowIdx++);
            Cell cDed = rowDed.createCell(0);
            cDed.setCellValue("KHOẢN KHẤU TRỪ");
            cDed.setCellStyle(headerStyle);
            rowDed.createCell(1).setCellStyle(headerStyle);

            createRow(sheet, rowIdx++, "Bảo hiểm Xã hội (BHXH)", salary.getBhxhAmount(), null, currencyStyle);
            createRow(sheet, rowIdx++, "Bảo hiểm Y tế (BHYT)", salary.getBhytAmount(), null, currencyStyle);
            createRow(sheet, rowIdx++, "Bảo hiểm Thất nghiệp (BHTN)", salary.getBhtnAmount(), null, currencyStyle);
            createRow(sheet, rowIdx++, "Thuế TNCN (Tax)", salary.getTaxAmount(), null, currencyStyle);
            createRow(sheet, rowIdx++, "TỔNG KHẤU TRỪ", salary.getTotalDeduction(), labelStyle, boldCurrencyStyle);

            rowIdx++; // Trống

            // THỰC NHẬN
            Row rowNet = sheet.createRow(rowIdx++);
            rowNet.setHeightInPoints(25);
            Cell cNetL = rowNet.createCell(0);
            cNetL.setCellValue("THỰC NHẬN (NET SALARY)");
            cNetL.setCellStyle(labelStyle);

            Cell cNetV = rowNet.createCell(1);
            if (salary.getNetSalary() != null) {
                cNetV.setCellValue(salary.getNetSalary().doubleValue());
            }
            cNetV.setCellStyle(netSalaryStyle);

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Lỗi khi xuất Phiếu lương: " + e.getMessage());
        }
    }

    private static void createRow(Sheet sheet, int rowIdx, String label, Object value, CellStyle labelStyle, CellStyle valueStyle) {
        Row row = sheet.createRow(rowIdx);
        Cell c0 = row.createCell(0);
        c0.setCellValue(label);
        if (labelStyle != null) c0.setCellStyle(labelStyle);

        Cell c1 = row.createCell(1);
        if (value == null) {
            c1.setCellValue("");
        } else if (value instanceof String) {
            c1.setCellValue((String) value);
        } else if (value instanceof BigDecimal) {
            c1.setCellValue(((BigDecimal) value).doubleValue());
        } else if (value instanceof Integer) {
            c1.setCellValue((Integer) value);
        }
        if (valueStyle != null) c1.setCellStyle(valueStyle);
    }
}