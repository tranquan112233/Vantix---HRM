package poly.edu.vantix_hrm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import poly.edu.vantix_hrm.entity.PayrollBatch;
import poly.edu.vantix_hrm.entity.Salary;
import poly.edu.vantix_hrm.repository.PayrollBatchRepository;
import poly.edu.vantix_hrm.repository.SalariesRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class PayrollBatchService {

    private final PayrollBatchRepository payrollBatchRepository;
    private final SalariesRepository salariesRepository;

    /**
     * Chốt bảng lương tháng (Gom dữ liệu gửi Giám đốc)
     */
    public PayrollBatch finalizePayrollBatch(int month, int year) {
        // 1. Lấy danh sách lương chi tiết của tháng đó
        List<Salary> salaries = salariesRepository.findByMonthAndYear(month, year);
        if (salaries.isEmpty()) {
            throw new RuntimeException("Không có dữ liệu bảng lương cho tháng " + month + "/" + year);
        }

        // 2. Tính toán tổng quỹ lương và số lượng nhân viên
        int totalEmployees = salaries.size();
        BigDecimal totalNetAmount = salaries.stream()
                .map(Salary::getNetSalary)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 3. Kiểm tra xem đợt lương tháng này đã tạo chưa
        Optional<PayrollBatch> existingBatchOpt = payrollBatchRepository.findByMonthAndYear(month, year);

        PayrollBatch batch;
        if (existingBatchOpt.isPresent()) {
            batch = existingBatchOpt.get();

            // Nếu Giám đốc đã duyệt hoặc kế toán đã trả tiền thì không cho chốt lại (sửa lại)
            if (batch.getStatus() == PayrollBatch.BatchStatus.APPROVED ||
                    batch.getStatus() == PayrollBatch.BatchStatus.COMPLETED) {
                throw new RuntimeException("Bảng lương tháng này đã được duyệt hoặc thanh toán, không thể chốt lại!");
            }

            // Cập nhật lại số liệu nếu có thay đổi
            batch.setTotalEmployees(totalEmployees);
            batch.setTotalNetAmount(totalNetAmount);
            batch.setStatus(PayrollBatch.BatchStatus.PENDING); // Gửi chờ duyệt lại
        } else {
            // Chưa có thì tạo mới đợt chốt lương
            batch = PayrollBatch.builder()
                    .batchName("Bảng lương tháng " + String.format("%02d/%d", month, year))
                    .salaryMonth(LocalDate.of(year, month, 1)) // Lưu ngày mùng 1 của tháng đó
                    .totalEmployees(totalEmployees)
                    .totalNetAmount(totalNetAmount)
                    .status(PayrollBatch.BatchStatus.PENDING)
                    .build();
        }

        // Lưu vào DB
        return payrollBatchRepository.save(batch);
    }
}