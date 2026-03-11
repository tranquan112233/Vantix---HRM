package poly.edu.vantix_hrm.dto.role;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.List;

@Getter
@Setter
public class RoleRequest {

    @NotBlank(message = "Role name is required")
    private String roleName;
    private String description;
}