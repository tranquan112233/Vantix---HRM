package poly.edu.vantix_hrm.dto.department;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DepartmentRequest {

    @NotBlank(message = "Department name is required")
    private String departmentName;

    private String description;
}
