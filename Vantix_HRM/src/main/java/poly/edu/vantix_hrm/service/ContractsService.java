package poly.edu.vantix_hrm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import poly.edu.vantix_hrm.dto.contract.ContractResponseDTO;
import poly.edu.vantix_hrm.entity.Contract;
import poly.edu.vantix_hrm.repository.ContractAnnexesRepository;
import poly.edu.vantix_hrm.repository.ContractsRepository;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ContractsService {

    private final ContractsRepository contractsRepository;
    private final ContractAnnexesRepository contractAnnexesRepository;

    // Lấy danh sách Hợp Đồng
    public List<ContractResponseDTO> getAllContracts() {
        // 1. Lấy dữ liệu từ DB
        List<Contract> contracts = contractsRepository.findAllContractsWithEmployee();

        // 2. Chuyển đổi Entity sang DTO
        return contracts.stream().map(c -> ContractResponseDTO.builder().contractId(c.getContractId()).employeeId(c.getEmployee().getEmployeeId()).fullName(c.getEmployee().getFullName()).type(c.getType()).startDate(c.getStartDate()).endDate(c.getEndDate()).position(c.getPosition()).baseSalary(c.getBaseSalary()).status(c.getStatus()).build()).collect(Collectors.toList());
    }

    // Kiểm tra trạng thái Hợp Đồng của nhân viên
    public void validateEmployeeContractEligibility(Integer employeeId) {
        // Lấy toàn bộ Hợp Đồng của nhân viên kèm trạng thái phiếu
        List<Contract> contracts = contractsRepository.findByEmployee_EmployeeId(employeeId);

        // Kiểm tra nếu có Hợp Đồng có trạng thái Active
        boolean hasActiveContract = contracts.stream().anyMatch(c -> c.getStatus() == Contract.ContractStatus.ACTIVE);

        // Hợp Đồng có trạng thái Active thì báo lỗi
        if (hasActiveContract) {
            throw new RuntimeException("Nhân viên đang có hợp đồng ACTIVE. Không thể tạo thêm!");
        }
    }

    // Tính EndDate dựa trên StartDate và Type
    public LocalDate calculateEndDateByType(LocalDate startDate, Contract.Type type) {
        // Kiểm tra tránh null
        if (startDate == null || type == null) {
            throw new RuntimeException("StartDate hoặc Type không hợp lệ.");
        }

        // Tính và trả về EndDate
        return switch (type) {
            case YEAR_1 -> startDate.plusYears(1);
            case YEAR_3 -> startDate.plusYears(3);
            case INDEFINITE -> null;
        };
    }

    // Lưu Hợp Đồng
    public Contract saveContract(Contract contract) {
        if (contract == null) {
            throw new RuntimeException("Dữ liệu hợp đồng không hợp lệ.");
        }
        return contractsRepository.save(contract);
    }

    // Tìm Hơp Đồng bằng mã
    public Contract findById(Integer contractId) {
        return contractsRepository.findById(contractId).orElseThrow(() -> new RuntimeException("Không tìm thấy hợp đồng ID: " + contractId));
    }

    // Hàm tìm xem Hợp Đồng đó có Phụ Lục không
    public void checkContractAnnexes(Contract contract) {
        // Kiểm tra null tránh lỗi
        if (contract == null || contract.getContractId() == null) {
            throw new RuntimeException("Hợp đồng không hợp lệ!");
        }

        // Gọi query để đếm số lượng phụ lục
        long annexCount = contractAnnexesRepository.countAnnexesByContractId(contract.getContractId());

        // Nếu có ít nhất 1 phụ lục thì throw lỗi chặn lại luôn
        if (annexCount > 0) {
            String msg = "Không thể xóa Hợp Đồng ID " + contract.getContractId() + " vì có " + annexCount + " phụ lục đi kèm.";
            throw new RuntimeException(msg);
        }
    }


    // ================= DELETE =================
    public void deleteContract(Integer contractId) {
        Contract contract = findById(contractId);

        if (contract.getStatus() == Contract.ContractStatus.ACTIVE) {
            throw new RuntimeException("Không thể xóa hợp đồng đang ACTIVE.");
        }

        contractsRepository.delete(contract);
    }

    // ================= UPDATE STATUS =================
    public Contract updateContractStatus(Integer contractId) {

        Contract contract = findById(contractId);

        if (contract.getStatus() == Contract.ContractStatus.ACTIVE) {

            contract.setStatus(Contract.ContractStatus.EXPIRED);
            return contractsRepository.save(contract);
        }

        if (contract.getStatus() == Contract.ContractStatus.EXPIRED) {

            if (contract.getType() != Contract.Type.INDEFINITE && contract.getEndDate() != null) {

                LocalDate currentDate = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));

                if (currentDate.isAfter(contract.getEndDate())) {
                    throw new RuntimeException("Không thể khôi phục hợp đồng đã hết hạn (" + contract.getEndDate() + ")");
                }
            }

            contract.setStatus(Contract.ContractStatus.ACTIVE);
            return contractsRepository.save(contract);
        }

        throw new RuntimeException("Trạng thái hợp đồng không hợp lệ.");
    }
}