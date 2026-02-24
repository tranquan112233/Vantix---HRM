package poly.edu.vantix_hrm.service.Impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import poly.edu.vantix_hrm.repository.ContractsRepository;
import poly.edu.vantix_hrm.entity.Contract;
import poly.edu.vantix_hrm.entity.Employee;
import poly.edu.vantix_hrm.service.ContractsService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class ContractsServiceImpl implements ContractsService {

    @Autowired
    private ContractsRepository contractsRepository;

    @Override
    public List<Contract> getAllContracts() {
        return contractsRepository.findAll();
    }

    @Override
    public Contract saveContract(Employee employee, Contract.Type type, String position, LocalDate startDate, BigDecimal baseSalary, Contract.ContractStatus status) {
        try {
            Contract ct = new Contract();
            ct.setEmployee(employee);
            ct.setPosition(position);
            ct.setType(type);
            ct.setStartDate(startDate);
            ct.setBaseSalary(baseSalary);
            ct.setStatus(status != null ? status : Contract.ContractStatus.ACTIVE);

            if (startDate != null) {
                if (type == Contract.Type.YEAR_1) {
                    ct.setEndDate(startDate.plusYears(1));
                } else if (type == Contract.Type.YEAR_3) {
                    ct.setEndDate(startDate.plusYears(3));
                } else if (type == Contract.Type.INDEFINITE) {
                    ct.setEndDate(null);
                }
            }

            return contractsRepository.save(ct);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi lưu hợp đồng: " + e.getMessage());
        }
    }

    @Override
    public void validateEmployeeContractEligibility(Integer employeeId) {
        // Lấy danh sách lịch sử hợp đồng của nhân viên này
        List<Contract> contracts = contractsRepository.findByEmployee_EmployeeId(employeeId);

        // GIAI ĐOẠN 1: Kiểm tra xem nhân viên đã từng có hợp đồng nào chưa
        if (!contracts.isEmpty()) {

            // GIAI ĐOẠN 2: Nếu đã từng có, kiểm tra xem có cái nào đang ACTIVE không
            boolean hasActiveContract = contracts.stream()
                    .anyMatch(c -> c.getStatus() == Contract.ContractStatus.ACTIVE);

            if (hasActiveContract) {
                // Nếu có hợp đồng ACTIVE -> Chặn lại và ném lỗi
                throw new RuntimeException("Nhân viên này hiện đang có hợp đồng còn hiệu lực. Không thể tạo thêm!");
            }

            // Nếu code chạy xuống được đây có nghĩa là họ có hợp đồng, nhưng tất cả đều đã EXPIRED -> Cho phép tạo mới
        }
        // Nếu contracts.isEmpty() -> Nhân viên mới tinh chưa có hợp đồng -> Cho phép tạo mới
    }
}