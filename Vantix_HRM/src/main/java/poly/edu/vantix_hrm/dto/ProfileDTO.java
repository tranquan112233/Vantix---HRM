package poly.edu.vantix_hrm.DTO;

import lombok.Data;
import java.time.LocalDate;

@Data
public class ProfileDTO {

    private Integer userId;

    private String username;
    private String email;

    private String fullName;
    private String phone;
    private String address;
    private String gender;
    private LocalDate birthDate;

    private String department;
    private String position;
}