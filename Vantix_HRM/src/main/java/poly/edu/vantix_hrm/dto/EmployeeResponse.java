package poly.edu.vantix_hrm.dto;
import lombok.Data;
import poly.edu.vantix_hrm.entity.Employee;

import java.time.LocalDate;

@Data
public class EmployeeResponse {

    private Integer id;
    private String fullName;
    private Employee.Gender gender;
    private LocalDate birthDate;
    private String phone;
    private String address;
    private Employee.WorkStatus workStatus;

    private SimpleDepartmentDTO department;
    private SimplePositionDTO position;
}




