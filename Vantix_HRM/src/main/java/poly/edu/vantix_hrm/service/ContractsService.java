package poly.edu.vantix_hrm.service;

import poly.edu.vantix_hrm.entity.Contract;
import poly.edu.vantix_hrm.entity.Employee;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface ContractsService {
    // Lấy toàn bộ danh sách hợp đồng
    List<Contract> getAllContracts();

    // Lưu Contract
    Contract saveContract(Employee employee, Contract.Type type, String position, LocalDate startDate, BigDecimal baseSalary, Contract.ContractStatus status);

    // Kiểm tra Contract của nhân viên
    void validateEmployeeContractEligibility(Integer employeeId);
}
