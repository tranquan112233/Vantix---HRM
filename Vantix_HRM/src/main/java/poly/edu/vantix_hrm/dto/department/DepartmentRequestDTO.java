package poly.edu.vantix_hrm.dto.department;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepartmentRequestDTO {

    @NotBlank(message = "Tên phòng ban không được để trống!")
    private String name;

    private String description;

    private Long managerId; // ID của trưởng phòng (Employee)
}