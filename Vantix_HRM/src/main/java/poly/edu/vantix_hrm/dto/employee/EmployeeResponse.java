package poly.edu.vantix_hrm.dto.employee;

import lombok.Builder;
import lombok.Data;
import poly.edu.vantix_hrm.entity.Employee;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class EmployeeResponse {

    private Integer employeeId;

    // === THÊM MẤY DÒNG NÀY VÀO ĐỂ HẾT ĐỎ ===
    private String username;
    private String email;
    private Integer roleId;
    private List<String> permissions;

    // === CÁC TRƯỜNG CŨ CỦA BẠN ===
    private String fullName;
    private Employee.Gender gender;
    private LocalDate birthDate;
    private String phone;
    private String address;
    private Integer departmentId;
    private String departmentName;
    private Integer positionId;
    private String positionName;
    private Employee.WorkStatus workStatus;
}