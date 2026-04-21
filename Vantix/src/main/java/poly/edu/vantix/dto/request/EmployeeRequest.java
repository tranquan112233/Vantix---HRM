package poly.edu.vantix.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import poly.edu.vantix.entity.enums.EmploymentStatus;
import poly.edu.vantix.entity.enums.Gender;

import java.time.LocalDate;

@Getter
@Setter
public class EmployeeRequest {

    @NotBlank(message = "Employee code is required")
    @Size(max = 30, message = "Employee code must be at most 30 characters")
    private String employeeCode;

    @NotBlank(message = "Full name is required")
    @Size(max = 150, message = "Full name must be at most 150 characters")
    private String fullName;

    private LocalDate dateOfBirth;

    private Gender gender;

    @Size(max = 20, message = "Citizen ID must be at most 20 characters")
    private String citizenId;

    @Size(max = 20, message = "Phone number must be at most 20 characters")
    private String phoneNumber;

    @Size(max = 100, message = "Email must be at most 100 characters")
    private String personalEmail;

    private String address;

    private Long departmentId;

    private Long positionId;

    private Long userId;

    private Boolean createUserAccount;

    @Size(max = 50, message = "Username must be at most 50 characters")
    private String accountUsername;

    @Email(message = "Invalid account email format")
    @Size(max = 100, message = "Account email must be at most 100 characters")
    private String accountEmail;

    @Size(max = 100, message = "Account password must be at most 100 characters")
    private String accountPassword;

    private Long accountRoleId;

    private LocalDate joinDate;

    private LocalDate terminationDate;

    private EmploymentStatus status;

    @Size(max = 50, message = "Bank account must be at most 50 characters")
    private String bankAccount;

    @Size(max = 30, message = "Tax code must be at most 30 characters")
    private String taxCode;

    @Size(max = 30, message = "Insurance number must be at most 30 characters")
    private String insuranceNumber;

    @Size(max = 150, message = "Emergency contact name must be at most 150 characters")
    private String emergencyContactName;

    @Size(max = 20, message = "Emergency contact phone must be at most 20 characters")
    private String emergencyContactPhone;
}
