package poly.edu.vantix_hrm.helper;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import poly.edu.vantix_hrm.dto.salaries.ResponseSalaryTableDTO;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class SalaryExcelExporter {

    public static ByteArrayInputStream salariesToExcel(List<ResponseSalaryTableDTO> salaries) {
        String[] HEADERS = {"STT", "Mã NV", "Họ Tên", "Phòng Ban", "Tháng", "Lương Cơ Bản", "Ngày Công (C)", "Ngày Công (TT)", "Phụ Cấp", "Thưởng", "Trừ BHXH", "Trừ BHYT", "Trừ BHTN", "Thuế TNCN", "Tổng Thu Nhập", "Tổng Khấu Trừ", "Thực Nhận (NET)", "Trạng Thái", "Ghi Chú"};

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Bảng Lương");

            // 1. STYLE CHO TIÊU ĐỀ CHÍNH (TITLE)
            CellStyle titleStyle = workbook.createCellStyle();
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 16); // Chữ to
            titleFont.setColor(IndexedColors.DARK_BLUE.getIndex()); // Màu xanh đậm
            titleStyle.setFont(titleFont);
            titleStyle.setAlignment(HorizontalAlignment.CENTER); // Căn giữa ngang
            titleStyle.setVerticalAlignment(VerticalAlignment.CENTER); // Căn giữa dọc

            // 2. STYLE CHO HEADER (Cột tiêu đề)
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex()); // Chữ trắng
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.SEA_GREEN.getIndex()); // Nền xanh lá
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            // 3. STYLE CHO SỐ TIỀN
            CellStyle currencyStyle = workbook.createCellStyle();
            DataFormat format = workbook.createDataFormat();
            currencyStyle.setDataFormat(format.getFormat("#,##0"));
            currencyStyle.setBorderTop(BorderStyle.THIN);
            currencyStyle.setBorderBottom(BorderStyle.THIN);
            currencyStyle.setBorderLeft(BorderStyle.THIN);
            currencyStyle.setBorderRight(BorderStyle.THIN);
            currencyStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            // 4. STYLE CHO TEXT BÌNH THƯỜNG
            CellStyle textStyle = workbook.createCellStyle();
            textStyle.setBorderTop(BorderStyle.THIN);
            textStyle.setBorderBottom(BorderStyle.THIN);
            textStyle.setBorderLeft(BorderStyle.THIN);
            textStyle.setBorderRight(BorderStyle.THIN);
            textStyle.setVerticalAlignment(VerticalAlignment.CENTER);


            // --- VẼ GIAO DIỆN ---

            // Dòng 0: Tiêu đề báo cáo
            Row titleRow = sheet.createRow(0);
            titleRow.setHeightInPoints(35); // Chiều cao dòng title
            Cell titleCell = titleRow.createCell(0);

            // Lấy tháng năm từ phần tử đầu tiên để làm Tiêu đề
            String titleText = "BÁO CÁO TỔNG HỢP BẢNG LƯƠNG NHÂN VIÊN";
            if (!salaries.isEmpty() && salaries.get(0).getSalaryMonth() != null) {
                LocalDate date = salaries.get(0).getSalaryMonth();
                titleText = "BÁO CÁO BẢNG LƯƠNG THÁNG " + date.getMonthValue() + " NĂM " + date.getYear();
            }
            titleCell.setCellValue(titleText);
            titleCell.setCellStyle(titleStyle);

            // Merge Cells (Gộp ô từ cột 0 đến cột cuối cùng)
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, HEADERS.length - 1));

            // Dòng 1: Dòng trống để tạo khoảng cách cho đẹp (Không cần code gì, bỏ qua Dòng 1)

            // Dòng 2: Header của bảng
            int headerRowIndex = 2;
            Row headerRow = sheet.createRow(headerRowIndex);
            headerRow.setHeightInPoints(25);
            for (int col = 0; col < HEADERS.length; col++) {
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(HEADERS[col]);
                cell.setCellStyle(headerStyle);
            }

            // Dòng 3 trở đi: Đổ dữ liệu
            int rowIdx = 3;
            for (ResponseSalaryTableDTO salary : salaries) {
                Row row = sheet.createRow(rowIdx++);

                // Cột 0: STT
                createCell(row, 0, rowIdx - 3, textStyle);
                createCell(row, 1, "EMP" + salary.getEmployeeId(), textStyle);
                createCell(row, 2, salary.getEmployeeName(), textStyle);
                createCell(row, 3, salary.getDepartment(), textStyle);

                // Ngày tháng
                String monthStr = salary.getSalaryMonth() != null ? salary.getSalaryMonth().getMonthValue() + "/" + salary.getSalaryMonth().getYear() : "";
                createCell(row, 4, monthStr, textStyle);

                createCell(row, 5, salary.getBaseSalarySnapshot(), currencyStyle);
                createCell(row, 6, salary.getStandardWorkDays(), textStyle);
                createCell(row, 7, salary.getActualWorkDays(), textStyle);
                createCell(row, 8, salary.getAllowance(), currencyStyle);
                createCell(row, 9, salary.getBonus(), currencyStyle);
                createCell(row, 10, salary.getBhxhAmount(), currencyStyle);
                createCell(row, 11, salary.getBhytAmount(), currencyStyle);
                createCell(row, 12, salary.getBhtnAmount(), currencyStyle);
                createCell(row, 13, salary.getTaxAmount(), currencyStyle);
                createCell(row, 14, salary.getTotalIncome(), currencyStyle);
                createCell(row, 15, salary.getTotalDeduction(), currencyStyle);
                createCell(row, 16, salary.getNetSalary(), currencyStyle);
                createCell(row, 17, salary.getStatus(), textStyle);
                createCell(row, 18, salary.getNote() != null ? salary.getNote() : "", textStyle);
            }

            // --- CÁC TÍNH NĂNG NÂNG CAO ---

            // 1. Thêm AutoFilter (Bộ lọc) cho dòng Header (Từ dòng 2 đến dòng cuối cùng)
            sheet.setAutoFilter(new CellRangeAddress(headerRowIndex, rowIdx - 1, 0, HEADERS.length - 1));

            // 2. Cố định dòng (Freeze Panes): Cố định 3 dòng đầu (Tiêu đề + Header) và 3 cột đầu (STT, Mã, Tên)
            // Lệnh tạo: sheet.createFreezePane(cột_muốn_chốt, dòng_muốn_chốt)
            sheet.createFreezePane(3, headerRowIndex + 1);

            // 3. Auto-size các cột cho vừa chữ (Có cộng thêm một chút khoảng trống cho đẹp)
            for (int i = 0; i < HEADERS.length; i++) {
                sheet.autoSizeColumn(i);
                // Nới rộng thêm một chút sau khi autoSize để nhìn thoáng hơn
                int currentWidth = sheet.getColumnWidth(i);
                sheet.setColumnWidth(i, currentWidth + 1024);
            }

            // Xuất ra stream
            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());

        } catch (IOException e) {
            throw new RuntimeException("Lỗi khi xuất file Excel bảng lương: " + e.getMessage());
        }
    }

    // Các hàm helper nhỏ để set dữ liệu an toàn tránh NullPointerException
    private static void createCell(Row row, int colIndex, Object value, CellStyle style) {
        Cell cell = row.createCell(colIndex);
        if (value == null) {
            cell.setCellValue("");
        } else if (value instanceof String) {
            cell.setCellValue((String) value);
        } else if (value instanceof Integer) {
            cell.setCellValue((Integer) value);
        } else if (value instanceof BigDecimal) {
            cell.setCellValue(((BigDecimal) value).doubleValue());
        }
        cell.setCellStyle(style);
    }
}