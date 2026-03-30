package poly.edu.vantix_hrm.dto.role;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleRequestDTO {

    @NotBlank(message = "Role name is required!")
    private String name;

    private String description;

    // Danh sách ID Permission gán cho Role
    private Set<Long> permissionIds;
}