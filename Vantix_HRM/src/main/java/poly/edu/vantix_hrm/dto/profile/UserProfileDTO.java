package poly.edu.vantix_hrm.dto.profile;

import lombok.Data;
import java.time.LocalDate;

@Data
public class UserProfileDTO {
    private Integer employeeId;
    private String fullName;
    private String email;
    private String phone;
    private String address;
    private LocalDate birthDate;
    private String gender;

    // Thông tin công việc
    private String departmentName;
    private String positionName;
    private String workStatus;
}