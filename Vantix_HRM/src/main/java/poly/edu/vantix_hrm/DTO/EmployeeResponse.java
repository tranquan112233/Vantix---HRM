package poly.edu.vantix_hrm.DTO;
import lombok.Data;
import poly.edu.vantix_hrm.entity.Employee.Gender;
import poly.edu.vantix_hrm.entity.Employee.WorkStatus;

import java.time.LocalDate;

@Data
public class EmployeeResponse {

    private Integer id;
    private String fullName;
    private Gender gender;
    private LocalDate birthDate;
    private String phone;
    private String address;
    private WorkStatus workStatus;

    private SimpleDepartmentDTO department;
    private SimplePositionDTO position;
}




