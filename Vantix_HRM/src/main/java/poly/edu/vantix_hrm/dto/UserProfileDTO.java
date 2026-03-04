package poly.edu.vantix_hrm.DTO;

import lombok.Data;
import java.time.LocalDate;

@Data
public class UserProfileDTO {
    private Integer employeeId;
    private String username;
    private String email;
    private String fullName;
    private String phone;
    private String address;
    private LocalDate birthDate;
    private String gender; // "MALE", "FEMALE", "OTHER"
    private String department;
    private String position;
    private String avatarUrl;
}