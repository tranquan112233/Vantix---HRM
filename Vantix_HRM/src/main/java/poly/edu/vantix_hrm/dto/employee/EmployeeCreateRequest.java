package poly.edu.vantix_hrm.dto.employee;

import jakarta.validation.constraints.*;
import lombok.Data;
import poly.edu.vantix_hrm.entity.Employee;

import java.time.LocalDate;

@Data
public class EmployeeCreateRequest {

    // ================= USER =================

    @NotBlank(message = "Username is required")
    private String username;

    @Email(message = "Invalid email")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @NotNull(message = "Role is required")
    private Integer roleId;

    // ================= EMPLOYEE =================

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

    private Employee.WorkStatus workStatus = Employee.WorkStatus.WORKING;
}