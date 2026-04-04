package poly.edu.vantix_hrm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import poly.edu.vantix_hrm.dto.payrollbatch.PayrollBatchDTO;
import poly.edu.vantix_hrm.entity.PayrollBatch;
import poly.edu.vantix_hrm.entity.Salary;
import poly.edu.vantix_hrm.repository.PayrollBatchRepository;
import poly.edu.vantix_hrm.repository.SalariesRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PayrollBatchService {

    private final PayrollBatchRepository payrollBatchRepository;
    private final SalariesRepository salariesRepository;

    /**
     * Lấy danh sách toàn bộ các đợt chốt lương, map sang DTO
     */
    public List<PayrollBatchDTO> getAllPayrollBatches() {
        List<PayrollBatch> batches = payrollBatchRepository.findAllBatchesDesc();
        return batches.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Chốt bảng lương tháng (Gom dữ liệu gửi Giám đốc)
     */
    public PayrollBatchDTO finalizePayrollBatch(int month, int year) {
        List<Salary> salaries = salariesRepository.findByMonthAndYear(month, year);
        if (salaries.isEmpty()) {
            throw new RuntimeException("Không có dữ liệu bảng lương cho tháng " + month + "/" + year);
        }

        int totalEmployees = salaries.size();
        BigDecimal totalNetAmount = salaries.stream()
                .map(Salary::getNetSalary)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Optional<PayrollBatch> existingBatchOpt = payrollBatchRepository.findByMonthAndYear(month, year);

        PayrollBatch batch;
        if (existingBatchOpt.isPresent()) {
            batch = existingBatchOpt.get();

            if (batch.getStatus() == PayrollBatch.BatchStatus.APPROVED ||
                    batch.getStatus() == PayrollBatch.BatchStatus.COMPLETED) {
                throw new RuntimeException("Bảng lương tháng này đã được duyệt hoặc thanh toán, không thể chốt lại!");
            }

            batch.setTotalEmployees(totalEmployees);
            batch.setTotalNetAmount(totalNetAmount);
            batch.setStatus(PayrollBatch.BatchStatus.PENDING);
        } else {
            batch = PayrollBatch.builder()
                    .batchName("Bảng lương tháng " + String.format("%02d/%d", month, year))
                    .salaryMonth(LocalDate.of(year, month, 1))
                    .totalEmployees(totalEmployees)
                    .totalNetAmount(totalNetAmount)
                    .status(PayrollBatch.BatchStatus.PENDING)
                    .build();
        }

        // Lưu vào database và map kết quả sang DTO trả về cho Vue
        PayrollBatch savedBatch = payrollBatchRepository.save(batch);
        return mapToDTO(savedBatch);
    }

    /**
     * Hàm helper để map Entity -> DTO
     */
    private PayrollBatchDTO mapToDTO(PayrollBatch batch) {
        return PayrollBatchDTO.builder()
                .batchId(batch.getBatchId())
                .batchName(batch.getBatchName())
                .salaryMonth(batch.getSalaryMonth())
                .totalEmployees(batch.getTotalEmployees())
                .totalNetAmount(batch.getTotalNetAmount())
                .approvedBy(batch.getApprovedBy())
                .status(batch.getStatus() != null ? batch.getStatus().name() : null)
                .createdAt(batch.getCreatedAt())
                .build();
    }
}