package poly.edu.vantix_hrm.dto.department;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateDepartmentRequest {

    @NotBlank(message = "Department name is required")
    private String departmentName;

    private String description;
}
