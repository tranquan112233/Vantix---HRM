package poly.edu.vantix_hrm.dto.employee;

import jakarta.validation.constraints.*;
import lombok.Data;
import poly.edu.vantix_hrm.entity.Employee;

import java.time.LocalDate;

@Data
public class EmployeeUpdateRequest {

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotNull(message = "Gender is required")
    private Employee.Gender gender;

    @NotNull(message = "Birth date is required")
    @Past(message = "Birth date must be in the past")
    private LocalDate birthDate;

    @NotBlank(message = "Phone is required")
    private String phone;

    @NotBlank(message = "Address is required")
    private String address;

    @NotNull(message = "Department is required")
    private Integer departmentId;

    @NotNull(message = "Position is required")
    private Integer positionId;

    @NotNull(message = "Work status is required")
    private Employee.WorkStatus workStatus;
}