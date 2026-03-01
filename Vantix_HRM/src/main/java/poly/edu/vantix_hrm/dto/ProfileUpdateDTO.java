package poly.edu.vantix_hrm.DTO;

import lombok.Data;
import java.time.LocalDate;

@Data
public class ProfileUpdateDTO {

    private String fullName;
    private String phone;
    private String address;
    private String gender;
    private LocalDate birthDate;
}