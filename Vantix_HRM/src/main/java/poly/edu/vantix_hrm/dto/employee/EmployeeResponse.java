package poly.edu.vantix_hrm.dto.employee;

import lombok.Builder;
import lombok.Data;
import poly.edu.vantix_hrm.entity.Employee;

import java.time.LocalDate;

@Data
@Builder
public class EmployeeResponse {

    private Integer employeeId;

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