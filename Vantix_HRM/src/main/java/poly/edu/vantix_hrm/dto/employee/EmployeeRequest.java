package poly.edu.vantix_hrm.dto.employee;

import jakarta.validation.constraints.*;
import lombok.Data;
import poly.edu.vantix_hrm.entity.Employee.Gender;
import poly.edu.vantix_hrm.entity.Employee.WorkStatus;

import java.time.LocalDate;

@Data
public class EmployeeRequest {

    // ================= EMPLOYEE INFO =================

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotNull(message = "Gender is required")
    private Gender gender;

    @NotNull(message = "Birth date is required")
    private LocalDate birthDate;

    @NotBlank(message = "Phone number is required")
    private String phone;

    @NotBlank(message = "Address is required")
    private String address;

    @NotNull(message = "Please select department")
    private Integer departmentId;

    @NotNull(message = "Please select position")
    private Integer positionId;

    @NotNull(message = "Please select work status")
    private WorkStatus workStatus;


    // ================= USER INFO =================

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters long")
    private String password;

    @NotNull(message = "Please select role")
    private Integer roleId;
}