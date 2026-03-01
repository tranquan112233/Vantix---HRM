package poly.edu.vantix_hrm.dto.department;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateDepartmentRequest {

    @NotBlank(message = "Department name is required")
    private String name;

    private String description;
}
