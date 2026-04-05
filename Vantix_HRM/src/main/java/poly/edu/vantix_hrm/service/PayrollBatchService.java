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
        return batches.stream().map(this::mapToDTO).collect(Collectors.toList());
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
        BigDecimal totalNetAmount = salaries.stream().map(Salary::getNetSalary).reduce(BigDecimal.ZERO, BigDecimal::add);

        // Lấy danh sách các đợt lương của tháng này (Phiếu mới nhất nằm ở index 0)
        List<PayrollBatch> existingBatches = payrollBatchRepository.findByMonthAndYear(month, year);

        if (!existingBatches.isEmpty()) {
            PayrollBatch latestBatch = existingBatches.get(0); // Lấy đợt gần nhất

            // Nếu đợt mới nhất KHÔNG PHẢI là REJECTED -> Chặn không cho tạo thêm
            if (latestBatch.getStatus() != PayrollBatch.BatchStatus.REJECTED) {
                String statusName = "";
                switch (latestBatch.getStatus()) {
                    case PENDING:
                        statusName = "Chờ phê duyệt (PENDING)";
                        break;
                    case APPROVED:
                        statusName = "Đã phê duyệt (APPROVED)";
                        break;
                    case COMPLETED:
                        statusName = "Đã chi trả (COMPLETED)";
                        break;
                    default:
                        statusName = latestBatch.getStatus().name();
                }
                throw new RuntimeException("Bảng lương tháng " + month + "/" + year + " đã tồn tại ở trạng thái [" + statusName + "]. Không thể tạo đợt chốt mới!");
            }
        }

        // Nếu chưa có, HOẶC đợt mới nhất đã bị REJECTED -> TẠO BẢN GHI MỚI
        PayrollBatch newBatch = PayrollBatch.builder().batchName("Bảng lương tháng " + String.format("%02d/%d", month, year)).salaryMonth(LocalDate.of(year, month, 1)).totalEmployees(totalEmployees).totalNetAmount(totalNetAmount).status(PayrollBatch.BatchStatus.PENDING).build();

        // Lưu bản ghi mới vào DB (Nó sẽ sinh ra 1 ID mới)
        PayrollBatch savedBatch = payrollBatchRepository.save(newBatch);

        // (Tùy chọn thêm cho chắc chắn): Đồng bộ lại toàn bộ trạng thái chi tiết Salary của nhân viên về PENDING
        salariesRepository.updateStatusByMonthAndYear(Salary.SalaryStatus.PENDING, month, year);

        return mapToDTO(savedBatch);
    }

    /**
     * Hàm helper để map Entity -> DTO
     */
    private PayrollBatchDTO mapToDTO(PayrollBatch batch) {
        return PayrollBatchDTO.builder().batchId(batch.getBatchId()).batchName(batch.getBatchName()).salaryMonth(batch.getSalaryMonth()).totalEmployees(batch.getTotalEmployees()).totalNetAmount(batch.getTotalNetAmount()).approvedBy(batch.getApprovedBy()).status(batch.getStatus() != null ? batch.getStatus().name() : null).createdAt(batch.getCreatedAt()).build();
    }

    /**
     * Giám đốc phê duyệt hoặc từ chối đợt chốt lương
     */
    public PayrollBatchDTO updateBatchStatus(Integer batchId, PayrollBatch.BatchStatus newStatus) {
        PayrollBatch batch = payrollBatchRepository.findById(batchId).orElseThrow(() -> new RuntimeException("Không tìm thấy đợt chốt lương với ID: " + batchId));

        if (batch.getStatus() == PayrollBatch.BatchStatus.COMPLETED) {
            throw new RuntimeException("Đợt lương này đã được chi trả, không thể thay đổi trạng thái!");
        }

        // 1. Cập nhật trạng thái đợt chốt lương
        batch.setStatus(newStatus);
        PayrollBatch savedBatch = payrollBatchRepository.save(batch);

        // 2. Đồng bộ trạng thái xuống các bản ghi Salary chi tiết
        int month = batch.getSalaryMonth().getMonthValue();
        int year = batch.getSalaryMonth().getYear();

        Salary.SalaryStatus detailStatus;
        if (newStatus == PayrollBatch.BatchStatus.APPROVED) {
            detailStatus = Salary.SalaryStatus.APPROVED; // Sếp duyệt -> Kế toán chuẩn bị tiền
        } else if (newStatus == PayrollBatch.BatchStatus.REJECTED) {
            detailStatus = Salary.SalaryStatus.DRAFT;    // Sếp từ chối -> Trả về DRAFT để kế toán sửa lại
        } else {
            detailStatus = Salary.SalaryStatus.PENDING;
        }

        // Gọi hàm update hàng loạt bên SalariesRepository
        salariesRepository.updateStatusByMonthAndYear(detailStatus, month, year);

        return mapToDTO(savedBatch);
    }
}