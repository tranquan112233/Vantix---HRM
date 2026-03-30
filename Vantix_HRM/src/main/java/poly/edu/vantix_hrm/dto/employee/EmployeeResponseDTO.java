package poly.edu.vantix_hrm.dto.employee;

import lombok.*;
import poly.edu.vantix_hrm.entity.Employee.Gender;
import poly.edu.vantix_hrm.entity.Employee.WorkStatus;
import poly.edu.vantix_hrm.entity.User.UserStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeResponseDTO {

    private Long id;

    // Thông tin User
    private Long userId;
    private String username;
    private String email;
    private UserStatus userStatus;
    private LocalDateTime lastActive;

    // Thông tin Employee
    private String fullName;
    private Gender gender;
    private LocalDate birthDate;
    private String phone;
    private String address;
    private WorkStatus workStatus;

    // Thông tin Department
    private Long departmentId;
    private String departmentName;
    private Long managerId;
    private String managerName;

    // Thông tin Position
    private Long positionId;
    private String positionName;

    // Audit
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdBy;
    private Long updatedBy;
}