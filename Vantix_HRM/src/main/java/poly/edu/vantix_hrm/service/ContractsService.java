package poly.edu.vantix_hrm.service;

import poly.edu.vantix_hrm.entity.Contract;
import poly.edu.vantix_hrm.entity.Employee;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface ContractsService {
    // Lấy toàn bộ danh sách hợp đồng
    List<Contract> getAllContracts();

    // lưu hợp đồng
    Contract saveContract(Employee employee, Contract.Type type, String position, LocalDate startDate, BigDecimal baseSalary, Contract.ContractStatus status);

    // kiểm tra mã nhân viên này đã có hd chưa
    void validateEmployeeContractEligibility(Integer employeeId);
}
