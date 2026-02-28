package poly.edu.vantix_hrm.service;

import poly.edu.vantix_hrm.entity.Contract;

import java.time.LocalDate;
import java.util.List;

public interface ContractsService {
    // Lấy toàn bộ danh sách hợp đồng
    List<Contract> getAllContracts();

    // Lưu Contract
    Contract saveContract(Contract contract);

    // Kiểm tra Contract của nhân viên
    void validateEmployeeContractEligibility(Integer employeeId);

    // Lấy endDate theo Type hợp đồng
    LocalDate calculateEndDateByType(LocalDate startDate, Contract.Type type);

    // Kiểm tra contractId có tồn tại
    Contract isContractValid(Integer contractId);

    // Kiểm tra status của contract
    void validateContractDeletionEligibility(Contract contract);

    // Xóa contract
    void deleteContract(Integer contractId);

    // Update lại trạng thái status
    Contract updateContractStatus(Contract contract);

}
