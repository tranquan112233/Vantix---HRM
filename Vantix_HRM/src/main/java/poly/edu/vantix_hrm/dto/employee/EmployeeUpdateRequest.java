package poly.edu.vantix_hrm.dto.employee;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.*;
import lombok.Data;
import poly.edu.vantix_hrm.entity.Employee;

import java.time.LocalDate;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true) // 🔥 KHIÊN SỐ 1: Ai gửi thừa data (như employeeId, username) thì cứ bơ đi, không được báo lỗi 400.
public class EmployeeUpdateRequest {

    // === USER INFO ===
    @Email(message = "Invalid email")
    @NotBlank(message = "Email is required")
    private String email;

    @NotNull(message = "Role is required")
    private List<Integer> roleIds;

    // Danh sách quyền cho riêng User này
    private List<String> permissions;

    // === EMPLOYEE INFO ===
    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotNull(message = "Gender is required")
    private Employee.Gender gender;

    @NotNull(message = "Birth date is required")
    @JsonFormat(pattern = "yyyy-MM-dd") // 🔥 KHIÊN SỐ 2: Dạy cho Spring Boot biết cách đọc ngày tháng gửi từ Vue xuống
    private LocalDate birthDate;

    @NotBlank(message = "Phone is required")
    private String phone;

    @NotBlank(message = "Address is required")
    private String address;

    @NotNull(message = "Department is required")
    private Integer departmentId;

    @NotNull(message = "Position is required")
    private Integer positionId;

    private Employee.WorkStatus workStatus;
}