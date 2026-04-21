package poly.edu.vantix.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class RoleRequest {

    @NotBlank(message = "Role name is required")
    @Size(max = 50, message = "Role name must be at most 50 characters")
    private String name;

    @Size(max = 255, message = "Description must be at most 255 characters")
    private String description;

    // Danh sách id permission gán cho role
    private Set<Long> permissionIds;
}
