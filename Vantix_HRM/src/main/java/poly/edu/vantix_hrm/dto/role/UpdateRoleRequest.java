package poly.edu.vantix_hrm.dto.role;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
public class UpdateRoleRequest {

    @NotBlank(message = "Role name is required")
    private String roleName;

    private String description;
}