package poly.edu.vantix_hrm.service.Impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import poly.edu.vantix_hrm.entity.Contract;
import poly.edu.vantix_hrm.repository.ContractsRepository;
import poly.edu.vantix_hrm.service.ContractsService;

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
    public Contract saveContract(Contract contract) {
        try {
            return contractsRepository.save(contract);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi lưu hợp đồng: " + e.getMessage());
        }
    }

    @Override
    public void validateEmployeeContractEligibility(Integer employeeId) {
        List<Contract> contracts = contractsRepository.findByEmployee_EmployeeId(employeeId);
        if (!contracts.isEmpty()) {
            Boolean hasActiveContract = contracts.stream().anyMatch(c -> c.getStatus() == Contract.ContractStatus.ACTIVE);
            if (hasActiveContract) {
                String msgError = "Nhân viên này hiện đang có hợp đồng còn hiệu lực. Không thể tạo thêm!";
                throw new RuntimeException(msgError);
            }
        }
    }

    @Override
    public LocalDate calculateEndDateByType(LocalDate startDate, Contract.Type type) {
        if (startDate == null || type == null) return null;
        return switch (type) {
            case YEAR_1 -> startDate.plusYears(1);
            case YEAR_3 -> startDate.plusYears(3);
            case INDEFINITE -> null;
        };
    }

    @Override
    public Contract isContractValid(Integer contractId) {
        String msgError = "Không tìm thấy hợp đồng (" + contractId + ") trên hệ thống.";
        return contractsRepository.findById(contractId).orElseThrow(() -> new RuntimeException(msgError));
    }

    @Override
    public void validateContractDeletionEligibility(Contract contract) {
        if (contract.getStatus() == Contract.ContractStatus.ACTIVE) {
            String msgError = "Không thể xóa hợp đồng có trạng thái ACTIVE";
            throw new RuntimeException(msgError);
        }
    }

    @Override
    public void deleteContract(Integer contractId) {
        contractsRepository.deleteById(contractId);
    }

}