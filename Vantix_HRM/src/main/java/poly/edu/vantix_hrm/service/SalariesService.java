package poly.edu.vantix_hrm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import poly.edu.vantix_hrm.dto.salaries.ResponseDepartmentDTO;
import poly.edu.vantix_hrm.dto.salaries.ResponseSalaryTableDTO;
import poly.edu.vantix_hrm.entity.Department;
import poly.edu.vantix_hrm.entity.Salary;
import poly.edu.vantix_hrm.repository.DepartmentRepository;
import poly.edu.vantix_hrm.repository.SalariesRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class SalariesService {

    private final DepartmentRepository departmentRepository;
    private final SalariesRepository salariesRepository;

    /**
     * Lấy danh sáchtên tất cả các phòng ban chưa bị xóa.
     *
     * @return Danh sách ResponseDepartmentDTO chứa tên phòng ban.
     */
    public List<ResponseDepartmentDTO> findDepartmentNames() {
        List<Department> departments = departmentRepository.findAll();
        if (departments.isEmpty()) {
            throw new RuntimeException("Không tìm dữ liệu Phòng Ban nào!");
        }
        return departments.stream().map(dept -> ResponseDepartmentDTO.builder().departmentName(dept.getName()).build()).toList();
    }


    /**
     * Lấy danh sách bảng lương theo tháng và năm, đồng thời map sang DTO.
     */
    public List<ResponseSalaryTableDTO> getSalariesByMonthAndYear(int month, int year) {
        // Lấy danh sách Salary từ database bằng hàm JPQL đã tối ưu (JOIN FETCH)
        List<Salary> salaries = salariesRepository.findByMonthAndYear(month, year);

        // Map từng phần tử Entity sang DTO và trả về list
        return salaries.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    /**
     * Hàm helper để thực hiện map từ Entity Salary sang DTO ResponseSalaryTableDTO
     */
    private ResponseSalaryTableDTO mapToDTO(Salary salary) {
        return ResponseSalaryTableDTO.builder().id(salary.getSalaryId()).employeeId(salary.getEmployee() != null ? salary.getEmployee().getId() : null).employeeName(salary.getEmployee() != null ? salary.getEmployee().getFullName() : null).department(salary.getEmployee() != null && salary.getEmployee().getDepartment() != null ? salary.getEmployee().getDepartment().getName() : null).salaryMonth(salary.getSalaryMonth()).baseSalarySnapshot(salary.getBaseSalarySnapshot()).standardWorkDays(salary.getStandardWorkDays())
                // actualWorkDays nay đã là Integer ở cả 2 bên nên truyền thẳng
                .actualWorkDays(salary.getActualWorkDays()).allowance(salary.getAllowance()).bonus(salary.getBonus()).bhxhAmount(salary.getBhxhAmount()).bhytAmount(salary.getBhytAmount()).bhtnAmount(salary.getBhtnAmount()).taxAmount(salary.getTaxAmount()).totalIncome(salary.getTotalIncome()).totalDeduction(salary.getTotalDeduction()).netSalary(salary.getNetSalary()).status(salary.getStatus() != null ? salary.getStatus().name() : null).note(salary.getNote()).build();
    }

    /**
     * Chuyển trạng thái toàn bộ bảng lương của một tháng sang PENDING.
     * * @return Số lượng bản ghi đã được cập nhật
     */
    public int submitAllSalariesToPending(int month, int year) {
        // Truyền thẳng Enum PENDING vào câu Query
        int updatedCount = salariesRepository.updateStatusByMonthAndYear(Salary.SalaryStatus.PENDING, month, year);
        if (updatedCount == 0) {
            throw new RuntimeException("Không có bản ghi bảng lương nào trong tháng " + month + "/" + year + " để cập nhật!");
        }
        return updatedCount;
    }
}