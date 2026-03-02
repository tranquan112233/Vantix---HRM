package poly.edu.vantix_hrm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import poly.edu.vantix_hrm.entity.Contract;
import poly.edu.vantix_hrm.repository.ContractsRepository;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ContractsService {

    private final ContractsRepository contractsRepository;

    // ================= GET ALL =================
    public List<Contract> getAllContracts() {
        return contractsRepository.findAll();
    }

    // ================= SAVE =================
    public Contract saveContract(Contract contract) {
        if (contract == null) {
            throw new RuntimeException("Dữ liệu hợp đồng không hợp lệ.");
        }
        return contractsRepository.save(contract);
    }

    // ================= VALIDATE CREATE =================
    public void validateEmployeeContractEligibility(Integer employeeId) {
        List<Contract> contracts =
                contractsRepository.findByEmployee_EmployeeId(employeeId);

        boolean hasActiveContract = contracts.stream()
                .anyMatch(c -> c.getStatus() == Contract.ContractStatus.ACTIVE);

        if (hasActiveContract) {
            throw new RuntimeException(
                    "Nhân viên đang có hợp đồng ACTIVE. Không thể tạo thêm!"
            );
        }
    }

    // ================= CALCULATE END DATE =================
    public LocalDate calculateEndDateByType(LocalDate startDate, Contract.Type type) {
        if (startDate == null || type == null) {
            throw new RuntimeException("StartDate hoặc Type không hợp lệ.");
        }

        return switch (type) {
            case YEAR_1 -> startDate.plusYears(1);
            case YEAR_3 -> startDate.plusYears(3);
            case INDEFINITE -> null;
        };
    }

    // ================= FIND BY ID =================
    public Contract findById(Integer contractId) {
        return contractsRepository.findById(contractId)
                .orElseThrow(() ->
                        new RuntimeException("Không tìm thấy hợp đồng ID: " + contractId)
                );
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

            if (contract.getType() != Contract.Type.INDEFINITE
                    && contract.getEndDate() != null) {

                LocalDate currentDate =
                        LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));

                if (currentDate.isAfter(contract.getEndDate())) {
                    throw new RuntimeException(
                            "Không thể khôi phục hợp đồng đã hết hạn ("
                                    + contract.getEndDate() + ")"
                    );
                }
            }

            contract.setStatus(Contract.ContractStatus.ACTIVE);
            return contractsRepository.save(contract);
        }

        throw new RuntimeException("Trạng thái hợp đồng không hợp lệ.");
    }
}